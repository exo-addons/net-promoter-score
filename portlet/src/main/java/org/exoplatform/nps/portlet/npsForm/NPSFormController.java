package org.exoplatform.nps.portlet.npsForm;

import juzu.*;
import juzu.bridge.portlet.JuzuPortlet;
import juzu.impl.bridge.spi.portlet.PortletRequestBridge;
import juzu.impl.common.JSON;
import juzu.impl.request.Request;
import juzu.plugin.jackson.Jackson;
import juzu.request.RequestContext;
import juzu.template.Template;
import org.chromattic.spi.type.SimpleTypeProvider;
import org.exoplatform.commons.juzu.ajax.Ajax;
import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.nps.dto.*;
import org.exoplatform.nps.services.*;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.inject.Inject;
import javax.portlet.PortletMode;
import javax.portlet.PortletPreferences;
import java.util.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@SessionScoped
public class NPSFormController {
    private static Log log = ExoLogger.getLogger(NPSFormController.class);

    // Don't use inject to not get the merge of all resource bundles
    // @Inject
    ResourceBundle bundle;

    @Inject
    NpsService npsService;

    @Inject
    NpsTypeService npsTypeService;

    @Inject
    IdentityManager identityManager;

    @Inject
    ListenerService listenerService;

    @Inject
    @Path("index.gtmpl")
    Template indexTmpl;


    @Inject
    @Path("edit.gtmpl")
    Template editTmpl;

    private String bundleString;

    private final String currentUser = ConversationState.getCurrent().getIdentity().getUserId();

    private static String RESP_COOKIES_EXP = "exo.nps.addon.respondedCookiesExpiration";
    private static String RESP_COOKIES_EXP_DEFAULT_VALUE = "30";
    private static String REPORTED_COOKIES_EXP_DEFAULT_VALUE = "10";
    private static String REPORTED_COOKIES_EXP = "exo.nps.addon.reportedCookiesExpiration";
    private static String SCORE_TYPE = "exo.nps.addon.selectedType";
    private static String FIRST_DISPLAY_DELAY = "exo.nps.addon.firstDisplayDelay";
    private static String FIRST_DISPLAY_DELAY_DEFAULT_VALUE = "10";
    private static String DISPLAY_POPUP= "exo.nps.addon.displayPopup";
    private static String DISPLAY_POPUP_DEFAULT_VALUE = "";

    private final static String MARKETO_WS_INTEGRATION_ENABLED = "exo.marketo.ws.integration.enabled";
    private final static String MARKETO_WS_INTEGRATION_CLIENT_ID = "exo.marketo.ws.integration.clientId";
    private final static String MARKETO_WS_INTEGRATION_CLIENT_SECRET = "exo.marketo.ws.integration.clientSecret";
    private final static String MARKETO_WS_INTEGRATION_BASE_URL = "exo.marketo.ws.integration.baseUrl";

    private String mktToken;
    private String mktLead;


