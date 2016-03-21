package org.traccar.database.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import org.bson.Document;
import org.traccar.geofence.Notification;
import org.traccar.geofence.RestrictionUnion;
import org.traccar.geofence.RestrictionUnit;
import org.traccar.model.Device;
import org.traccar.model.Polygon;
import org.traccar.model.Position;
import org.traccar.model.User;

import javax.swing.text.html.Option;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

/**
 * Created by niko on 2/2/16.
 */
public class NotificationRepository extends Repository{
    public void addNotification(RestrictionUnion restrictionUnion, Position position) {
        MongoCollection<Document> collection = database.getCollection(CollectionName.notifications);

        long id = getId(CollectionName.notifications);

        Document doc = new Document()
                .append("id", id)
                .append("restrictionUnits", restrictionUnion.getDocument());

        doc.append("deviceId", position.getDeviceId());
        doc.append("creationDate", new Date());
        doc.append("condition", restrictionUnion.getConditionString());
        doc.append("positionId", position.getId());
        doc.append("seen", false);
        collection.insertOne(doc);
    }

    public Optional<Notification> getLastNotification(RestrictionUnion restrictionUnion, Position position){
        MongoCollection<Document> collection = database.getCollection(CollectionName.notifications);

        Document document = collection.find(new Document("deviceId", position.getDeviceId())
                .append("condition", restrictionUnion.getConditionString()))
                .sort(new Document("creationDate", -1)).first();


        if (document ==null || document.isEmpty()) {
            return Optional.empty();
        }

        Notification notification = new Notification();
        notification.setId(document.getLong("id"));
        notification.setCreationDate(document.getDate("creationDate"));
        notification.setPositionId(document.getLong("positionId"));
        notification.setDeviceId(document.getLong("deviceId"));
        notification.setSeen(document.getBoolean("seen"));
        notification.setCanceled(document.getBoolean("canceled", false));
        notification.setCancelDate(document.getDate("cancelDate"));

        return Optional.of(notification);
    }

    public List<Notification> getNotifications(boolean all, User user) {

        List<Notification> notifications = new ArrayList<>();

        MongoCollection<Document> collection = database.getCollection(CollectionName.notifications);

        Collection<Device> devices = new DeviceRepository().getDevices(user);
        List<Long> deviceIds = devices.stream().mapToLong(Device::getId).boxed().collect(Collectors.toList());
        BasicDBObject q = new BasicDBObject("deviceId", new BasicDBObject("$in", deviceIds));
        if ( ! all) {
            q.append("seen", false);
        }

        MongoCursor<Document> iterator = collection.find(q).sort(new Document("creationDate", -1)).iterator();


        while (iterator.hasNext()) {
            Document document = iterator.next();

            Notification notification = new Notification();
            notification.setId(document.getLong("id"));
            notification.setCreationDate(document.getDate("creationDate"));
            notification.setPositionId(document.getLong("positionId"));
            notification.setDeviceId(document.getLong("deviceId"));
            notification.setSeen(document.getBoolean("seen"));
            if (document.containsKey("canceled")) {
                notification.setCanceled(document.getBoolean("canceled"));
                notification.setCancelDate(document.getDate("cancelDate"));
            }
//            Document r = (Document) document.get("restrictionUnit");

            notifications.add(notification);
        }

        return notifications;
    }

    public void markNotificationAsSeen(long notificationId) {
        database.getCollection(CollectionName.notifications).updateOne(new Document("id", notificationId),
                new Document("$set", new Document("seen", Boolean.TRUE)));
    }

    public void markNotificationAsCanceled(long notificationId) {
        database.getCollection(CollectionName.notifications)
                .updateOne(new Document("id", notificationId),
                        new Document("$set", new Document("canceled", Boolean.TRUE)
                                .append("cancelDate", new Date())));
    }
}
