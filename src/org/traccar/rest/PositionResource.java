package org.traccar.rest;

import org.traccar.Context;
import org.traccar.database.mongo.PositionRepository;
import org.traccar.model.MiscFormatter;
import org.traccar.model.Position;
import org.traccar.rest.utils.SessionUtil;
import org.traccar.web.JsonConverter;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by niko on 11/28/15.
 */
@Path("positions")
@Produces(MediaType.APPLICATION_JSON)
public class PositionResource {

    @javax.ws.rs.core.Context
    HttpServletRequest req;

    private static double x=174.7281713;

    @Path("ws")
    @GET
    public Response wsTest(
            @QueryParam("deviceId") long deviceId,
            @DefaultValue("false")@QueryParam("insert") boolean insert) throws Exception {
        Position position = new Position();
        position.setDeviceId(deviceId);

        position.setTime(new Date());
        position.setServerTime(JsonConverter.parseDate("2015-05-22T12:00:01.000Z"));
        position.setLatitude(-36.8785803);
        x=x-1.1;
        position.setLongitude(x);
        position.setSpeed(1311.234123);


        Context.getConnectionManager().updatePosition(position);
        if (insert) {
            new PositionRepository().addPosition(position);
        }
        return Response.ok().build();
    }

    @GET
    public Response get(
            @QueryParam("deviceId") long deviceId,
            @QueryParam("from") String from,
            @QueryParam("to") String to) throws Exception {

        Context.getPermissionsManager().checkDevice(SessionUtil.getUserId(req), deviceId);
        Collection<Position> positions = new PositionRepository().getPositions(
                deviceId, JsonConverter.parseDate(from), JsonConverter.parseDate(to));

        return Response.ok().entity(positions).build();
    }

    @Path("devices")
    @GET
    public Response devices() throws Exception {
        long userId = SessionUtil.getUserId(req);
        Map<String, Object> positions = new HashMap<>();

        for (String deviceIdString : req.getParameterValues("devicesId")) {
            Long deviceId = Long.parseLong(deviceIdString);

            Context.getPermissionsManager().checkDevice(userId, deviceId);

            Position position = Context.getConnectionManager().getLastPosition(deviceId);
            positions.put(deviceId.toString(), position);
        }

        return ResponseBuilder.getResponse(MiscFormatter.toJson(positions));
    }

}
