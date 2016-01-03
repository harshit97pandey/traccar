package org.traccar.rest;

import org.traccar.Context;
import org.traccar.database.DataManager;
import org.traccar.database.mongo.MongoDataManager;
import org.traccar.geofence.Notification;
import org.traccar.rest.utils.SessionUtil;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * Created by niko on 1/3/16.
 */
@Path("notifications")
@Produces(MediaType.APPLICATION_JSON)
public class NotificationResource {

    @javax.ws.rs.core.Context
    HttpServletRequest req;

    @Path("list")
    @GET
    public Response getNotifications(@QueryParam("all") boolean all) throws Exception {

        Context.getPermissionsManager().checkAdmin(SessionUtil.getUserId(req));
        DataManager dataManager = Context.getDataManager();
        if (dataManager instanceof MongoDataManager) {
            MongoDataManager mongoDataManager = (MongoDataManager)dataManager;
            List<Notification> notifications = mongoDataManager.getNotifications(all);

            return Response.ok().entity(notifications).build();
        }
        return Response.status(Response.Status.NOT_IMPLEMENTED).build();

    }

    @Path("seen")
    @POST
    public Response seen(@FormParam("notificationId") long notificationId) throws Exception {
        Context.getPermissionsManager().checkAdmin(SessionUtil.getUserId(req));
        DataManager dataManager = Context.getDataManager();
        if (dataManager instanceof MongoDataManager) {
            MongoDataManager mongoDataManager = (MongoDataManager)dataManager;
            mongoDataManager.markNotificationAsSeen(notificationId);

            return Response.ok().build();
        }
        return Response.status(Response.Status.NOT_IMPLEMENTED).build();
    }
}
