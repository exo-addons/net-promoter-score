package org.exoplatform.nps.portlet.npsCharts;

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
import org.exoplatform.nps.dto.NPSDetailsDTO;
import org.exoplatform.nps.dto.ScoreEntryDTO;
import org.exoplatform.nps.dto.ScoreTypeDTO;
import org.exoplatform.nps.services.NpsService;
import org.exoplatform.nps.services.NpsTypeService;
import org.exoplatform.nps.services.Utils;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.portlet.PortletMode;
import javax.portlet.PortletPreferences;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by exo on 8/3/16.
 */
public class NPSChartsController {

  private static Log  LOG = ExoLogger.getLogger(NPSChartsController.class);
  private String     bundleString;
  ResourceBundle     bundle;
  private static String SCORE_TYPE = "exo.nps.addon.selectedType";


  @Inject
  NpsService npsService;

  @Inject
  NpsTypeService npsTypeService;

  @Inject
  IdentityManager identityManager;


  @Inject
  @Path("index.gtmpl")
  Template            indexTmpl;

  @Inject
  @Path("edit.gtmpl")
  Template editTmpl;


  @View
  public Response.Content index(RequestContext requestContext) {

    PortletMode mode = requestContext.getProperty(JuzuPortlet.PORTLET_MODE);

    if (PortletMode.EDIT.equals(mode)) {
      Request request = Request.getCurrent();
      PortletRequestBridge bridge = (PortletRequestBridge) request.getBridge();
      PortletPreferences prefs = bridge.getPortletRequest().getPreferences();
      String selectedType = prefs.getValue(SCORE_TYPE, "");
      List<ScoreTypeDTO> scoreTypes=npsTypeService.getScoreTypes(0,0);
      Map<String, Object> parameters = new HashMap<>();
      parameters.put("scoreTypes", scoreTypes);
      parameters.put("selectedType", selectedType);

      return editTmpl.with(parameters).ok();
    } else {
      return indexTmpl.ok();
    }
  }

  private final String currentUser = ConversationState.getCurrent().getIdentity().getUserId();

  @Action
  @Route("updateSettings")
  public Response.Content updateSettings(String respondedCookiesExpiration, String reportedCookiesExpiration,String typeId, String firstDisplayDelay , String displayPopup) throws Exception {
    Request request = Request.getCurrent();
    PortletRequestBridge bridge = (PortletRequestBridge) request.getBridge();
    PortletPreferences prefs = bridge.getPortletRequest().getPreferences();
    prefs.setValue(SCORE_TYPE, typeId);
    prefs.store();
    return indexTmpl.ok();
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
      Request request = Request.getCurrent();
      PortletRequestBridge bridge = (PortletRequestBridge) request.getBridge();
      PortletPreferences prefs = bridge.getPortletRequest().getPreferences();
      String scoreTypeId = prefs.getValue(SCORE_TYPE, "");
      JSON data = new JSON();
      ScoreTypeDTO  sType = npsTypeService.getScoreType(Long.parseLong(scoreTypeId));
      long typeId=sType.getId();

      data.set("typeId",typeId);

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


      JSONArray npsList = new JSONArray();

      List <NPSDetailsDTO> npsDetails = Utils.getWeeklyNPS(typeId);

      for(NPSDetailsDTO nps : npsDetails){
        JSONObject nps_ = new JSONObject();
        Calendar c=Calendar.getInstance();
        c.setTimeInMillis(nps.getNpsDate());
        nps_.put("npsDate","W "+c.get(Calendar.WEEK_OF_YEAR)+"-"+c.get(Calendar.YEAR));
        nps_.put("score",String.format("%.2f", nps.getNpScore()));
        npsList.put(nps_);
      }

      data.set("weeklyNpScore",npsList);
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
  public Response getNPSLineChart(Long typeId, String chartType) {
    if(chartType.equals("weekly")){
      return  getWeeklyNPS(typeId);
    }else if(chartType.equals("byWeek")){
      return  getNPSByWeek(typeId);
    }else return Response.notFound();
  }


  @Ajax
  @juzu.Resource
  @MimeType.JSON
  @Jackson
  public Response getWeeklyNPS(Long typeId) {
    try {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        JSONArray npsList = new JSONArray();
      List <NPSDetailsDTO> npsDetails = Utils.getWeeklyNPS(typeId);
      for(NPSDetailsDTO nps : npsDetails){
        JSONObject nps_ = new JSONObject();
        Calendar c=Calendar.getInstance();
        c.setTimeInMillis(nps.getNpsDate());
        nps_.put("npsFullDate",sdf.format(c.getTime()));
        nps_.put("npsDetails",String.format("%.2f", nps.getNpScore())+" (Detractors: "+nps.getDetractorsNbr()+", Passives: "+nps.getPassivesNb()+", Promoters: "+nps.getPromotersNbr()+")");
        nps_.put("npsDate","W "+c.get(Calendar.WEEK_OF_YEAR)+"-"+c.get(Calendar.YEAR));
        nps_.put("score",String.format("%.2f", nps.getNpScore()));
        npsList.put(nps_);
      }
      return Response.ok(npsList.toString());
    } catch (Throwable e) {
      LOG.error("error while getting context", e);
      return Response.status(500);
    }
  }



  @Ajax
  @juzu.Resource
  @MimeType.JSON
  @Jackson
  public Response getNPSByWeek(Long typeId) {
    try {

      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      JSONArray npsList = new JSONArray();
      List <NPSDetailsDTO> npsDetails = Utils.getNPSByWeek(typeId);
      for(NPSDetailsDTO nps : npsDetails){
        JSONObject nps_ = new JSONObject();
        Calendar c=Calendar.getInstance();
        c.setTimeInMillis(nps.getNpsDate());
        nps_.put("npsFullDate",sdf.format(c.getTime()));
        nps_.put("npsDetails",String.format("%.2f", nps.getNpScore())+" (Detractors: "+nps.getDetractorsNbr()+", Passives: "+nps.getPassivesNb()+", Promoters: "+nps.getPromotersNbr()+")");
        nps_.put("npsDate","W "+c.get(Calendar.WEEK_OF_YEAR)+"-"+c.get(Calendar.YEAR));
        nps_.put("score",String.format("%.2f", nps.getNpScore()));
        npsList.put(nps_);
      }
      return Response.ok(npsList.toString());
    } catch (Throwable e) {
      LOG.error("error while getting context", e);
      return Response.status(500);
    }
  }


  @Ajax
  @juzu.Resource
  @MimeType.JSON
  @Jackson
  public Response getNPSByMonth(Long typeId) {
    try {
      JSONArray npsList = new JSONArray();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List <NPSDetailsDTO> npsDetails = Utils.getWeeklyNPS(typeId);
      for(NPSDetailsDTO nps : npsDetails){
        JSONObject nps_ = new JSONObject();
        Calendar c=Calendar.getInstance();
        c.setTimeInMillis(nps.getNpsDate());
        nps_.put("npsFullDate",sdf.format(c.getTime()));
        nps_.put("npsDate","W "+c.get(Calendar.WEEK_OF_YEAR)+"-"+c.get(Calendar.YEAR));
        nps_.put("score",String.format("%.2f", nps.getNpScore()));
        npsList.put(nps_);
      }
      return Response.ok(npsList.toString());
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
