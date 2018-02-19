package org.exoplatform.nps.services;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.nps.dto.NPSDetailsDTO;
import org.exoplatform.nps.dto.ScoreEntryDTO;
import org.exoplatform.nps.dto.ScoreTypeDTO;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import javax.jcr.Node;
import javax.jcr.Session;
import java.text.SimpleDateFormat;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.LocalDate;

public class Utils
{

    private static Log log = ExoLogger.getLogger(Utils.class);


    /**
     * Get first login
     *
     * @return first login date
     * @throws Exception
     */
    public static Calendar getFirstLoginDate(String userId) throws Exception {

        RepositoryService repositoryService = CommonsUtils.getService(RepositoryService.class);
        SessionProvider sessionProvider = SessionProvider.createSystemProvider();
        try {
            Session session = sessionProvider.getSession("collaboration",repositoryService.getCurrentRepository());
            String path="exo:LoginHistoryHome/"+userId;
            Node rootNode = session.getRootNode();
            if (rootNode.hasNode(path)) {
                Node att= rootNode.getNode(path);
                return(att.getProperty("exo:dateCreated").getDate());
            }else{
                return null;
            }
        } catch (Exception e) {
            log.error("Error while getting the date: ", e.getMessage());
        } finally {
            sessionProvider.close();
        }
        return null;
    }


    public static int getDiffinDays(Calendar startCal, Calendar stopCal) throws Exception {

        long startMillis = startCal.getTimeInMillis();
        DateTime startDateTime = new DateTime(startMillis);
        long stopMillis = stopCal.getTimeInMillis();
        DateTime stopDateTime = new DateTime(stopMillis);
        LocalDate start = startDateTime.toLocalDate();
        LocalDate stop = stopDateTime.toLocalDate();
        return Days.daysBetween(start, stop).getDays();
    }

