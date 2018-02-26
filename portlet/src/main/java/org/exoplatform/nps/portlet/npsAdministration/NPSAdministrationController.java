package org.exoplatform.nps.portlet.npsAdministration;

import juzu.*;
import juzu.impl.common.JSON;
import juzu.plugin.jackson.Jackson;
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
import java.text.SimpleDateFormat;
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
  SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");

  @Ajax
  @juzu.Resource
  @MimeType.JSON
  @Jackson
  public List<ScoreEntryDTO> getScores(Long typeId, String respCat, int offset, int limit) {
    try {
      List<ScoreEntryDTO> scores= new ArrayList<>();
       if(respCat.equals("detractors")){
         scores=npsService.getDetractorScores(typeId,offset,limit);
       } else if(respCat.equals("passives")){
         scores=npsService.getPassiveScores(typeId,offset,limit);
       } else if(respCat.equals("promoters")){
         scores=npsService.getPromotesScores(typeId,offset,limit);
       } else{
         scores=npsService.getScores(typeId,offset,limit);
       }
      for (ScoreEntryDTO score : scores){
          score.setUserFullName("Anonymous");
          score.setUserProfile("#");
        if(score.getUserId()!=null&&!((String)score.getUserId()).equals("")){
            Profile profile=identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, score.getUserId(), false).getProfile();
            score.setUserFullName(profile.getFullName());
            score.setUserProfile("/portal/intranet/profile/"+score.getUserId());
            if(profile.getAvatarUrl()!=null){
                score.setPosterAvatar(profile.getAvatarUrl());
            }else{
                score.setPosterAvatar("/eXoSkin/skin/images/system/UserAvtDefault.png");
            }
        }else{
            score.setPosterAvatar("/eXoSkin/skin/images/system/UserAvtDefault.png");
        }
        if(score.getScore()>8) {
          score.setCategory("promoter");
        }else if(score.getScore()<6) {
          score.setCategory("detractor");
        }else {
          score.setCategory("passive");
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
      long detractorsNbr= npsService.getDetractorsCount(typeId, true);
      long promotersNbr= npsService.getPromotersCount(typeId, true);
      long passivesNbr= scorsnbr-(promotersNbr+detractorsNbr);

      float detractorsPrc=((float)detractorsNbr/(float)scorsnbr)*100;
      float promotersPrc=((float)promotersNbr/(float)scorsnbr)*100;
      float passivesPrc=((float)passivesNbr/(float)scorsnbr)*100;
      float npScore= promotersPrc-detractorsPrc;

      data.set("detractorsNbr",detractorsNbr);
      data.set("promotersNbr",promotersNbr);
      data.set("passivesNbr",passivesNbr);
      data.set("detractorsPrc",String.format("%.2f", detractorsPrc));
      data.set("promotersPrc",String.format("%.2f", promotersPrc));
      data.set("passivesPrc",String.format("%.2f", passivesPrc));
      data.set("npScore",String.format("%.2f", npScore));


        long allScorsnbr= npsService.getScoreCount(typeId, false);
        long allDetractorsNbr= npsService.getDetractorsCount(typeId, false);
        long allPromotersNbr= npsService.getPromotersCount(typeId, false);
        long allPassivesNbr= allScorsnbr-(allDetractorsNbr+allPromotersNbr);
        data.set("scorsnbr",allScorsnbr);
        data.set("allDetractorsNbr",allDetractorsNbr);
        data.set("allPromotersNbr",allPromotersNbr);
        data.set("allPassivesNbr",allPassivesNbr);

      data.set("meanScore",String.format("%.2f",npsService.getMeanScore(typeId)));

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
    if(chartType.equals("global")){
      return  getWeeklyNPS(typeId);
    }else if(chartType.equals("weeklyOver")){
      return  getNPSByWeek(typeId);
    }else if(chartType.equals("monthlyOver")){
      return  getNPSByMonth(typeId);
    }else if(chartType.equals("rolling30")){
      return  getRollingAvg(typeId,30);
    }else if(chartType.equals("rolling7")){
      return  getRollingAvg(typeId,7);
    }else return Response.notFound();
  }


  @Ajax
  @juzu.Resource
  @MimeType.JSON
  @Jackson
  public Response getWeeklyNPS(Long typeId) {
    try {
      SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
      JSONArray npsList = new JSONArray();
      List <NPSDetailsDTO> npsDetails = Utils.getWeeklyNPS(typeId);
      for(NPSDetailsDTO nps : npsDetails){
        JSONObject nps_ = new JSONObject();
        Calendar c=Calendar.getInstance();
        c.setTimeInMillis(nps.getNpsToDate());
        nps_.put("npsDate","W: "+c.get(Calendar.WEEK_OF_YEAR)+"-"+c.get(Calendar.YEAR));
        nps_.put("npsDetails",String.format("%.2f", nps.getNpScore())+" ( Week: "+c.get(Calendar.WEEK_OF_YEAR)+"-"+c.get(Calendar.YEAR)+"Detractors: "+nps.getDetractorsNbr()+", Passives: "+nps.getPassivesNb()+", Promoters: "+nps.getPromotersNbr()+")");
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

      JSONArray npsList = new JSONArray();
      List <NPSDetailsDTO> npsDetails = Utils.getNPSByWeek(typeId);
      for(NPSDetailsDTO nps : npsDetails){
        JSONObject nps_ = new JSONObject();
        nps_.put("npsDate",sdf.format(nps.getNpsToDate()));
        nps_.put("npsDetails",Utils.npsToString(nps));
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
      List <NPSDetailsDTO> npsDetails = Utils.getNPSByMonth(typeId);
      for(NPSDetailsDTO nps : npsDetails){
        JSONObject nps_ = new JSONObject();
        nps_.put("npsDate",sdf.format(nps.getNpsToDate()));
        nps_.put("npsDetails",Utils.npsToString(nps));
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
  public Response getRollingAvg(Long typeId, int period) {
    try {

      JSONArray npsList = new JSONArray();
      List <NPSDetailsDTO> npsDetails = Utils.getRollingAvg(typeId, period);
      for(NPSDetailsDTO nps : npsDetails){
        JSONObject nps_ = new JSONObject();
        nps_.put("npsDate",sdf.format(nps.getNpsToDate()));
        nps_.put("npsDetails",Utils.npsToString(nps));
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
  public Response getRespCountByScore(Long typeId) {
    try {
      JSONArray resposesByScores = new JSONArray();
      List<Object[]> results =npsService.countGroupdByScores(typeId);
      for (int i = 0; i < results.size(); i++) {
        JSONObject data = new JSONObject();
        Object[] arr = results.get(i);
        data.put("score",arr[0]);
        data.put("count",arr[1]);
        resposesByScores.put(data);
      }
      return Response.ok(resposesByScores.toString());
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
    if (obj.getAnonymous()==null) obj.setAnonymous(false);
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
