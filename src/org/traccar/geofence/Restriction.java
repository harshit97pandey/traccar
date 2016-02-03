package org.traccar.geofence;

import org.traccar.database.mongo.DeviceRepository;
import org.traccar.database.mongo.NotificationRepository;
import org.traccar.database.mongo.PolygonRepository;
import org.traccar.model.Polygon;
import org.traccar.model.Position;
import org.traccar.rest.PositionEventEndpoint;

import java.util.Date;
import java.util.List;

/**
 * Created by niko on 12/26/15.
 */
public class Restriction {

    private Position position;


    public Restriction(Position position) {
        this.position = position;
    }

    public void apply() {

        for (RestrictionUnit restrictionUnit : getDeviceRestrictions()) {
            checkRestriction(restrictionUnit);
        }
    }

    private List<RestrictionUnit> getDeviceRestrictions() {
        return new DeviceRepository().getDeviceRestrictions(position.getDeviceId());
    }

    private void checkRestriction(RestrictionUnit restrictionUnit) {
        Polygon polygon = new PolygonRepository().getPolygon(restrictionUnit
                .getPolygonId());
        Notification lastNotification = new NotificationRepository().getLastNotification(
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
                new NotificationRepository().markNotificationAsCanceled(lastNotification
                        .getId());
            }
        }

    }

    private void saveNotification(RestrictionUnit restrictionUnit,
            Polygon polygon) {
        new NotificationRepository().addNotification(restrictionUnit, polygon, position);

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
