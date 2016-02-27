package org.traccar.rest;

import org.traccar.Context;
import org.traccar.database.mongo.PolygonRepository;
import org.traccar.model.Polygon;
import org.traccar.model.User;
import org.traccar.rest.utils.PolygonUtil;
import org.traccar.rest.utils.SessionUtil;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by niko on 12/19/15.
 */
@Path("/polygon")
@Produces(MediaType.APPLICATION_JSON)
public class PolygonResource {


    @javax.ws.rs.core.Context
    HttpServletRequest req;

    @Path("add")
    @POST
    public Response add(Polygon polygon) throws Exception {
        Context.getPermissionsManager().checkAdmin(SessionUtil.getUserId(req));

        //set company
        User user = SessionUtil.getUser(req);
        polygon.setCompany(user.getCompany());

        new PolygonRepository().addPolygon(polygon);

        return Response.ok().entity(polygon).build();
    }

    @Path("update")
    @POST
    public Response update(Polygon polygon) throws Exception {
        Context.getPermissionsManager().checkAdmin(SessionUtil.getUserId(req));
        new PolygonRepository().updatePolygon(polygon);

        return Response.ok().entity(polygon).build();
    }

    @Path("remove")
    @POST
    public Response remove(Polygon polygon) throws Exception {
        Context.getPermissionsManager().checkAdmin(SessionUtil.getUserId(req));
        new PolygonRepository().removePolygon(polygon.getId());

        return ResponseBuilder.getResponse(true);
    }

    @Path("get")
    @GET
    public Response get(@QueryParam("polygonId") long polygonId) throws Exception {
        Context.getPermissionsManager().checkAdmin(SessionUtil.getUserId(req));

        return Response.ok().entity(new PolygonRepository().getPolygon(polygonId)).build();
    }

    @Path("list")
    @GET
    public Response list() throws Exception {
        //TODO permission
        //Context.getPermissionsManager().checkAdmin(SessionUtil.getUserId(req));
        User user = SessionUtil.getUser(req);

        return Response.ok().entity(new PolygonRepository().getPolygons(user)).build();
    }

    @Path("contains")
    @GET
    public Response contains(@QueryParam("polygonId")long polygonId,
                             @QueryParam("latitude") Double latitude,
                             @QueryParam("longitude") Double longitude) throws Exception {
        Context.getPermissionsManager().checkAdmin(SessionUtil.getUserId(req));
        Boolean contains = PolygonUtil.contains(polygonId, latitude, longitude);

        return Response.ok().entity(contains).build();
    }

    @Path("link")
    @POST
    public Response linkPolygon(
            @FormParam("polygonId") long polygonId,
            @FormParam("deviceId") long deviceId) throws Exception {
        Context.getPermissionsManager().checkAdmin(SessionUtil.getUserId(req));
        new PolygonRepository().linkPolygon(polygonId, deviceId);

        return Response.ok().build();
    }

    @Path("unlink")
    @POST
    public Response unlinkPolygon(
            @FormParam("polygonId") long polygonId,
            @FormParam("deviceId") long deviceId) throws Exception {
        Context.getPermissionsManager().checkAdmin(SessionUtil.getUserId(req));
        new PolygonRepository().unlinkPolygon(polygonId, deviceId);

        return Response.ok().build();
    }
}
