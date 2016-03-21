package org.traccar.rest;

import org.traccar.database.mongo.RestrictionRepository;
import org.traccar.geofence.restrictions.*;
import org.traccar.model.User;
import org.traccar.rest.utils.SessionUtil;

import javax.json.*;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.function.Function;

/**
 * Created by niko on 3/19/16.
 */
@Path("restriction")
@Produces(MediaType.APPLICATION_JSON)
public class RestrictionResource {

    @javax.ws.rs.core.Context
    HttpServletRequest req;

    @GET
    public Response get() {
        User user = SessionUtil.getUser(req);
        return Response
                .ok()
                .entity(new RestrictionRepository().getRestrictions(user.getCompany()))
                .build();
    }

    @POST
    public Response add(String restrictionJSONData) {

        RestrictionUnion restrictionUnion = new RestrictionUnion();
        User user = SessionUtil.getUser(req);

        JsonReader reader = Json.createReader(new StringReader(restrictionJSONData));

        JsonObject jsonObject = reader.readObject();

        if (jsonObject.containsKey("enabled")) {
            restrictionUnion.setEnabled(jsonObject.getBoolean("enabled"));
        }
        restrictionUnion.setCompanyName(user.getCompany());

        JsonArray units = jsonObject.getJsonArray("units");
        LinkedList<RestrictionUnit> u = new LinkedList<>();
        for (int i = 0; i < units.size(); i++) {
            JsonObject unit = units.getJsonObject(i);
            int restrictionType = unit.getInt("restrictionType");
            RestrictionUnit restrictionUnit = getFromJson(restrictionType, unit);
            u.add(restrictionUnit);
        }
        restrictionUnion.setUnits(u);
        return Response
                .ok()
                .entity(new RestrictionRepository().add(restrictionUnion))
                .build();
    }

    @POST
    @Path("applyToDevice")
    public Response applyToDevice(@FormParam("deviceId") long deviceId,
                                  @FormParam("restrictionUnionId") String restrictionUnionId) {

        return Response
                .ok()
                .entity(new RestrictionRepository().applyToDevice(deviceId, restrictionUnionId))
                .build();
    }

    @POST
    @Path("ignoreFromDevice")
    public Response ignoreFromDevice(@FormParam("deviceId") long deviceId,
                                  @FormParam("restrictionUnionId") String restrictionUnionId) {

        return Response
                .ok()
                .entity(new RestrictionRepository().ignoreFromDevice(deviceId, restrictionUnionId))
                .build();
    }

    @GET
    @Path("deviceRestrictions")
    public Response getDeviceRestrictions(@QueryParam("deviceId") long deviceId) {
        return Response
                .ok()
                .entity(new RestrictionRepository().getDeviceRestrictions(deviceId))
                .build();
    }

    private static RestrictionUnit getFromJson(Integer restrictionType, JsonObject jsonObject) {

        //lambdas for extract value from json
        Function<JsonObject, Integer> polygonIdSupplier = (p) -> p.getInt("polygonId");
        Function<JsonObject, Boolean> chainConditionSupplier = (p) -> {
            if (p.containsKey("chainCondition")) {
                return p.getBoolean("chainCondition");
            }else {
                return false;
            }
        };

        switch (restrictionType) {
            case 1:
                IntoAreaRestriction intoAreaRestriction = new IntoAreaRestriction();
                intoAreaRestriction.setPolygonId(polygonIdSupplier.apply(jsonObject));
                intoAreaRestriction.restrictionType = restrictionType;
                intoAreaRestriction.chainCondition = chainConditionSupplier.apply(jsonObject);
            case 2:
                OutOfAreaRestriction r1 = new OutOfAreaRestriction();
                r1.restrictionType = restrictionType;
                r1.chainCondition = chainConditionSupplier.apply(jsonObject);
                r1.setPolygonId(polygonIdSupplier.apply(jsonObject));
                return r1;
            case 3:
                SpeedRestriction sr = new SpeedRestriction();
                JsonNumber speedLimit = jsonObject.getJsonNumber("speedLimit");
                sr.setSpeedLimit(speedLimit.doubleValue());
                sr.restrictionType = restrictionType;
                sr.chainCondition = chainConditionSupplier.apply(jsonObject);
                return sr;
            case 4:
                DistanceRestriction dr = new DistanceRestriction();
                JsonNumber distanceLimit = jsonObject.getJsonNumber("distanceLimit");
                dr.setDistanceLimit(distanceLimit.doubleValue());
                dr.restrictionType = restrictionType;
                dr.chainCondition = chainConditionSupplier.apply(jsonObject);
                return dr;
            default:
                return null;
        }
    }
}
