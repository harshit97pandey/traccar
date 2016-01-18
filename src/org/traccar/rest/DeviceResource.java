package org.traccar.rest;

import org.traccar.Context;
import org.traccar.model.Device;
import org.traccar.rest.utils.SessionUtil;

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


    @POST
    public Response add(Device device) throws Exception {
        Context.getPermissionsManager().checkReadonly(SessionUtil.getUserId(req));
        Context.getDataManager().addDevice(device);
        Context.getDataManager().linkDevice(SessionUtil.getUserId(req), device.getId());
        Context.getPermissionsManager().refresh();

        return Response.ok(device).build();
    }

    @Path("{id}")
    @PUT
    public Response update(@PathParam("id") long id, Device entity) throws Exception {
        Context.getPermissionsManager().checkReadonly(SessionUtil.getUserId(req));
        Context.getPermissionsManager().checkDevice(SessionUtil.getUserId(req), id);
        Context.getDataManager().updateDevice(entity);
        return Response.ok(entity).build();
    }

    @Path("{id}")
    @DELETE
    public Response remove(@PathParam("id") long id) throws Exception {
        Context.getPermissionsManager().checkReadonly(SessionUtil.getUserId(req));
        Context.getPermissionsManager().checkDevice(SessionUtil.getUserId(req), id);
        Device device = new Device();
        device.setId(id);
        Context.getDataManager().removeDevice(device);
        Context.getPermissionsManager().refresh();
        return Response.noContent().build();
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
