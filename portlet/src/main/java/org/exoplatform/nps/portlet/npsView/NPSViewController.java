package org.exoplatform.nps.portlet.npsView;

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
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.ConversationState;
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
public class NPSViewController {
    private static Log log = ExoLogger.getLogger(NPSViewController.class);

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
    @Path("index.gtmpl")
    Template indexTmpl;


    @Inject
    @Path("edit.gtmpl")
    Template editTmpl;

    private String bundleString;

    private final String currentUser = ConversationState.getCurrent().getIdentity().getUserId();

    private static String SCORE_TYPE = "exo.nps.addon.selectedType";


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
            log.error("error while getting categories", e);
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

            try {
                ScoreTypeDTO  sType = npsTypeService.getScoreType(Long.parseLong(scoreTypeId));
                long typeId=sType.getId();
                long scorsnbr= npsService.getScoreCount(typeId, true);
                long detractorsNbr= npsService.getDetractorsCount(typeId, true);
                long promotersNbr= npsService.getPromotersCount(typeId,true);
                long passivesNbr= scorsnbr-(promotersNbr+detractorsNbr);


                float detractorsPrc=((float)detractorsNbr/(float)scorsnbr)*100;
                float promotersPrc=((float)promotersNbr/(float)scorsnbr)*100;
                float passivesPrc=((float)passivesNbr/(float)scorsnbr)*100;

                float npScore= promotersPrc-detractorsPrc;

                float dashoffset = 300-(3*npScore);

                data.set("scorsnbr",npsService.getScoreCount(typeId,false));
                data.set("detractorsNbr",detractorsNbr);
                data.set("promotersNbr",promotersNbr);
                data.set("passivesNbr",passivesNbr);

                data.set("detractorsPrc",String.format("%.2f", detractorsPrc));
                data.set("promotersPrc",String.format("%.2f", promotersPrc));
                data.set("passivesPrc",String.format("%.2f", passivesPrc));
                data.set("npScore",String.format("%.2f", npScore));
                data.set("dashoffset",String.format("%.2f", dashoffset));
                data.set("scoreTypeName", sType.getTypeName());
            } catch (NumberFormatException e) {
                log.warn("Cannot get the NPS type");
                data.set("npScore",0);
            }

            return Response.ok(data.toString());
        } catch (Throwable e) {
            log.error("error while getting context", e);
            return Response.status(500);
        }
    }



    @Action
    @Route("updateSettings")
    public Response.Content updateSettings(String typeId) throws Exception {
        Request request = Request.getCurrent();
        PortletRequestBridge bridge = (PortletRequestBridge) request.getBridge();
        PortletPreferences prefs = bridge.getPortletRequest().getPreferences();
        prefs.setValue(SCORE_TYPE, typeId);
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
