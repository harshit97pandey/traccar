package org.traccar.rest;

import com.fasterxml.jackson.annotation.JsonValue;
import com.sun.xml.internal.ws.wsdl.writer.document.StartWithExtensionsType;
import org.traccar.database.mongo.RestrictionRepository;
import org.traccar.geofence.IntoAreaRestriction;
import org.traccar.geofence.RestrictionUnion;
import org.traccar.geofence.RestrictionUnit;
import org.traccar.model.User;
import org.traccar.rest.utils.SessionUtil;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.StringReader;
import java.util.LinkedList;

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
    public Response put(String restrictionJSONData) {

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

    private static RestrictionUnit getFromJson(Integer restrictionType, JsonObject jsonObject) {
        switch (restrictionType) {
            case 1:
                IntoAreaRestriction intoAreaRestriction = new IntoAreaRestriction();
                intoAreaRestriction.setPolygonId(jsonObject.getInt("polygonId"));
                intoAreaRestriction.restrictionType = restrictionType;
                if (jsonObject.containsKey("chainCondition")) {
                    intoAreaRestriction.chainCondition = jsonObject.getBoolean("chainCondition");
                }
                return intoAreaRestriction;
            default:
                return null;
        }
    }
}
