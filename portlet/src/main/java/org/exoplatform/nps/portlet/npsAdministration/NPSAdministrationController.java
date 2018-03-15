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
  public List<ScoreEntryDTO> getScores(Long typeId, String respCat, int offset, int limit, Long startDate, Long endDate) {
    try {
      List<ScoreEntryDTO> scores= new ArrayList<>();
       if(respCat.equals("detractors")){
         scores=npsService.getDetractorScores(typeId,offset,limit,startDate,endDate);
       } else if(respCat.equals("passives")){
         scores=npsService.getPassiveScores(typeId,offset,limit,startDate,endDate);
       } else if(respCat.equals("promoters")){
         scores=npsService.getPromotesScores(typeId,offset,limit,startDate,endDate);
       } else{
         scores=npsService.getScores(typeId,offset,limit,startDate,endDate);
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
  public Response getData(Long typeId, Long startDate, Long endDate) {
    try {
      JSON data = new JSON();
      if(startDate==0){
        startDate =npsService.getFirstScoreEntries(typeId).getPostedTime();
      }

      long scorsnbr= npsService.getScoreCount(typeId, true, startDate, endDate);
      long detractorsNbr= npsService.getDetractorsCount(typeId, true, startDate, endDate);
      long promotersNbr= npsService.getPromotersCount(typeId, true, startDate, endDate);
      long passivesNbr= scorsnbr-(promotersNbr+detractorsNbr);

      float detractorsPrc=((float)detractorsNbr/(float)scorsnbr)*100;
      float promotersPrc=((float)promotersNbr/(float)scorsnbr)*100;
      float passivesPrc=((float)passivesNbr/(float)scorsnbr)*100;
      float npScore= promotersPrc-detractorsPrc;
      data.set("enabledScorsNbr",scorsnbr);
      data.set("startDate",startDate);
      data.set("detractorsNbr",detractorsNbr);
      data.set("promotersNbr",promotersNbr);
      data.set("passivesNbr",passivesNbr);
      data.set("detractorsPrc",String.format("%.2f", detractorsPrc));
      data.set("promotersPrc",String.format("%.2f", promotersPrc));
      data.set("passivesPrc",String.format("%.2f", passivesPrc));
      data.set("npScore",String.format("%.2f", npScore));


        long allScorsnbr= npsService.getScoreCount(typeId, false, startDate, endDate);
        long allDetractorsNbr= npsService.getDetractorsCount(typeId, false, startDate, endDate);
        long allPromotersNbr= npsService.getPromotersCount(typeId, false, startDate, endDate);
        long allPassivesNbr= allScorsnbr-(allDetractorsNbr+allPromotersNbr);
        long allCount=npsService.getAllCount(typeId, startDate, endDate);
        double responseRate=0;
            if(allCount>0){
                responseRate= (allScorsnbr*100)/allCount;
        }
        data.set("scorsnbr",allScorsnbr);
        data.set("allDetractorsNbr",allDetractorsNbr);
        data.set("allPromotersNbr",allPromotersNbr);
        data.set("allPassivesNbr",allPassivesNbr);
        data.set("disablesScoresNbr",allScorsnbr-scorsnbr);
        float dashoffset = 300-(3*npScore);
        data.set("dashoffset",String.format("%.2f", dashoffset));

      data.set("meanScore",String.format("%.2f",npsService.getMeanScore(typeId, startDate, endDate)));
      data.set("responseRate",responseRate);

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
  public Response getNPSLineChart(Long typeId, String chartType, Long startDate, Long endDate) {
    if(startDate==0){
      startDate =npsService.getFirstScoreEntries(typeId).getPostedTime();
    }
    if(chartType.equals("global")){
      return  getWeeklyNPS(typeId, startDate, endDate);
    }else if(chartType.equals("weeklyOver")){
      return  getNPSByWeek(typeId, startDate, endDate);
    }else if(chartType.equals("monthlyOver")){
      return  getNPSByMonth(typeId, startDate, endDate);
    }else if(chartType.equals("rolling30")){
      return  getRollingAvg(typeId,30, startDate, endDate);
    }else if(chartType.equals("rolling7")){
      return  getRollingAvg(typeId,7, startDate, endDate);
    }else return Response.notFound();
  }


  @Ajax
  @juzu.Resource
  @MimeType.JSON
  @Jackson
  public Response getWeeklyNPS(Long typeId, Long startDate, Long endDate) {
    try {
      SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
      JSONArray npsList = new JSONArray();
      List <NPSDetailsDTO> npsDetails = Utils.getWeeklyNPS(typeId, startDate, endDate);
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
  public Response getNPSByWeek(Long typeId, Long startDate, Long endDate) {
    try {

      JSONArray npsList = new JSONArray();
      List <NPSDetailsDTO> npsDetails = Utils.getNPSByWeek(typeId, startDate, endDate);
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
  public Response getNPSByMonth(Long typeId, Long startDate, Long endDate) {
    try {

      JSONArray npsList = new JSONArray();
      List <NPSDetailsDTO> npsDetails = Utils.getNPSByMonth(typeId, startDate, endDate);
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
  public Response getRollingAvg(Long typeId, int period, Long startDate, Long endDate) {
    try {

      JSONArray npsList = new JSONArray();
      List <NPSDetailsDTO> npsDetails = Utils.getRollingAvg(typeId, period, startDate, endDate);
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
  @Resource
  @MimeType.JSON
  @Jackson
  public Response getRespCountByScore(Long typeId, Long startDate, Long endDate) {
    try {
      Map<String, Long> scores = new HashMap<String, Long>();

      scores.put("0", (long) 0);
      scores.put("1", (long) 0);
      scores.put("2", (long) 0);
      scores.put("3", (long) 0);
      scores.put("4", (long) 0);
      scores.put("5", (long) 0);
      scores.put("6", (long) 0);
      scores.put("7", (long) 0);
      scores.put("8", (long) 0);
      scores.put("9", (long) 0);
      scores.put("10", (long) 0);



      JSONArray resposesByScores = new JSONArray();
      List<Object[]> results =npsService.countGroupdByScores(typeId, startDate, endDate);
      long total=npsService.getScoreCount(typeId, true, startDate, endDate);
      for (int i = 0; i < results.size(); i++) {
        Object[] arr = results.get(i);
        scores.put(((Integer) arr[0]).toString(),(Long)arr[1]);
      }
      for(String key: scores.keySet()){
        JSONObject data = new JSONObject();
        data.put("score",key);
        data.put("count",scores.get(key));
        if(total!=0){
          data.put("percent",(((Long)scores.get(key)).longValue()*100)/total);
        }else{
          data.put("percent",0);
        }

        if(Integer.parseInt(key)>8){
          data.put("category","promoter");
        }else if(Integer.parseInt(key)<7){
          data.put("category","detractor");
        }else data.put("category","passive");
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
