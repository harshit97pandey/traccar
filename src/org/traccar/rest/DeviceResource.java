package org.traccar.rest;

import org.traccar.Context;
import org.traccar.model.Device;
import org.traccar.rest.utils.SessionUtil;
import org.traccar.web.JsonConverter;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by niko on 11/28/15.
 */
@Path("devices")
@Produces(MediaType.APPLICATION_JSON)
public class DeviceResource {

    @javax.ws.rs.core.Context
    HttpServletRequest req;

    @GET
    public Response get(
            @QueryParam("all") boolean all,
            @QueryParam("userId") long userId) throws Exception {
        if (all) {
            Context.getPermissionsManager().checkAdmin(SessionUtil.getUserId(req));

            return Response.ok().entity(Context.getDataManager().getAllDevices()).build();
        } else {
            if (userId == 0) {
                userId = SessionUtil.getUserId(req);
            }
            Context.getPermissionsManager().checkUser(SessionUtil.getUserId(req), userId);

            return Response.ok().entity(Context.getDataManager().getDevices(userId)).build();
        }
    }

    @Path("add")
    @POST
    public Response add(Device device) throws Exception {
        long userId = SessionUtil.getUserId(req);
        Context.getDataManager().addDevice(device);
        Context.getDataManager().linkDevice(userId, device.getId());
        Context.getPermissionsManager().refresh();

        return ResponseBuilder.getResponse(JsonConverter.objectToJson(device));
    }

    @Path("update")
    @POST
    public Response update(Device device) throws Exception {
        Context.getPermissionsManager().checkDevice(SessionUtil.getUserId(req), device.getId());
        Context.getDataManager().updateDevice(device);

        return ResponseBuilder.getResponse(true);
    }

    @Path("remove")
    @POST
    public Response remove(Device device) throws Exception {
        Context.getPermissionsManager().checkDevice(SessionUtil.getUserId(req), device.getId());
        Context.getDataManager().removeDevice(device);
        Context.getPermissionsManager().refresh();

        return ResponseBuilder.getResponse(true);
    }

    @Path("link")
    @GET
    public Response link() throws Exception {
        Context.getPermissionsManager().checkAdmin(SessionUtil.getUserId(req));
        Context.getDataManager().linkDevice(
                Long.parseLong(req.getParameter("userId")),
                Long.parseLong(req.getParameter("deviceId")));
        Context.getPermissionsManager().refresh();

        return ResponseBuilder.getResponse(true);
    }

    @Path("unlink")
    @GET
    public Response unlink() throws Exception {
        Context.getPermissionsManager().checkAdmin(SessionUtil.getUserId(req));
        Context.getDataManager().unlinkDevice(
                Long.parseLong(req.getParameter("userId")),
                Long.parseLong(req.getParameter("deviceId")));
        Context.getPermissionsManager().refresh();

        return ResponseBuilder.getResponse(true);
    }
}
