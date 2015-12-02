package org.traccar.database.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.BsonDouble;
import org.bson.Document;
import org.traccar.Config;
import org.traccar.helper.Log;
import org.traccar.model.*;
import org.traccar.web.AsyncServlet;
import org.traccar.web.JsonConverter;

import java.sql.SQLException;
import java.util.*;

/**
 * Created by Niko on 11/30/2015.
 */
public class MongoDataManager extends org.traccar.database.DataManager {
    private static final long DEFAULT_REFRESH_DELAY = 300;

    private final Config config;

    private MongoClient dataSource;
    MongoDatabase database;
    private final Map<Long, Device> devicesById = new HashMap<>();
    private final Map<String, Device> devicesByUniqueId = new HashMap<>();
    private long devicesLastUpdate;
    private final long devicesRefreshDelay;

    public MongoDataManager(Config config) throws Exception {
        this.config = config;

        initDatabase();
        initDatabaseSchema();

        devicesRefreshDelay = config.getLong("database.refreshDelay", DEFAULT_REFRESH_DELAY) * 1000;
    }


    private void initDatabase() throws Exception {
        String host = config.getString("mongo.host");
        int port = config.getInteger("mongo.port");
        String dbName = config.getString("mongo.db.name");
        dataSource = new MongoClient(host, port);
        database = dataSource.getDatabase(dbName);
        initDatabaseSchema();
    }

    @Override
    public Device getDeviceById(long id) {
        return devicesById.get(id);
    }

