package org.traccar.geofence;

import org.traccar.Context;
import org.traccar.database.mongo.MongoDataManager;
import org.traccar.model.Polygon;
import org.traccar.model.Position;
import org.traccar.rest.PositionEventEndpoint;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * Created by niko on 12/26/15.
 */
public class Restriction {

    private Position position;

    private MongoDataManager mongoDataManager;

    public Restriction(Position position) {
        this.position = position;
        mongoDataManager = (MongoDataManager) Context.getDataManager();
    }

    public void apply() throws SQLException {

        for (RestrictionUnit restrictionUnit : getDeviceRestrictions()) {
            checkRestriction(restrictionUnit);
        }
    }

    private List<RestrictionUnit> getDeviceRestrictions() {
        return mongoDataManager.getDeviceRestrictions(position.getDeviceId());
    }

    private void checkRestriction(RestrictionUnit restrictionUnit)
            throws SQLException {
        Polygon polygon = mongoDataManager.getPolygon(restrictionUnit
                .getPolygonId());
        Notification lastNotification = mongoDataManager.getLastNotification(
                restrictionUnit, position);
        if (polygon != null) {
            Boolean check = restrictionUnit.check(polygon, position);
            if (lastNotification == null) {
                if (!check) {
                    saveNotification(restrictionUnit, polygon);
                }
            } else if (lastNotification.isCanceled() && !check) {
                saveNotification(restrictionUnit, polygon);
            } else if (!lastNotification.isCanceled() && check) {
                mongoDataManager.markNotificationAsCanceled(lastNotification
                        .getId());
            }
        }

    }

    private void saveNotification(RestrictionUnit restrictionUnit,
            Polygon polygon) throws SQLException {
        mongoDataManager.addNotification(restrictionUnit, polygon, position);

        Notification notification = new Notification();
        notification.setCreationDate(new Date());
        notification.setPolygonId(polygon.getId());
        notification.setPolygonName(polygon.getName());
        notification.setSeen(false);
        notification.setDeviceId(position.getDeviceId());

        notification.setRestrictionUnit(restrictionUnit);
        notification.setPositionId(position.getId());

        Alert alert = new Alert("OUT_OF_AREA", notification);
        Message message = new Message("alert", alert);
        PositionEventEndpoint.showAlert(message);
    }
}