    @View
    public Response.Content index(RequestContext requestContext) {

        PortletMode mode = requestContext.getProperty(JuzuPortlet.PORTLET_MODE);

        if (PortletMode.EDIT.equals(mode)) {
            Request request = Request.getCurrent();
            PortletRequestBridge bridge = (PortletRequestBridge) request.getBridge();
            PortletPreferences prefs = bridge.getPortletRequest().getPreferences();
            String respondedCookiesExpiration = prefs.getValue(RESP_COOKIES_EXP, RESP_COOKIES_EXP_DEFAULT_VALUE);
            String reportedCookiesExpiration = prefs.getValue(REPORTED_COOKIES_EXP, REPORTED_COOKIES_EXP_DEFAULT_VALUE);
            if (respondedCookiesExpiration == null || respondedCookiesExpiration.equals(""))
                respondedCookiesExpiration = RESP_COOKIES_EXP_DEFAULT_VALUE;
            if (reportedCookiesExpiration == null || reportedCookiesExpiration.equals(""))
                reportedCookiesExpiration = REPORTED_COOKIES_EXP_DEFAULT_VALUE;
            String selectedType = prefs.getValue(SCORE_TYPE, "");
            String firstDisplayDelay = prefs.getValue(FIRST_DISPLAY_DELAY, FIRST_DISPLAY_DELAY_DEFAULT_VALUE);
            if (firstDisplayDelay == null || firstDisplayDelay.equals(""))
                firstDisplayDelay = FIRST_DISPLAY_DELAY_DEFAULT_VALUE;
            String displayPopup = prefs.getValue(DISPLAY_POPUP, DISPLAY_POPUP_DEFAULT_VALUE);
            if (displayPopup == null)
                displayPopup = DISPLAY_POPUP_DEFAULT_VALUE;
            List<ScoreTypeDTO> scoreTypes=npsTypeService.getScoreTypes(0,0);
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("respondedCookiesExpiration", respondedCookiesExpiration);
            parameters.put("reportedCookiesExpiration", reportedCookiesExpiration);
            parameters.put("scoreTypes", scoreTypes);
            parameters.put("selectedType", selectedType);
            parameters.put("firstDisplayDelay", firstDisplayDelay);
            parameters.put("displayPopup", displayPopup);


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
        if(obj.getIsAnonymous()==null) obj.setIsAnonymous(false);
        if(obj.getIsAnonymous()){
            obj.setUserId("");
        }else obj.setUserId(currentUser);
        boolean gamified = false;
        if(obj.getGamified()!=null){
            gamified =obj.getGamified();
        }
        obj = npsService.save(obj, true);

        if(gamified) {
            try {
                Map<String, String> gam = new HashMap<>();
                gam.put("ruleTitle", "addNPSScore");
                gam.put("senderId", currentUser);
                gam.put("receiverId", currentUser);
                gam.put("object", "");
                listenerService.broadcast("exo.gamification.generic.action", gam, "");
            } catch (Exception e) {
                log.error("Cannot broadcast gamification event");
            }
            if(obj.getComment()!=null&&!"".equals(obj.getComment())){
                try {
                    Map<String, String> gam = new HashMap<>();
                    gam.put("ruleTitle", "commentedNPSScore");
                    gam.put("senderId", currentUser);
                    gam.put("receiverId", currentUser);
                    gam.put("object", "");
                    listenerService.broadcast("exo.gamification.generic.action", gam, "");
                } catch (Exception e) {
                    log.error("Cannot broadcast gamification event");
                }
            }
           if(!obj.getUserId().equals("")){
               try {
                   Map<String, String> gam = new HashMap<>();
                   gam.put("ruleTitle", "notAnonymousNPSScore");
                   gam.put("senderId", currentUser);
                   gam.put("receiverId", currentUser);
                   gam.put("object", "");
                   listenerService.broadcast("exo.gamification.generic.action", gam, "");
               } catch (Exception e) {
                   log.error("Cannot broadcast gamification event");
               }
           }

        }

        if(obj!=null && obj.getResponded()==true){
            ExoSocialActivity activity = Utils.createActivity(obj);
            if(activity!=null){
                obj.setActivityId(activity.getId());
                npsService.save(obj, false);
            }
            String mkWsIntegartionEnabled = System.getProperty(MARKETO_WS_INTEGRATION_ENABLED);
            if(mkWsIntegartionEnabled==null || !mkWsIntegartionEnabled.equals("true")){
                log.info("=== MARKETO INTEGRATION Disabled ===");
            }else{
                /**
                 * MARKETO INTEGRATION
                 */
                String result = null;
                /**/
                /* SET LEAD REVIEW */
                /**/
                log.info("================ MARKETO INTEGRATION BEGIN =============================");

                try {
                    String mkWsIntegartionBaseUrl = System.getProperty(MARKETO_WS_INTEGRATION_BASE_URL);
                    URL url = new URL(mkWsIntegartionBaseUrl+"/rest/v1/leads.json?access_token=" + mktToken);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setDoOutput(true);
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");

                    /* JSON INPUT*/
                    try {

                        JSONArray inputArray = new JSONArray();
                        JSONObject inputObject = new JSONObject();

                        inputObject.put("id", mktLead);
                        inputObject.put("NPS_Note__c", obj.getScore());

                        inputArray.put(inputObject);

                        /* END JSON INPUT*/

                        /* JSON OBJECT FOR POST CALL*/
                        JSONObject requestBody = new JSONObject();

                        requestBody.put("action", "updateOnly");
                        requestBody.put("lookupField", "id");
                        requestBody.put("input", inputArray);

                        /* END JSON OBJECT FOR POST CALL*/

                        OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                        wr.write(requestBody.toString());
                        wr.flush();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    int responseCode = conn.getResponseCode();
                    if (responseCode == 200) {
                        InputStream inStream = conn.getInputStream();
                        result = convertStreamToString(inStream);
                    } else {
                        result = "Status Code: " + responseCode;
                    }

                    conn.disconnect();
                    log.info("================ MARKETO INTEGRATION Done with "+result+" =============================");
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    javax.ws.rs.core.Response.status(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR).entity(result).build();
                } catch (IOException e) {
                    e.printStackTrace();
                    javax.ws.rs.core.Response.status(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR).entity(result).build();
                }

            }
        }
    }

    /**
     * TO GET MARKETO ACCESS TOKEN
     *
     * @return token
     */
    public String getToken() {
        String mkWsIntegartionEnabled = System.getProperty(MARKETO_WS_INTEGRATION_ENABLED);
        if(mkWsIntegartionEnabled==null || !mkWsIntegartionEnabled.equals("true")){
            return  "";
        }
        // GET MARKETO ACCESS TOKEN
        String mkWsIntegartionClientId = System.getProperty(MARKETO_WS_INTEGRATION_CLIENT_ID);
        String mkWsIntegartionClientSecret = System.getProperty(MARKETO_WS_INTEGRATION_CLIENT_SECRET);
        String[] token = new String[5];
        String output = null;
        String mkWsIntegartionBaseUrl = System.getProperty(MARKETO_WS_INTEGRATION_BASE_URL);
        try {
            URL url = new URL(mkWsIntegartionBaseUrl+"/identity/oauth/token?client_id="+mkWsIntegartionClientId+"&client_secret="+mkWsIntegartionClientSecret+"&grant_type=client_credentials");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + conn.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));


            while ((output = br.readLine()) != null) {
                token = output.split("\"");
            }
            conn.disconnect();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return token[3];
    }


    public String getMktLead(String cookie) {
        String mkWsIntegartionEnabled = System.getProperty(MARKETO_WS_INTEGRATION_ENABLED);
        if(mkWsIntegartionEnabled==null || !mkWsIntegartionEnabled.equals("true")){
            return  "";
        }
        // GET MARKETO ACCESS TOKEN
        String output = null;
        String[] leadID = new String[5];
        String mkWsIntegartionBaseUrl = System.getProperty(MARKETO_WS_INTEGRATION_BASE_URL);

        try {
            URL url = new URL(mkWsIntegartionBaseUrl+"/rest/v1/leads.json?access_token=" + mktToken + "&filterType=cookie&filterValues=" + cookie + "&fields=id,email");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + conn.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));


                while ((output = br.readLine()) != null) {
//                    System.out.println(output);
                    leadID = output.split("\"");
                }

                conn.disconnect();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return leadID[8].substring(1, ((leadID[8].length())-1));
        }