    @Override
    public Device getDeviceByUniqueId(String uniqueId) throws SQLException {
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

    private String getQuery(String key) {
        String query = config.getString(key);
        if (query == null) {
            Log.info("Query not provided: " + key);
        }
        return query;
    }

    private void initDatabaseSchema() throws SQLException {
        List<String> databaseNames = dataSource.getDatabaseNames();
        if (!databaseNames.contains("traccar")) {
            User admin = new User();
            admin.setId(getId(CollectionName.user));
            admin.setName("admin");
            admin.setEmail("admin");
            admin.setAdmin(true);
            admin.setPassword("admin");
            addUser(admin);

            Server server = new Server();
            server.setRegistration(true);

            MongoCollection<Document> collection = database.getCollection(CollectionName.server);
            Document serverDocument = new Document("id", getId(CollectionName.server))
                    .append("registration", server.getRegistration())
                    .append("speedUnit", server.getSpeedUnit())
                    .append("map", server.getMap())
                    .append("mapUrl", server.getMapUrl())
                    .append("bingKey", server.getBingKey())
                    .append("latitude", server.getLatitude())
                    .append("longitude", server.getLongitude())
                    .append("language", server.getLanguage())
                    .append("distanceUnit", server.getDistanceUnit())
                    .append("zoom", server.getZoom());

            collection.insertOne(serverDocument);
            mockData(admin.getId());
        }
    }

    private void mockData(long userId) {
        if (config.getBoolean("database.mock")) {
            try {
                Device device = new Device();
                device.setName("test1");
                device.setUniqueId("123456789012345");
                addDevice(device);
                linkDevice(userId, device.getId());

                Position position = new Position();
                position.setDeviceId(device.getId());

                position.setTime(JsonConverter.parseDate("2015-05-22T12:00:01.000Z"));
                position.setServerTime(JsonConverter.parseDate("2015-05-22T12:00:01.000Z"));
                position.setLatitude(-36.8785803);
                position.setLongitude(174.7281713);
                position.setSpeed(1311.234123);
                addPosition(position);

                position.setTime(JsonConverter.parseDate("2015-05-22T12:00:02.000Z"));
                position.setLatitude(-36.8870932);
                position.setLongitude(174.7473116);
                position.setSpeed(1311.234123);
                addPosition(position);

                position.setTime(JsonConverter.parseDate("2015-05-22T12:00:03.000Z"));
                position.setLatitude(-36.8932371);
                position.setLongitude(174.7743053);
                position.setSpeed(1311.234123);
                addPosition(position);

                updateLatestPosition(position);

            } catch (SQLException error) {
                Log.warning(error);
            }
        }
    }

    private synchronized long getId(String model) {
        MongoCollection<Document> collection = database.getCollection(CollectionName.idGenerators);
        Document id = collection.find(new Document("name", model)).first();
        if (id == null) {
            Document doc = new Document("name", model)
                    .append("value", 1L);

            collection.insertOne(doc);
        }

        collection
                .updateOne(new Document("name", model), new Document("$inc", new Document("value", 1)));
        id = collection.find(new Document("name", model)).first();
        return id.getLong("value");
    }

    public User login(String email, String password) throws SQLException {
        MongoCollection<Document> collection = database.getCollection(CollectionName.user);
        Document cursor = collection.find(new BasicDBObject("email", email)).first();
        if (cursor != null && !cursor.isEmpty()) {
            User user = new User();
            user.setId(cursor.getLong("id"));
            user.setName(cursor.getString("name"));
            user.setEmail(cursor.getString("email"));
            user.setReadonly(cursor.getBoolean("readonly", false));
            user.setAdmin(cursor.getBoolean("admin", false));
            user.setMap(cursor.getString("map"));
            user.setLanguage(cursor.getString("language"));
            user.setDistanceUnit(cursor.getString("distanceUnit"));
            user.setSpeedUnit(cursor.getString("speedUnit"));
            user.setLatitude(cursor.getDouble("latitude"));
            user.setLongitude(cursor.getDouble("longitude"));
            user.setZoom(cursor.getInteger("zoom", 0));
            user.setHashedPassword(cursor.getString("hashedPassword"));
            user.setSalt(cursor.getString("salt"));

            if (user != null && user.isPasswordValid(password)) {
                return user;
            } else {
                return null;
            }
        }
        return null;
    }

    public Collection<User> getUsers() throws SQLException {
        MongoCollection<Document> collection = database.getCollection(CollectionName.user);
        MongoCursor<Document> cursor = collection.find().iterator();
        List<User> users = new ArrayList<>();
        try {
            while (cursor.hasNext()) {
                Document next = cursor.next();
                User user = new User();
                user.setId(next.getLong("id"));
                user.setName(next.getString("name"));
                user.setEmail(next.getString("email"));
                user.setReadonly(next.getBoolean("readonly", false));
                user.setAdmin(next.getBoolean("admin", false));
                user.setMap(next.getString("map"));
                user.setLanguage(next.getString("language"));
                user.setDistanceUnit(next.getString("distanceUnit"));
                user.setSpeedUnit(next.getString("speedUnit"));
                user.setLatitude(next.getDouble("latitude"));
                user.setLongitude(next.getDouble("longitude"));
                user.setZoom(next.getInteger("zoom", 0));
                users.add(user);
            }
        } finally {
            cursor.close();
        }
        return users;
    }

    public User getUser(long userId) throws SQLException {
        MongoCollection<Document> collection = database.getCollection(CollectionName.user);
        Document cursor = collection.find(new BasicDBObject("id", userId)).first();
        if (cursor != null && !cursor.isEmpty()) {
            User user = new User();
            user.setId(cursor.getLong("id"));
            user.setName(cursor.getString("name"));
            user.setEmail(cursor.getString("email"));
            user.setReadonly(cursor.getBoolean("readonly", false));
            user.setAdmin(cursor.getBoolean("admin", false));
            user.setMap(cursor.getString("map"));
            user.setLanguage(cursor.getString("language"));
            user.setDistanceUnit(cursor.getString("distanceUnit"));
            user.setSpeedUnit(cursor.getString("speedUnit"));
            user.setLatitude(cursor.getDouble("latitude"));
            user.setLongitude(cursor.getDouble("longitude"));
            user.setZoom(cursor.getInteger("zoom", 0));
            user.setHashedPassword(cursor.getString("hashedPassword"));
            user.setSalt(cursor.getString("salt"));
            return user;
        }
        return null;
    }

    public void addUser(User user) throws SQLException {
        MongoCollection<Document> collection = database.getCollection(CollectionName.user);
        long id = getId(CollectionName.user);
        user.setId(id);
        Document doc = new Document("id", user.getId())
                .append("name", user.getName())
                .append("email", user.getEmail())
                .append("admin", user.getAdmin())
                .append("hashedPassword", user.getHashedPassword())
                .append("readonly", user.getReadonly())
                .append("map", user.getMap())
                .append("language", user.getLanguage())
                .append("distanceUnit", user.getDistanceUnit())
                .append("speedUnit", user.getSpeedUnit())
                .append("latitude", user.getLatitude())
                .append("longitude", user.getLongitude())
                .append("zoom", user.getZoom())
                .append("salt", user.getSalt());

        collection.insertOne(doc);
    }

    public void updateUser(User user) throws SQLException {
        database.getCollection(CollectionName.user).updateOne(new Document("id", user.getId()),
                new Document("$set", new Document("name", user.getName())
                        .append("email", user.getEmail())
                        .append("admin", user.getAdmin())
                        .append("map", user.getMap())
                        .append("language", user.getLanguage())
                        .append("distanceUnit", user.getDistanceUnit())
                        .append("latitude", user.getLatitude())
                        .append("longitude", user.getLongitude())
                        .append("speedUnit", user.getSpeedUnit())
                        .append("zoom", user.getZoom())));

        if (user.getHashedPassword() != null) {
            database.getCollection(CollectionName.user).updateOne(new Document("id", user.getId()),
                    new Document("$set", new Document("hashedPassword", user.getHashedPassword())
                            .append("salt", user.getSalt())));
        }
    }

    public void removeUser(User user) throws SQLException {
        database.getCollection(CollectionName.user).findOneAndDelete(new BasicDBObject("id", user.getId()));
    }

    public Collection<Permission> getPermissions() throws SQLException {
        List<Permission> permissions = new ArrayList<>();
        MongoCollection<Document> collection = database.getCollection(CollectionName.userDevice);
        MongoCursor<Document> cursor = collection.find().iterator();
        try {
            while (cursor.hasNext()) {
                Document next = cursor.next();
                Permission permission = new Permission();
                permission.setUserId(next.getLong("userId"));
                permission.setDeviceId(next.getLong("deviceId"));

                permissions.add(permission);
            }
        } finally {
            cursor.close();
        }
        return permissions;
    }

    public Collection<Device> getAllDevices() throws SQLException {
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
        device.setDataId(document.getLong("dataId"));

        return device;
    }
    public Collection<Device> getDevices(long userId) throws SQLException {
        MongoCursor<Document> udCursor = database.getCollection(CollectionName.userDevice).find(new BasicDBObject("userId", userId)).iterator();

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

    public void addDevice(Device device) throws SQLException {
        MongoCollection<Document> collection = database.getCollection(CollectionName.device);
        long id = getId(CollectionName.device);
        device.setId(id);
        Document doc = new Document("id", device.getId())
                .append("name", device.getName())
                .append("uniqueId", device.getUniqueId())
                .append("status", device.getStatus())
                .append("lastUpdate", device.getLastUpdate())
                .append("positionId", device.getPositionId())
                .append("dataId", device.getDataId());
        collection.insertOne(doc);
    }

    public void updateDevice(Device device) throws SQLException {
        database.getCollection(CollectionName.device).updateOne(new Document("id", device.getId()),
                new Document("$set", new Document("name", device.getName())
                        .append("uniqueId", device.getUniqueId())));
    }

    public void updateDeviceStatus(Device device) throws SQLException {
        database.getCollection(CollectionName.device).updateOne(new Document("id", device.getId()),
                new Document("$set", new Document("status", device.getStatus())
                        .append("lastUpdate", device.getLastUpdate())));
    }

    public void removeDevice(Device device) throws SQLException {
        database.getCollection(CollectionName.device).findOneAndDelete(new Document("id", device.getId()));
        AsyncServlet.sessionRefreshDevice(device.getId());
    }

    public void linkDevice(long userId, long deviceId) throws SQLException {
        MongoCollection<Document> collection = database.getCollection(CollectionName.userDevice);
        Document doc = new Document()
                .append("userId", userId)
                .append("deviceId", deviceId);
        collection.insertOne(doc);
        AsyncServlet.sessionRefreshUser(userId);
    }

    public void unlinkDevice(long userId, long deviceId) throws SQLException {
        database.getCollection(CollectionName.userDevice)
                .findOneAndDelete(new BasicDBObject("userId", userId)
                .append("deviceId", deviceId));
        AsyncServlet.sessionRefreshUser(userId);
    }

    public Collection<Position> getPositions(long userId, long deviceId, java.util.Date from, java.util.Date to) throws SQLException {
        MongoCursor<Document> cursor = database.getCollection(CollectionName.position).find(
                new BasicDBObject("deviceId", deviceId)
                .append("fixTime", new BasicDBObject("$gte", from).append("$lte", to))).sort(new BasicDBObject("fixTime", -1)).iterator();

        List<Position> positions = new ArrayList<>();
        while (cursor.hasNext()) {
            Document next = cursor.next();

            Position position = new Position();
            position.setId(next.getLong("id"));
            position.setDeviceId(next.getLong("deviceId"));
            position.setProtocol(next.getString("protocol"));
            position.setServerTime(next.getDate("serverTime"));
            position.setDeviceTime(next.getDate("deviceTime"));
            position.setFixTime(next.getDate("fixTime"));
            position.setValid(next.getBoolean("valid", false));
            position.setLatitude(next.getDouble("latitude"));
            position.setLongitude(next.getDouble("longitude"));
            position.setAltitude(next.getDouble("altitude"));
            position.setSpeed(next.getDouble("speed"));
            position.setCourse(next.getDouble("course"));
            position.setAddress(next.getString("address"));

            positions.add(position);
        }
        return positions;
    }

    public void addPosition(Position position) throws SQLException {

        MongoCollection<Document> collection = database.getCollection(CollectionName.position);

        long id = getId(CollectionName.position);
        position.setId(id);
        Document doc = new Document()
                .append("id", position.getId())
                .append("deviceId", position.getDeviceId())
                .append("protocol", position.getProtocol())
                .append("serverTime", new Date())
                .append("deviceTime", position.getDeviceTime())
                .append("fixTime", position.getFixTime())
                .append("valid", position.getValid())
                .append("latitude", new BsonDouble(position.getLatitude()))//
                .append("longitude", new BsonDouble(position.getLongitude()))//
                .append("altitude", new BsonDouble(position.getAltitude()))
                .append("speed", position.getSpeed())
                .append("course", position.getCourse())
                .append("address", position.getAddress())
                .append("attributes", position.getAttributes());

        collection.insertOne(doc);
    }

    public void updateLatestPosition(Position position) throws SQLException {
        database.getCollection(CollectionName.device)
                .findOneAndUpdate(new BasicDBObject("id", position.getDeviceId()),
                new BasicDBObject("$set", new BasicDBObject("positionId", position.getId())));
    }

    public Collection<Position> getLatestPositions() throws SQLException {
        List<Long> positionIds = new ArrayList<>();
        MongoCollection<Document> device = database.getCollection(CollectionName.device);
        MongoCursor<Document> iterator = device.find().iterator();
        while (iterator.hasNext()) {
            Document next = iterator.next();
            positionIds.add(next.getLong("id"));
        }

        MongoCollection<Document> collection = database.getCollection(CollectionName.position);
        MongoCursor<Document> cursor = collection.find(new BasicDBObject("deviceId", new BasicDBObject("$in", positionIds.toArray()))).iterator();

        try {
            List<Position> positions = new ArrayList<>();
            while (cursor.hasNext()) {
                Document next = cursor.next();
                Position position = new Position();
                position.setId(next.getLong("id"));
                position.setDeviceId(next.getLong("deviceId"));
                position.setProtocol(next.getString("protocol"));
                position.setServerTime(next.getDate("serverTime"));
                position.setDeviceTime(next.getDate("deviceTime"));
                position.setFixTime(next.getDate("fixTime"));
                position.setOutdated(next.getBoolean("outdated", false));
                position.setValid(next.getBoolean("valid"));
                position.setLatitude(next.getDouble("latitude"));
                position.setLongitude(next.getDouble("longitude"));
                position.setAltitude(next.getDouble("altitude"));
                position.setSpeed(next.getDouble("speed"));
                position.setCourse(next.getDouble("course"));
                position.setAddress(next.getString("address"));
                positions.add(position);
            }
            return positions;
        } finally {
            cursor.close();
        }
    }

    public Server getServer() throws SQLException {
        Document cursor = database.getCollection(CollectionName.server).find().first();

        Server server = new Server();
        server.setId(cursor.getLong("id"));
        server.setBingKey(cursor.getString("bingKey"));
        server.setDistanceUnit(cursor.getString("distanceUnit"));
        server.setLanguage(cursor.getString("language"));
        server.setMap(cursor.getString("map"));
        server.setMapUrl(cursor.getString("mapUrl"));
        server.setSpeedUnit(cursor.getString("speedUnit"));
        server.setLatitude(cursor.getDouble("latitude"));
        server.setLongitude(cursor.getDouble("longitude"));
        server.setRegistration(cursor.getBoolean("registration"));
        server.setZoom(cursor.getInteger("zoom"));
        return server;
    }

    public void updateServer(Server server) throws SQLException {
        database.getCollection(CollectionName.server).updateOne(new Document("id", server.getId()),
                new Document("$set", new Document("registration", server.getRegistration())
                        .append("map", server.getMap())
                        .append("bingKey", server.getBingKey())
                        .append("mapUrl", server.getMapUrl())
                        .append("language", server.getLanguage())
                        .append("speedUnit", server.getSpeedUnit())
                        .append("distanceUnit", server.getDistanceUnit())
                        .append("latitude", server.getLatitude())
                        .append("longitude", server.getLongitude())
                        .append("zoom", server.getZoom())));
    }
}
