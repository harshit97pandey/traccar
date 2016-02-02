package org.traccar.database.mongo;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import org.bson.Document;
import org.traccar.geofence.Notification;
import org.traccar.geofence.RestrictionUnit;
import org.traccar.model.Polygon;
import org.traccar.model.Position;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by niko on 2/2/16.
 */
public class NotificationRepository extends Repository{
    public void addNotification(RestrictionUnit restrictionUnit, Polygon polygon, Position position) throws SQLException {
        MongoCollection<Document> collection = database.getCollection(CollectionName.notifications);

        long id = getId(CollectionName.notifications);


        Document doc = new Document()
                .append("id", id)
                .append("restrictionUnit",
                        new Document("polygonId", restrictionUnit.getPolygonId())
                                .append("restrictionType", restrictionUnit.getRestrictionType()));

        doc.append("deviceId", position.getDeviceId());
        doc.append("creationDate", new Date());
        doc.append("positionId", position.getId());
        doc.append("polygonId", polygon.getId());
        doc.append("polygonName", polygon.getName());
        doc.append("seen", false);
        collection.insertOne(doc);
    }

    public Notification getLastNotification(RestrictionUnit restrictionUnit, Position position){
        MongoCollection<Document> collection = database.getCollection(CollectionName.notifications);

        Document document = collection.find(new Document("deviceId", position.getDeviceId())
                .append("polygonId", restrictionUnit.getPolygonId())
                .append("restrictionUnit", new Document("polygonId", restrictionUnit.getPolygonId())
                        .append("restrictionType", restrictionUnit.getRestrictionType())))
                .sort(new Document("creationDate", -1)).first();


        if (document ==null || document.isEmpty()) {
            return null;
        }

        Notification notification = new Notification();
        notification.setId(document.getLong("id"));
        notification.setPolygonId(document.getLong("polygonId"));
        notification.setCreationDate(document.getDate("creationDate"));
        notification.setPolygonName(document.getString("polygonName"));
        notification.setPositionId(document.getLong("positionId"));
        notification.setDeviceId(document.getLong("deviceId"));
        notification.setSeen(document.getBoolean("seen"));
        if (document.containsKey("canceled")) {
            notification.setCanceled(document.getBoolean("canceled"));
            notification.setCancelDate(document.getDate("cancelDate"));
        }

        Document rd = (Document) document.get("restrictionUnit");
        RestrictionUnit r = new RestrictionUnit();
        r.setPolygonId(rd.getLong("polygonId"));
        r.setRestrictionType(rd.getInteger("restrictionType"));

        return notification;
    }

    public List<Notification> getNotifications(boolean all) throws SQLException {

        List<Notification> notifications = new ArrayList<>();

        MongoCollection<Document> collection = database.getCollection(CollectionName.notifications);
        FindIterable<Document> iterable;
        if (all) {
            iterable = collection.find();
        } else {
            iterable = collection.find(new Document("seen", false));
        }

        iterable.sort(new Document("creationDate", -1));

        MongoCursor<Document> iterator = iterable.iterator();


        while (iterator.hasNext()) {
            Document document = iterator.next();

            Notification notification = new Notification();
            notification.setId(document.getLong("id"));
            notification.setPolygonId(document.getLong("polygonId"));
            notification.setCreationDate(document.getDate("creationDate"));
            notification.setPolygonName(document.getString("polygonName"));
            notification.setPositionId(document.getLong("positionId"));
            notification.setDeviceId(document.getLong("deviceId"));
            notification.setSeen(document.getBoolean("seen"));

            Document r = (Document) document.get("restrictionUnit");
            RestrictionUnit restrictionUnit = new RestrictionUnit();
            restrictionUnit.setPolygonId(r.getLong("polygonId"));
            restrictionUnit.setRestrictionType(r.getInteger("restrictionType"));

            notification.setRestrictionUnit(restrictionUnit);
            notifications.add(notification);
        }

        return notifications;
    }

    public void markNotificationAsSeen(long notificationId) throws SQLException {
        database.getCollection(CollectionName.notifications).updateOne(new Document("id", notificationId),
                new Document("$set", new Document("seen", Boolean.TRUE)));
    }

    public void markNotificationAsCanceled(long notificationId) throws SQLException {
        database.getCollection(CollectionName.notifications)
                .updateOne(new Document("id", notificationId),
                        new Document("$set", new Document("canceled", Boolean.TRUE)
                                .append("cancelDate", new Date())));
    }
}
