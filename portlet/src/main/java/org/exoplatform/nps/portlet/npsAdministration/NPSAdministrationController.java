package org.exoplatform.nps.portlet.npsAdministration;

import juzu.MimeType;
import juzu.Path;
import juzu.Response;
import juzu.View;
import juzu.impl.common.JSON;
import juzu.plugin.jackson.Jackson;
import juzu.template.Template;
import org.exoplatform.commons.juzu.ajax.Ajax;
import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.nps.dto.ScoreEntryDTO;
import org.exoplatform.nps.services.NpsService;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;

import javax.inject.Inject;
import java.util.*;

/**
 * Created by exo on 8/3/16.
 */
public class NPSAdministrationController {
/*  private static final String SUPPORT_TEAM_NAME_DEFAULT = "support-team";
  private static final String SUPPORT_GROUP_NAME_CONFIGURATION = "exo.addon.cs.support.group.name";*/
  private static Log  LOG = ExoLogger.getLogger(NPSAdministrationController.class);
  private String     bundleString;
  ResourceBundle     bundle;


  @Inject
  NpsService npsService;

  @Inject
  IdentityManager identityManager;


  @Inject
  @Path("index.gtmpl")
  Template            indexTmpl;

  @View
  public Response.Content index() {
    return indexTmpl.ok();
  }

  private final String currentUser = ConversationState.getCurrent().getIdentity().getUserId();

  @Ajax
  @juzu.Resource
  @MimeType.JSON
  @Jackson
  public List<ScoreEntryDTO> getScores(int offset, int limit) {
    try {
      List<ScoreEntryDTO> scores=npsService.getScores(offset,limit);
      for (ScoreEntryDTO score : scores){
        Profile profile=identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, score.getUserId(), false).getProfile();
        score.setUserFullName(profile.getFullName());
      }
      return scores;
    } catch (Throwable e) {
      LOG.error(e);
      return null;
    }
  }


  @Ajax
  @juzu.Resource
  @MimeType.JSON
  @Jackson
  public Response getBundle() {
    try {
      if (!PropertyManager.isDevelopping() && bundleString != null && getResourceBundle().getLocale().equals(PortalRequestContext.getCurrentInstance().getLocale())) {
        return Response.ok(bundleString);
      }
      bundle = getResourceBundle(PortalRequestContext.getCurrentInstance().getLocale());
      JSON data = new JSON();
      Enumeration<String> enumeration = getResourceBundle().getKeys();
      while (enumeration.hasMoreElements()) {
        String key = (String) enumeration.nextElement();
        try {
          data.set(key.replaceAll("(.*)\\.", ""), getResourceBundle().getObject(key));
        } catch (MissingResourceException e) {
          // Nothing to do, this happens sometimes
        }
      }
      data.set("currentUser",currentUser);
      bundleString = data.toString();
      return Response.ok(bundleString);
    } catch (Throwable e) {
      LOG.error("error while getting categories", e);
      return Response.status(500);
    }
  }


  @Ajax
  @juzu.Resource
  @MimeType.JSON
  @Jackson
  public Response getData() {
    try {
      JSON data = new JSON();
      data.set("currentUser",currentUser);
      Profile profile=identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, currentUser, false).getProfile();
      if(profile.getAvatarUrl()!=null){
        data.set("currentUserAvatar",profile.getAvatarUrl());
      }else{
        data.set("currentUserAvatar","/eXoSkin/skin/images/system/UserAvtDefault.png");
      }
      data.set("currentUserName",profile.getFullName());

      long scorsnbr= npsService.getScoreCount();
      long detractorsNbr= npsService.getDetractorsCount();
      long promotersNbr= npsService.getPromotersCount();
      long passivesNbr= scorsnbr-(promotersNbr+detractorsNbr);


      float detractorsPrc=((float)detractorsNbr/(float)scorsnbr)*100;
      float promotersPrc=((float)promotersNbr/(float)scorsnbr)*100;
      float passivesPrc=((float)passivesNbr/(float)scorsnbr)*100;

      float npScore= promotersPrc-detractorsPrc;

      data.set("scorsnbr",scorsnbr);
      data.set("detractorsNbr",detractorsNbr);
      data.set("promotersNbr",promotersNbr);
      data.set("passivesNbr",passivesNbr);

      data.set("detractorsPrc",String.format("%.2f", detractorsPrc));
      data.set("promotersPrc",String.format("%.2f", promotersPrc));
      data.set("passivesPrc",String.format("%.2f", passivesPrc));
      data.set("npScore",String.format("%.2f", npScore));

      return Response.ok(data.toString());
    } catch (Throwable e) {
      LOG.error("error while getting context", e);
      return Response.status(500);
    }
  }



  private ResourceBundle getResourceBundle(Locale locale) {
    return bundle = ResourceBundle.getBundle("locale.portlet.nps-addon", locale, this.getClass().getClassLoader());
  }

  private ResourceBundle getResourceBundle() {
    if (bundle == null) {
      bundle = getResourceBundle(PortalRequestContext.getCurrentInstance().getLocale());
    }
    return bundle;
  }



}
