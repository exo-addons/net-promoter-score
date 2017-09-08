package org.exoplatform.nps.portlet.npsForm;

import juzu.*;
import juzu.bridge.portlet.JuzuPortlet;
import juzu.impl.bridge.spi.portlet.PortletRequestBridge;
import juzu.impl.common.JSON;
import juzu.impl.request.Request;
import juzu.plugin.jackson.Jackson;
import juzu.request.RequestContext;
import juzu.template.Template;
import org.exoplatform.commons.juzu.ajax.Ajax;
import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.nps.dto.*;
import org.exoplatform.nps.services.*;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;

import javax.inject.Inject;
import javax.portlet.PortletMode;
import javax.portlet.PortletPreferences;
import java.util.*;


@SessionScoped
public class NPSFormController {
  private static Log log = ExoLogger.getLogger(NPSFormController.class);

  // Don't use inject to not get the merge of all resource bundles
  // @Inject
  ResourceBundle     bundle;

  @Inject
  NpsService npsService;

  @Inject
  IdentityManager identityManager;

  @Inject
  @Path("index.gtmpl")
  Template           indexTmpl;


  @Inject
  @Path("edit.gtmpl")
  Template           editTmpl;

  private String     bundleString;

  private final String currentUser = ConversationState.getCurrent().getIdentity().getUserId();

  private static String RESP_COOKIES_EXP = "exo.nps.addon.respondedCookiesExpiration";
  private static String RESP_COOKIES_EXP_DEFAULT_VALUE = "30";
  private static String REPORTED_COOKIES_EXP_DEFAULT_VALUE = "10";
  private static String REPORTED_COOKIES_EXP = "exo.nps.addon.reportedCookiesExpiration";

  @View
  public Response.Content index(RequestContext requestContext) {

    PortletMode mode = requestContext.getProperty(JuzuPortlet.PORTLET_MODE);

    if(PortletMode.EDIT.equals(mode)) {
      Request request = Request.getCurrent();
      PortletRequestBridge bridge = (PortletRequestBridge)request.getBridge();
      PortletPreferences prefs = bridge.getPortletRequest().getPreferences();
      String respondedCookiesExpiration = prefs.getValue(RESP_COOKIES_EXP,RESP_COOKIES_EXP_DEFAULT_VALUE);
      String reportedCookiesExpiration = prefs.getValue(REPORTED_COOKIES_EXP,REPORTED_COOKIES_EXP_DEFAULT_VALUE);
      if (respondedCookiesExpiration==null || respondedCookiesExpiration.equals(""))respondedCookiesExpiration=RESP_COOKIES_EXP_DEFAULT_VALUE;
      if (reportedCookiesExpiration==null || reportedCookiesExpiration.equals(""))reportedCookiesExpiration=REPORTED_COOKIES_EXP_DEFAULT_VALUE;
      Map<String,Object> parameters = new HashMap<>();
      parameters.put("respondedCookiesExpiration",respondedCookiesExpiration);
      parameters.put("reportedCookiesExpiration",reportedCookiesExpiration);
      return editTmpl.with(parameters).ok();
    } else {
      return indexTmpl.ok();
    }
  }



  @Ajax
  @Resource(method = HttpMethod.POST)
  @MimeType.JSON
  @Jackson
  public void saveScore(@Jackson ScoreEntryDTO obj) {
    obj.setEnabled(true);
    obj.setUserId(currentUser);
    npsService.save(obj,true);

  }




  @Ajax
  @juzu.Resource
  @MimeType.JSON
  @Jackson
  public Response getContext() {
    try {
      Request request = Request.getCurrent();
      PortletRequestBridge bridge = (PortletRequestBridge)request.getBridge();
      PortletPreferences prefs = bridge.getPortletRequest().getPreferences();
      String respondedCookiesExpiration = prefs.getValue(RESP_COOKIES_EXP,RESP_COOKIES_EXP_DEFAULT_VALUE);
      String reportedCookiesExpiration = prefs.getValue(REPORTED_COOKIES_EXP,REPORTED_COOKIES_EXP_DEFAULT_VALUE);
      if (respondedCookiesExpiration==null || respondedCookiesExpiration.equals(""))respondedCookiesExpiration=RESP_COOKIES_EXP_DEFAULT_VALUE;
      if (reportedCookiesExpiration==null || reportedCookiesExpiration.equals(""))reportedCookiesExpiration=REPORTED_COOKIES_EXP_DEFAULT_VALUE;
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
      Profile profile=identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, currentUser, false).getProfile();

      data.set("currentUser",currentUser);
      data.set("fullName",profile.getFullName());
      data.set("respondedCookiesExpiration",respondedCookiesExpiration);
      data.set("reportedCookiesExpiration",reportedCookiesExpiration);
      bundleString = data.toString();
      return Response.ok(bundleString);
    } catch (Throwable e) {
      log.error("error while getting bundele", e);
      return Response.status(500);
    }
  }

  @Action
  @Route("updateSettings")
  public Response.Content updateSettings(String respondedCookiesExpiration,String reportedCookiesExpiration) throws Exception {
    Request request = Request.getCurrent();
    PortletRequestBridge bridge = (PortletRequestBridge)request.getBridge();
    PortletPreferences prefs = bridge.getPortletRequest().getPreferences();
    prefs.setValue(RESP_COOKIES_EXP,respondedCookiesExpiration);
    prefs.setValue(REPORTED_COOKIES_EXP,reportedCookiesExpiration);
    prefs.store();
    return indexTmpl.ok();
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
