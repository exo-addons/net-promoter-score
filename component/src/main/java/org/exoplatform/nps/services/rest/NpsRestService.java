package org.exoplatform.nps.services.rest;


import org.exoplatform.commons.utils.ListAccess;

import org.exoplatform.nps.dto.NPSDetailsDTO;
import org.exoplatform.nps.services.Utils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.service.rest.RestChecker;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Medamine on 05/01/2017.
 */

@Path("/nps")
@Produces("application/json")
public class NpsRestService implements ResourceContainer {

    private static final Log LOG = ExoLogger.getLogger(NpsRestService.class);
    private static final String[] SUPPORTED_FORMATS = new String[]{"json"};
    private IdentityManager identityManager;
    private SpaceService spaceService;
    private OrganizationService organizationService;


    public NpsRestService(IdentityManager identityManager, SpaceService spaceService, OrganizationService organizationService) {
        this.identityManager=identityManager;
        this.spaceService=spaceService;
        this.organizationService=organizationService;
    }


    @GET
    @Path("users/find")
    @RolesAllowed("users")
    @Consumes({MediaType.APPLICATION_JSON})
    public Response find(@Context HttpServletRequest request,
                         @Context UriInfo uriInfo,
                         @QueryParam("nameToSearch") String nameToSearch,
                         @QueryParam("spaceURL") String spaceURL,
                         @QueryParam("currentUser") String currentUser) throws Exception {

        MediaType mediaType = RestChecker.checkSupportedFormat("json", SUPPORTED_FORMATS);
        try {
            JSONArray users = new JSONArray();

            Space space = spaceService.getSpaceByUrl(spaceURL);
            if(space!=null){
                List<Profile> profiles = getSpaceMembersProfiles(space).stream()
                        .filter(a -> a.getFullName().toLowerCase().contains(nameToSearch.toLowerCase()))
                        .collect(Collectors.toList());
                if (profiles != null && profiles.size() > 0) {
                    for (Profile profile : profiles) {
                        JSONObject user = new JSONObject();
                        user.put("value",profile.getIdentity().getRemoteId());
                        user.put("type","user");
                        user.put("invalid",false);
                        user.put("order","1");
                        if(profile.getAvatarUrl()!=null){
                            user.put("avatarUrl",profile.getAvatarUrl());
                        }else{
                            user.put("avatarUrl","/eXoSkin/skin/images/system/UserAvtDefault.png");
                        }
                        user.put("text",profile.getFullName() + " (" + profile.getIdentity().getRemoteId() + ")");
                        users.put(user);
                    }
                }
            }


            JSONObject jsonGlobal = new JSONObject();
            jsonGlobal.put("options",users);
            return Response.ok(jsonGlobal.toString(), mediaType).build();
        } catch (Exception e) {
            LOG.error(e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("An internal error has occured").build();
        }
    }


    @GET
    @Path("spaces/find")
    @RolesAllowed("users")
    @Consumes({MediaType.APPLICATION_JSON})
    public Response findspace(@Context HttpServletRequest request,
                         @Context UriInfo uriInfo,
                         @QueryParam("nameToSearch") String nameToSearch,
                         @QueryParam("currentUser") String currentUser) throws Exception {

        MediaType mediaType = RestChecker.checkSupportedFormat("json", SUPPORTED_FORMATS);
        try {
            JSONArray spaces = new JSONArray();

            ListAccess<Space> spacesListAcc = spaceService.getMemberSpaces(currentUser);
            if(spacesListAcc.getSize()>0){
            List<Space> spacesList = Arrays.asList(spacesListAcc.load(0,spacesListAcc.getSize()));

                List<Space> spaces_ = spacesList.stream()
                        .filter(a -> a.getDisplayName().toLowerCase().contains(nameToSearch.toLowerCase()))
                        .collect(Collectors.toList());
                if (spaces_ != null && spaces_.size() > 0) {
                    for (Space space_ : spaces_) {
                        JSONObject space = new JSONObject();
                        space.put("value",space_.getDisplayName());
                        space.put("type","space");
                        space.put("invalid",false);
                        space.put("order","1");
                        if(space_.getAvatarUrl()!=null){
                            space.put("avatarUrl",space_.getAvatarUrl());
                        }else{
                            space.put("avatarUrl","/eXoSkin/skin/images/system/SpaceAvtDefault.png");
                        }
                        space.put("text",space_.getDisplayName() + " (" + space_.getPrettyName() + ")");
                        spaces.put(space);
                    }
                }
            }


            JSONObject jsonGlobal = new JSONObject();
            jsonGlobal.put("options",spaces);
            return Response.ok(jsonGlobal.toString(), mediaType).build();
        } catch (Exception e) {
            LOG.error(e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("An internal error has occured").build();
        }
    }

    private List<Profile> getSpaceMembersProfiles(Space space){
        List<Profile> profiles=new ArrayList<Profile>();
        for(String userId : space.getMembers()){
            Profile userProfile = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, userId, false).getProfile();
            profiles.add(userProfile);
        }
        return profiles;

    }


    @GET
    @Path("scores/weekly")
    @Consumes({MediaType.APPLICATION_JSON})
    public Response findspace(@Context HttpServletRequest request,
                              @Context UriInfo uriInfo,
                              @QueryParam("npsTypeId") long npsTypeId,
                              @QueryParam("fromDate") long fromDate,
                              @QueryParam("toDate") long toDate) throws Exception {

        MediaType mediaType = RestChecker.checkSupportedFormat("json", SUPPORTED_FORMATS);
        SimpleDateFormat dt1 = new SimpleDateFormat("yyyy-MM-dd");

        try {
            JSONArray npsList = new JSONArray();

            List <NPSDetailsDTO> npsDetails =Utils.getWeeklyNPSbyDates(npsTypeId, fromDate, toDate);

            for(NPSDetailsDTO nps : npsDetails){
                JSONObject nps_ = new JSONObject();
                nps_.put("npsDate",dt1.format(nps.getNpsDate()));
                nps_.put("score",String.format("%.2f", nps.getNpScore()));
                npsList.put(nps_);
            }

            return Response.ok(npsList.toString(), mediaType).build();
        } catch (Exception e) {
            LOG.error(e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("An internal error has occured").build();
        }
    }

}