    private String convertStreamToString(InputStream inputStream) {
        try {
            return new Scanner(inputStream).useDelimiter("\\A").next();
        } catch (NoSuchElementException e) {
            return "";
        }
    }

    @Ajax
    @juzu.Resource
    @MimeType.JSON
    @Jackson
    public Response getContext(String mktCookie) {
        try {
            Request request = Request.getCurrent();
            PortletRequestBridge bridge = (PortletRequestBridge) request.getBridge();
            String portletId = bridge.getWindowContext().getId();
            PortletPreferences prefs = bridge.getPortletRequest().getPreferences();
            String respondedCookiesExpiration = prefs.getValue(RESP_COOKIES_EXP, RESP_COOKIES_EXP_DEFAULT_VALUE);
            String reportedCookiesExpiration = prefs.getValue(REPORTED_COOKIES_EXP, REPORTED_COOKIES_EXP_DEFAULT_VALUE);
            String scoreTypeId = prefs.getValue(SCORE_TYPE, "");
            String firstDisplayDelay = prefs.getValue(FIRST_DISPLAY_DELAY, FIRST_DISPLAY_DELAY_DEFAULT_VALUE);
            if (respondedCookiesExpiration == null || respondedCookiesExpiration.equals(""))
                respondedCookiesExpiration = RESP_COOKIES_EXP_DEFAULT_VALUE;
            if (reportedCookiesExpiration == null || reportedCookiesExpiration.equals(""))
                reportedCookiesExpiration = REPORTED_COOKIES_EXP_DEFAULT_VALUE;
            if (firstDisplayDelay == null || firstDisplayDelay.equals(""))
                firstDisplayDelay = FIRST_DISPLAY_DELAY_DEFAULT_VALUE;
            String displayPopup = prefs.getValue(DISPLAY_POPUP, DISPLAY_POPUP_DEFAULT_VALUE);

            if (displayPopup == null)
                displayPopup = FIRST_DISPLAY_DELAY_DEFAULT_VALUE;
/*            if (!PropertyManager.isDevelopping() && bundleString != null && getResourceBundle().getLocale().equals(PortalRequestContext.getCurrentInstance().getLocale())) {
                return Response.ok(bundleString);
            }*/
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
            Profile profile = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, currentUser, false).getProfile();

            ScoreTypeDTO  sType = npsTypeService.getScoreType(Long.parseLong(scoreTypeId));

            data.set("currentUser", currentUser);
            data.set("fullName", profile.getFullName());
            data.set("respondedCookiesExpiration", respondedCookiesExpiration);
            data.set("reportedCookiesExpiration", reportedCookiesExpiration);
            data.set("scoreTypeId", scoreTypeId);
            data.set("scoreTypeName", sType.getTypeName());
            data.set("portletId", portletId);
            data.set("scoreTypeMessage", sType.getQuestion());
            data.set("followUpPassive", sType.getFollowUpPassive());
            data.set("followUpPromoter", sType.getFollowUpPromoter());
            data.set("followUpDetractor", sType.getFollowUpDetractor());
            data.set("gamified", sType.getGamified());
            if(sType.getAnonymous()==null){
                data.set("scoreTypeAnonymous", false);
            }else{
                data.set("scoreTypeAnonymous", sType.getAnonymous());
            }
            data.set("firstDisplayDelay", firstDisplayDelay);
            data.set("displayPopup", displayPopup);
            data.set("firstLogDiff", Utils.getDiffinDays(Utils.getFirstLoginDate(currentUser),Calendar.getInstance().getTimeInMillis()));
            bundleString = data.toString();

            String mkWsIntegartionEnabled = System.getProperty(MARKETO_WS_INTEGRATION_ENABLED);
            if(mkWsIntegartionEnabled!=null && mkWsIntegartionEnabled.equals("true")) {
                mktToken = getToken();
                mktLead = getMktLead(mktCookie);
            }
            return Response.ok(bundleString);
        } catch (Exception e) {
            log.error("error while getting context", e);
            return Response.status(500);
        }
    }

    @Action
    @Route("updateSettings")
    public Response.Content updateSettings(String respondedCookiesExpiration, String reportedCookiesExpiration,String typeId, String firstDisplayDelay , String displayPopup) throws Exception {
        Request request = Request.getCurrent();
        PortletRequestBridge bridge = (PortletRequestBridge) request.getBridge();
        PortletPreferences prefs = bridge.getPortletRequest().getPreferences();
        prefs.setValue(RESP_COOKIES_EXP, respondedCookiesExpiration);
        prefs.setValue(REPORTED_COOKIES_EXP, reportedCookiesExpiration);
        prefs.setValue(SCORE_TYPE, typeId);
        prefs.setValue(FIRST_DISPLAY_DELAY, firstDisplayDelay);
        prefs.setValue(DISPLAY_POPUP, displayPopup);
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
