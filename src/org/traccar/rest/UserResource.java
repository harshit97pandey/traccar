package org.traccar.rest;

import org.traccar.Context;
import org.traccar.database.DataManager;
import org.traccar.database.mongo.MongoDataManager;
import org.traccar.model.User;
import org.traccar.rest.utils.SessionUtil;
import org.traccar.web.JsonConverter;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by niko on 11/28/15.
 */
@Path("user")
@Produces(MediaType.APPLICATION_JSON)
public class UserResource {

    @javax.ws.rs.core.Context
    HttpServletRequest req;

    @Path("get")
    @GET
    public Response get() throws Exception {
        Context.getPermissionsManager().checkAdmin(SessionUtil.getUserId(req));

        return ResponseBuilder.getResponse(JsonConverter.arrayToJson(
                Context.getDataManager().getUsers()));
    }

    @Path("add")
    @POST
    public Response add(User user) throws Exception {
        Context.getPermissionsManager().checkUser(SessionUtil.getUserId(req), user.getId());
        Context.getDataManager().addUser(user);
        Context.getPermissionsManager().refresh();

        return ResponseBuilder.getResponse(JsonConverter.objectToJson(user));
    }

    @Path("update")
    @POST
    public Response update(User user) throws Exception {
        if (user.getAdmin()) {
            Context.getPermissionsManager().checkAdmin(SessionUtil.getUserId(req));
        } else {
            Context.getPermissionsManager().checkUser(SessionUtil.getUserId(req), user.getId());
        }
        Context.getDataManager().updateUser(user);
        Context.getPermissionsManager().refresh();

        return ResponseBuilder.getResponse(true);
    }

    @Path("remove")
    @POST
    public Response remove(User user) throws Exception {
        Context.getPermissionsManager().checkUser(SessionUtil.getUserId(req), user.getId());
        Context.getDataManager().removeUser(user);
        Context.getPermissionsManager().refresh();

        return ResponseBuilder.getResponse(true);
    }

    @Path("location/update")
    @POST
    public Response updateLocation(
            @FormParam("map") String map,
            @FormParam("zoom") int zoom,
            @FormParam("latitude") double latitude,
            @FormParam("longitude") double longitude) throws Exception {
        long userId = SessionUtil.getUserId(req);
        Context.getPermissionsManager().checkUser(SessionUtil.getUserId(req), userId);
        DataManager dataManager = Context.getDataManager();
        if (dataManager instanceof MongoDataManager) {
            MongoDataManager mongoDataManager = (MongoDataManager)dataManager;
            mongoDataManager.updateLocation(userId, map, zoom, latitude, longitude);
            return Response.ok().build();
        }
        return Response.status(Response.Status.NOT_IMPLEMENTED).build();
    }

    @Path("language/update")
    @POST
    public Response updateLocation(@FormParam("language") String language) throws Exception {
        long userId = SessionUtil.getUserId(req);
        Context.getPermissionsManager().checkUser(SessionUtil.getUserId(req), userId);
        DataManager dataManager = Context.getDataManager();
        if (dataManager instanceof MongoDataManager) {
            MongoDataManager mongoDataManager = (MongoDataManager)dataManager;
            mongoDataManager.updateLanguage(userId, language);
            return Response.ok().build();
        }
        return Response.status(Response.Status.NOT_IMPLEMENTED).build();
    }

}
