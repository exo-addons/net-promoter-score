package org.exoplatform.nps.portlet.npsAdministration;

import juzu.*;
import juzu.impl.common.JSON;
import juzu.plugin.jackson.Jackson;
import juzu.template.Template;
import org.exoplatform.commons.juzu.ajax.Ajax;
import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.nps.dto.ScoreEntryDTO;
import org.exoplatform.nps.dto.ScoreTypeDTO;
import org.exoplatform.nps.services.NpsService;
import org.exoplatform.nps.services.NpsTypeService;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;

import javax.inject.Inject;
import java.util.*;

/**
 * Created by exo on 8/3/16.
 */
public class NPSAdministrationController {

  private static Log  LOG = ExoLogger.getLogger(NPSAdministrationController.class);
  private String     bundleString;
  ResourceBundle     bundle;


  @Inject
  NpsService npsService;

  @Inject
  NpsTypeService npsTypeService;

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
  public List<ScoreEntryDTO> getScores(Long typeId, int offset, int limit) {
    try {
      List<ScoreEntryDTO> scores=npsService.getScores(typeId,offset,limit);
      for (ScoreEntryDTO score : scores){
        if(score.getUserId()!=null&&!((String)score.getUserId()).equals("")){
            Profile profile=identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, score.getUserId(), false).getProfile();
            score.setUserFullName(profile.getFullName());
        }
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
  public Response getData(Long typeId) {
    try {
      JSON data = new JSON();
      long scorsnbr= npsService.getScoreCount(typeId, true);
      long detractorsNbr= npsService.getDetractorsCount(typeId);
      long promotersNbr= npsService.getPromotersCount(typeId);
      long passivesNbr= scorsnbr-(promotersNbr+detractorsNbr);


      float detractorsPrc=((float)detractorsNbr/(float)scorsnbr)*100;
      float promotersPrc=((float)promotersNbr/(float)scorsnbr)*100;
      float passivesPrc=((float)passivesNbr/(float)scorsnbr)*100;

      float npScore= promotersPrc-detractorsPrc;

      data.set("scorsnbr",npsService.getScoreCount(typeId,false));
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


  @Ajax
  @juzu.Resource
  @MimeType.JSON
  @Jackson
  public List<ScoreTypeDTO> getScoreTypes() {
    try {
      return npsTypeService.getScoreTypes(0,0);
    } catch (Throwable e) {
      LOG.error(e);
      return null;
    }
  }

  @Ajax
  @juzu.Resource
  @MimeType.JSON
  @Jackson
  public ScoreTypeDTO getScoreTypeById (Long id) {

    return npsTypeService.getScoreType(id);

  }

  @Ajax
  @Resource(method = HttpMethod.POST)
  @MimeType.JSON
  @Jackson
  public void saveType(@Jackson ScoreTypeDTO obj) {

    npsTypeService.save(obj,true);

  }

  @Ajax
  @Resource(method = HttpMethod.POST)
  @MimeType.JSON
  @Jackson
  public void updateType(@Jackson ScoreTypeDTO obj) {

    npsTypeService.save(obj,false);

  }


  @Ajax
  @Resource(method = HttpMethod.POST)
  @MimeType.JSON
  @Jackson
  public void deleteScore(@Jackson ScoreEntryDTO obj) {

    npsService.remove(obj);

  }

  @Ajax
  @Resource(method = HttpMethod.POST)
  @MimeType.JSON
  @Jackson
  public void disableScore(@Jackson ScoreEntryDTO obj) {
    obj.setEnabled(false);
    npsService.save(obj,false);

  }


  @Ajax
  @Resource(method = HttpMethod.POST)
  @MimeType.JSON
  @Jackson
  public void enableScore(@Jackson ScoreEntryDTO obj) {
    obj.setEnabled(true);
    npsService.save(obj,false);

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
