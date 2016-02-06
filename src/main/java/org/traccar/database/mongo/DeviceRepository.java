package org.traccar.database.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import org.bson.Document;
import org.traccar.Config;
import org.traccar.database.IdentityManager;
import org.traccar.geofence.RestrictionType;
import org.traccar.geofence.RestrictionUnit;
import org.traccar.model.Device;
import org.traccar.webSocket.PositionEventEndpoint;

import java.util.*;

/**
 * Created by niko on 2/2/16.
 */
public class DeviceRepository extends Repository implements IdentityManager{
    private static final long DEFAULT_REFRESH_DELAY = 300;
    private final Map<Long, Device> devicesById = new HashMap<>();
    private final Map<String, Device> devicesByUniqueId = new HashMap<>();
    private long devicesLastUpdate;
    private final long devicesRefreshDelay;

    public DeviceRepository(Config config) {
        devicesRefreshDelay = config.getLong("database.refreshDelay", DEFAULT_REFRESH_DELAY) * 1000;
    }

    public DeviceRepository() {
        devicesRefreshDelay = 0;
    }
    @Override
    public Device getDeviceById(long id) {
        return devicesById.get(id);
    }

    @Override
    public Device getDeviceByUniqueId(String uniqueId) {
        //TODO call super
        if (System.currentTimeMillis() - devicesLastUpdate > devicesRefreshDelay
                || !devicesByUniqueId.containsKey(uniqueId)) {
            devicesById.clear();
            devicesByUniqueId.clear();
            for (Device device : getAllDevices()) {
                devicesById.put(device.getId(), device);
                devicesByUniqueId.put(device.getUniqueId(), device);
            }
            devicesLastUpdate = System.currentTimeMillis();
        }

        return devicesByUniqueId.get(uniqueId);
    }

    public Collection<Device> getAllDevices() {
        MongoCursor<Document> cursor = database.getCollection(CollectionName.device).find().iterator();
        List<Device> devices = new ArrayList<>();
        while (cursor.hasNext()) {
            Document document = cursor.next();
            devices.add(getDeviceFromDocument(document));
        }

        return devices;
    }

    private Device getDeviceFromDocument(Document document){
        Device device = new Device();
        device.setId(document.getLong("id"));
        device.setName(document.getString("name"));
        device.setUniqueId(document.getString("uniqueId"));
        device.setStatus(document.getString("status"));
        device.setLastUpdate(document.getDate("lastUpdate"));
        device.setPositionId(document.getLong("positionId"));

        return device;
    }

    public Collection<Device> getDevices(long userId) {
        MongoCursor<Document> udCursor = database.getCollection(CollectionName.userDevice)
                .find(new BasicDBObject("userId", userId))
                .projection(new BasicDBObject("_id", 0).append("deviceId", 1)).iterator();

        List<Long> userDeviceIds = new ArrayList<>();
        while (udCursor.hasNext()) {
            Document next = udCursor.next();
            userDeviceIds.add(next.getLong("deviceId"));
        }

        MongoCursor<Document> deviceCursor = database.getCollection(CollectionName.device)
                .find(new BasicDBObject("id", new BasicDBObject("$in", userDeviceIds.toArray()))).iterator();
        List<Device> devices = new ArrayList<>();
        while (deviceCursor.hasNext()) {
            Document document = deviceCursor.next();
            devices.add(getDeviceFromDocument(document));
        }
        return devices;
    }

    public void addDevice(Device device) {
        MongoCollection<Document> collection = database.getCollection(CollectionName.device);
        long id = getId(CollectionName.device);
        device.setId(id);
        Document doc = new Document("id", device.getId())
                .append("name", device.getName())
                .append("uniqueId", device.getUniqueId())
                .append("status", device.getStatus())
                .append("lastUpdate", device.getLastUpdate())
                .append("positionId", device.getPositionId());
        collection.insertOne(doc);
    }

    public void updateDevice(Device device) {
        database.getCollection(CollectionName.device).updateOne(new Document("id", device.getId()),
                new Document("$set", new Document("name", device.getName())
                        .append("uniqueId", device.getUniqueId())));
    }

    public void updateDeviceStatus(Device device) {
        database.getCollection(CollectionName.device).updateOne(new Document("id", device.getId()),
                new Document("$set", new Document("status", device.getStatus())
                        .append("lastUpdate", device.getLastUpdate())));
    }

    public void removeDevice(Device device) {
        database.getCollection(CollectionName.device).findOneAndDelete(new Document("id", device.getId()));
        PositionEventEndpoint.sessionRefreshDevice(device.getId());
    }

    public void linkDevice(long userId, long deviceId) {
        MongoCollection<Document> collection = database.getCollection(CollectionName.userDevice);
        Document doc = new Document()
                .append("userId", userId)
                .append("deviceId", deviceId);
        collection.insertOne(doc);
        PositionEventEndpoint.sessionRefreshUser(userId);
    }

    public void unlinkDevice(long userId, long deviceId) {
        database.getCollection(CollectionName.userDevice)
                .findOneAndDelete(new BasicDBObject("userId", userId)
                        .append("deviceId", deviceId));
        PositionEventEndpoint.sessionRefreshUser(userId);
    }

    public List<RestrictionUnit> getDeviceRestrictions(long deviceId) {
        MongoCollection<Document> collection = database.getCollection(CollectionName.device);

        List<RestrictionUnit> restrictions = new ArrayList<>();

        Document device = collection.find(new Document("id", deviceId)).first();
        List<Long> polygons;
        if (device.containsKey("polygons")) {
            polygons = device.get("polygons", List.class);
        } else {
            polygons = new ArrayList<>();
        }

        for (Long polygon : polygons) {
            RestrictionUnit r = new RestrictionUnit();
            r.setPolygonId(polygon);
            r.setRestrictionType(RestrictionType.INTO_AREA);

            restrictions.add(r);
        }
        return restrictions;
    }
}
