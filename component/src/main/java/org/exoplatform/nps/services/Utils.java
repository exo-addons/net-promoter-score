package org.exoplatform.nps.services;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import javax.jcr.Node;
import javax.jcr.Session;
import java.util.Calendar;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
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

        RepositoryService repositoryService = (RepositoryService) PortalContainer.getInstance().getComponentInstanceOfType(RepositoryService.class);
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

}