    public static void createActivity (ScoreEntryDTO score){
        NpsTypeService npsTypeService= CommonsUtils.getService(NpsTypeService.class);
        ScoreTypeDTO scoreType = npsTypeService.getScoreType(score.getTypeId());
        if(scoreType!=null){
            if(scoreType.getLinkedToSpace()!=null&&scoreType.getLinkedToSpace()){
                SpaceService spaceService= CommonsUtils.getService(SpaceService.class);
                IdentityManager identityManager= CommonsUtils.getService(IdentityManager.class);
                ActivityManager activityManager= CommonsUtils.getService(ActivityManager.class);
                Space space = spaceService.getSpaceByPrettyName(scoreType.getSpaceId());
                if(space==null){
                    log.warn("Space bot found");
                }else{
                    Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getPrettyName(), false);
                    Identity posterIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, scoreType.getUserId(), false);
                    String userName = "Anonymous";
                    if(score.getUserId()!=null&&!"".equals(score.getUserId())){
                        Identity userIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, score.getUserId(), false);
                        if(userIdentity!=null){
                            //userName =userIdentity.getProfile().getFullName();
                            userName="<a  href=\""+userIdentity.getProfile().getUrl()+"\">"+userIdentity.getProfile().getFullName()+" </a>";
                        }else userName=score.getUserId();
                    }
                    if(posterIdentity!=null&&spaceIdentity!=null){
                        ExoSocialActivity activity = new ExoSocialActivityImpl();
                        activity.setType("DEFAULT_ACTIVITY");
                        activity.setTitle("<span id='npsActivity'>\n" +
                                "A new response has been added to the "+scoreType.getTypeName()+" survey: <br/>\n" +
                                " <b>User Name : </b>"+userName+"<br/>\n" +
                                " <b>Score : </b>"+score.getScore()+"<br/>\n" +
                                " <b>Comment: </b>"+score.getComment()+ "<br/>");
                        activity.setUserId(posterIdentity.getId());
                        activityManager.saveActivityNoReturn(spaceIdentity, activity);
                    }else{
                        log.warn("Not able to create the activity, the Poster or Space Identity is missing");
                    }
                }

            }
        }

    }

    public static NPSDetailsDTO calculateNpsByDate (long typeId,long date){
        NpsService npsService= CommonsUtils.getService(NpsService.class);
        long scorsnbr= npsService.getScoreCountByDate(typeId, date);
        long detractorsNbr= npsService.getDetractorsCountByDate(typeId, date);
        long promotersNbr= npsService.getPromotersCountByDate(typeId, date);
        long passivesNbr= scorsnbr-(promotersNbr+detractorsNbr);
        return new NPSDetailsDTO(typeId, 0,date, scorsnbr, detractorsNbr,  promotersNbr, passivesNbr);
    }

    public static NPSDetailsDTO calculateNpsByPeriod (long typeId,long from, long to){
        NpsService npsService= CommonsUtils.getService(NpsService.class);
        long scorsnbr= npsService.getScoreCountByPeriod(typeId, from, to);
        long detractorsNbr= npsService.getDetractorsCountByPeriod(typeId, from, to);
        long promotersNbr= npsService.getPromotersCountByPeriod(typeId, from, to);
        long passivesNbr= scorsnbr-(promotersNbr+detractorsNbr);
        return new NPSDetailsDTO(typeId, from, to, scorsnbr, detractorsNbr,  promotersNbr, passivesNbr);
    }



    public static List<NPSDetailsDTO> getWeeklyNPSbyDates(long typeId,long fromDate, long toDate){
        NpsService npsService= CommonsUtils.getService(NpsService.class);
        List<NPSDetailsDTO> NPSScors=new ArrayList<NPSDetailsDTO>();
        ScoreEntryDTO score = npsService.getFirstScoreEntries(typeId);
        if(score!=null) {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(fromDate);
            Calendar c_=Calendar.getInstance();
            c_.setTimeInMillis(score.getPostedTime());
            if(c.before(c_)) c.setTime(c_.getTime());
            int diff = Calendar.SATURDAY - c.get(Calendar.DAY_OF_WEEK);
            c.add(Calendar.DATE, diff);
            while (c.getTimeInMillis() < toDate) {
                NPSScors.add(calculateNpsByDate(typeId, c.getTimeInMillis()));
                c.add(Calendar.DATE, 7);
            }
            if (c.getTimeInMillis() != toDate) {
                NPSScors.add(calculateNpsByDate(typeId, toDate));
            }
        }
      return NPSScors;

    }

    public static List<NPSDetailsDTO> getWeeklyNPS(long typeId){
        NpsService npsService= CommonsUtils.getService(NpsService.class);
        List<NPSDetailsDTO> NPSScors=new ArrayList<NPSDetailsDTO>();
        ScoreEntryDTO score = npsService.getFirstScoreEntries(typeId);
        if(score!=null){
            Calendar fromDate=Calendar.getInstance();
            fromDate.setTimeInMillis(score.getPostedTime());
            int diff= Calendar.SATURDAY-fromDate.get(Calendar.DAY_OF_WEEK);
            fromDate.add(Calendar.DATE, diff);
            fromDate.set(Calendar.HOUR_OF_DAY, 0);
            fromDate.set(Calendar.MINUTE, 0);
            fromDate.set(Calendar.SECOND, 0);
            Calendar toDate=Calendar.getInstance();
            while(fromDate.before(toDate)){
                NPSScors.add(calculateNpsByDate (typeId,fromDate.getTimeInMillis()));
                fromDate.add(Calendar.DATE, 7);
            }
            if(fromDate.after(toDate)){
                NPSScors.add(calculateNpsByDate (typeId,fromDate.getTimeInMillis()));
            }
        }
        return NPSScors;
    }


    public static List<NPSDetailsDTO> getNPSByWeek(long typeId){
        NpsService npsService= CommonsUtils.getService(NpsService.class);
        List<NPSDetailsDTO> NPSScors=new ArrayList<NPSDetailsDTO>();
        ScoreEntryDTO score = npsService.getFirstScoreEntries(typeId);
        if(score!=null){
            Calendar fromDate=Calendar.getInstance();
            fromDate.setTimeInMillis(score.getPostedTime());
            fromDate.set(Calendar.HOUR_OF_DAY, 0);
            fromDate.set(Calendar.MINUTE, 0);
            fromDate.set(Calendar.SECOND, 0);
            Calendar toDate=Calendar.getInstance();
            int diff= Calendar.SATURDAY-fromDate.get(Calendar.DAY_OF_WEEK)+2;
            Calendar to_=Calendar.getInstance();
            to_.setTime(fromDate.getTime());
            to_.add(Calendar.DATE, diff);
            while(fromDate.before(toDate)){
                NPSScors.add(calculateNpsByPeriod (typeId,fromDate.getTimeInMillis(), to_.getTimeInMillis()));
                fromDate.setTime(to_.getTime());
                to_.add(Calendar.DATE, 7);
            }

        }
        return NPSScors;
    }



    public static List<NPSDetailsDTO> getNPSByMonth(long typeId){
        NpsService npsService= CommonsUtils.getService(NpsService.class);
        List<NPSDetailsDTO> NPSScors=new ArrayList<NPSDetailsDTO>();
        ScoreEntryDTO score = npsService.getFirstScoreEntries(typeId);
        if(score!=null){

            Calendar fromDate=Calendar.getInstance();
            fromDate.setTimeInMillis(score.getPostedTime());
            fromDate.set(Calendar.HOUR_OF_DAY, 0);
            fromDate.set(Calendar.MINUTE, 0);
            fromDate.set(Calendar.SECOND, 0);
            Calendar toDate=Calendar.getInstance();
            Calendar to_=Calendar.getInstance();
            to_.setTime(fromDate.getTime());
            to_.set(Calendar.DAY_OF_MONTH, fromDate.getActualMaximum(Calendar.DAY_OF_MONTH));
            while(fromDate.before(toDate)){
                NPSScors.add(calculateNpsByPeriod (typeId,fromDate.getTimeInMillis(), to_.getTimeInMillis()));
                fromDate.set(Calendar.MONTH,fromDate.get(Calendar.MONTH)+1);
                fromDate.set(Calendar.DAY_OF_MONTH,1);
                to_.setTime(fromDate.getTime());
                to_.set(Calendar.DAY_OF_MONTH, fromDate.getActualMaximum(Calendar.DAY_OF_MONTH));

            }

        }
        return NPSScors;
    }

    public static List<NPSDetailsDTO> getRollingAvg(long typeId, int period){
        NpsService npsService= CommonsUtils.getService(NpsService.class);
        List<NPSDetailsDTO> NPSScors=new ArrayList<NPSDetailsDTO>();
        ScoreEntryDTO score = npsService.getFirstScoreEntries(typeId);
        if(score!=null){
            Calendar fromDate=Calendar.getInstance();
            fromDate.setTimeInMillis(score.getPostedTime());
            fromDate.set(Calendar.HOUR_OF_DAY, 0);
            fromDate.set(Calendar.MINUTE, 0);
            fromDate.set(Calendar.SECOND, 0);
            Calendar toDate=Calendar.getInstance();
            Calendar to_=Calendar.getInstance();
            to_.setTime(fromDate.getTime());
            to_.add(Calendar.DATE, period);
            while(fromDate.before(toDate)){
                NPSScors.add(calculateNpsByPeriod (typeId,fromDate.getTimeInMillis(), to_.getTimeInMillis()));
                fromDate.add(Calendar.DATE, 1);
                to_.add(Calendar.DATE, 1);
            }

        }
        return NPSScors;
    }

    public static  String npsToString(NPSDetailsDTO nps){
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        Calendar from=Calendar.getInstance();
        from.setTimeInMillis(nps.getNpsFromDate());
        Calendar to=Calendar.getInstance();
        to.setTimeInMillis(nps.getNpsToDate());
        return                  "Period : from "+sdf.format(from.getTime())+" to "+sdf.format(to.getTime())+"\n" +
                "Score : "+String.format("%.2f", nps.getNpScore())+"\n" +
                "Detractors: "+nps.getDetractorsNbr()+ "\n"+
                "Passives: "+nps.getPassivesNb()+ "\n"+
                "Promoters: "+nps.getPromotersNbr()+ "\n";
    }
}
