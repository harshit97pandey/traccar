package org.traccar.geofence;

import org.traccar.database.mongo.NotificationRepository;
import org.traccar.database.mongo.RestrictionRepository;
import org.traccar.model.Position;
import org.traccar.webSocket.PositionEventEndpoint;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Created by niko on 12/26/15.
 */
public class Restriction {

    private Position position;


    public Restriction(Position position) {
        this.position = position;
    }

    public void apply() {
        getDeviceRestrictions().forEach(ru -> checkRestriction(ru));
    }

    private List<RestrictionUnion> getDeviceRestrictions() {
        return new RestrictionRepository().getDeviceRestrictions(position.getDeviceId());
    }

    private void checkRestriction(RestrictionUnion restrictionUnion) {
        Optional<Notification> lastNotification = new NotificationRepository().getLastNotification(
                restrictionUnion, position);


        Boolean check = restrictionUnion.test();

        if ( ! lastNotification.isPresent()) {
            if (!check) {
                saveNotification(restrictionUnion);
            }
        } else if (lastNotification.get().isCanceled() && !check) {
            saveNotification(restrictionUnion);
        } else if (!lastNotification.get().isCanceled() && check) {
            new NotificationRepository().markNotificationAsCanceled(lastNotification.get()
                    .getId());
        }

    }

    private void saveNotification(RestrictionUnion restrictionUnion) {
        new NotificationRepository().addNotification(restrictionUnion, position);

        Notification notification = new Notification();
        notification.setCreationDate(new Date());
        notification.setSeen(false);
        notification.setDeviceId(position.getDeviceId());

        notification.setRestrictionUnits(restrictionUnion.getUnits());
        notification.setPositionId(position.getId());

        Alert alert = new Alert("OUT_OF_AREA", notification);
        Message message = new Message("alert", alert);
        PositionEventEndpoint.showAlert(message);
    }
}
