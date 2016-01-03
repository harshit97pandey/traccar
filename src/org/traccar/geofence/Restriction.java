package org.traccar.geofence;

import org.traccar.Context;
import org.traccar.database.mongo.MongoDataManager;
import org.traccar.model.Polygon;
import org.traccar.model.Position;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by niko on 12/26/15.
 */
public class Restriction {

    private Position position;

    public Restriction(Position position) {
        this.position = position;
    }

    public void apply() throws SQLException {

        MongoDataManager mongoDataManager = (MongoDataManager)Context.getDataManager();
        for (RestrictionUnit restrictionUnit : mongoDataManager.getDeviceRestrictions(position.getDeviceId())) {
            Polygon polygon = mongoDataManager.getPolygon(restrictionUnit.getPolygonId());
            Boolean check = restrictionUnit.check(polygon, position);
            if (!check) {
                mongoDataManager.addNotification(restrictionUnit, polygon, position);
            }
        }
    }

    private List<RestrictionUnit> getDeviceRestrictions() {
        return new ArrayList<>();
        //TODO return restrictions
    }

    private void checkConditions() {

    }
}
