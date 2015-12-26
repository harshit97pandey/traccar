package org.traccar.rest;

import org.traccar.Context;
import org.traccar.database.DataManager;
import org.traccar.database.mongo.MongoDataManager;
import org.traccar.model.Point;
import org.traccar.model.Polygon;
import org.traccar.rest.utils.PolygonUtil;
import org.traccar.rest.utils.SessionUtil;
import org.traccar.web.JsonConverter;

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

    @Path("list")
    @GET
    public Response list() throws Exception {
        Context.getPermissionsManager().checkAdmin(SessionUtil.getUserId(req));

        DataManager dataManager = Context.getDataManager();
        if (dataManager instanceof MongoDataManager) {
            MongoDataManager mongoDataManager = (MongoDataManager)dataManager;

            return Response.ok().entity(mongoDataManager.getPolygons()).build();
        }

        return Response.status(Response.Status.NOT_IMPLEMENTED).build();
    }

    @Path("add")
    @POST
    public Response add(Polygon polygon) throws Exception {
        Context.getPermissionsManager().checkAdmin(SessionUtil.getUserId(req));
        DataManager dataManager = Context.getDataManager();
        if (dataManager instanceof MongoDataManager) {
            MongoDataManager mongoDataManager = (MongoDataManager)dataManager;
            mongoDataManager.addPolygon(polygon);
            return ResponseBuilder.getResponse(true);
        }
        return Response.status(Response.Status.NOT_IMPLEMENTED).build();
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
}
