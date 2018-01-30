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
                Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getPrettyName(), false);
                Identity userIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, scoreType.getUserId(), false);
                String posterName = "Anonymous";
                if(score.getUserId()!=null){
                    Identity posterIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, score.getUserId(), false);
                    posterName =userIdentity.getProfile().getFullName();
                }

                ExoSocialActivity activity = new ExoSocialActivityImpl();
                activity.setType("DEFAULT_ACTIVITY");
                activity.setTitle("<span id='npsActivity'>\n" +
                        "A new response has been added to the "+scoreType.getTypeName()+" survey: <br/>\n" +
                        " <b>User Name : </b>"+posterName+"<br/>\n" +
                        " <b>Score : </b>"+score.getScore()+"<br/>\n" +
                        " <b>Comment: </b>"+score.getComment()+ "<br/>");
                activity.setUserId(userIdentity.getId());
                activityManager.saveActivityNoReturn(spaceIdentity, activity);
            }
        }

    }

    public static NPSDetailsDTO calculateNpsByDate (long typeId,long date){
        NpsService npsService= CommonsUtils.getService(NpsService.class);
        long scorsnbr= npsService.getScoreCountByDate(typeId, date);
        long detractorsNbr= npsService.getDetractorsCountByDate(typeId, date);
        long promotersNbr= npsService.getPromotersCountByDate(typeId, date);
        long passivesNbr= scorsnbr-(promotersNbr+detractorsNbr);
        return new NPSDetailsDTO(typeId, date, scorsnbr, detractorsNbr,  promotersNbr, passivesNbr);
    }


    public static List<NPSDetailsDTO> getWeeklyNPS(long typeId,long fromDate, long toDate){
        List<NPSDetailsDTO> NPSScors=new ArrayList<NPSDetailsDTO>();
        Calendar c=Calendar.getInstance();
        c.setTimeInMillis(fromDate);
        int diff= Calendar.SATURDAY-c.get(Calendar.DAY_OF_WEEK);
        c.add(Calendar.DATE, diff);
        while(c.getTimeInMillis()<toDate){
            NPSScors.add(calculateNpsByDate (typeId,c.getTimeInMillis()));
            c.add(Calendar.DATE, 7);
        }
        if(c.getTimeInMillis()!=toDate){
            NPSScors.add(calculateNpsByDate (typeId,toDate));
        }
      return NPSScors;

    }

    public static List<NPSDetailsDTO> getWeeklyNPSForCurrentYear(long typeId){
        NpsService npsService= CommonsUtils.getService(NpsService.class);
        List<NPSDetailsDTO> NPSScors=new ArrayList<NPSDetailsDTO>();
        ScoreEntryDTO score = npsService.getFirstScoreEntries(typeId);
        if(score!=null){
            Calendar fromDate=Calendar.getInstance();
            fromDate.set(Calendar.DAY_OF_YEAR, 1);
            fromDate.set(Calendar.YEAR, 2017);
            Calendar c=Calendar.getInstance();
            c.setTimeInMillis(score.getPostedTime());
            if(fromDate.before(c)) fromDate.setTime(c.getTime());
            int diff= Calendar.SATURDAY-fromDate.get(Calendar.DAY_OF_WEEK);
            fromDate.add(Calendar.DATE, diff);
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

}
