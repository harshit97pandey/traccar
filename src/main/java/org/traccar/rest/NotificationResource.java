package org.traccar.rest;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.traccar.Context;
import org.traccar.database.mongo.NotificationRepository;
import org.traccar.geofence.Notification;
import org.traccar.rest.utils.SessionUtil;

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
        //TODO rights
        //Context.getPermissionsManager().checkAdmin(SessionUtil.getUserId(req));
        List<Notification> notifications = new NotificationRepository().getNotifications(all);

        return Response.ok().entity(notifications).build();

    }

    @Path("seen")
    @POST
    public Response seen(@FormParam("notificationId") long notificationId) throws Exception {
        Context.getPermissionsManager().checkAdmin(SessionUtil.getUserId(req));
        new NotificationRepository().markNotificationAsSeen(notificationId);

        return Response.ok().build();
    }
}
