package org.traccar.database.mongo;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.traccar.Config;
import org.traccar.model.Device;
import org.traccar.model.Position;
import org.traccar.model.Server;
import org.traccar.model.User;
import org.traccar.web.JsonConverter;

import java.util.List;

/**
 * Created by niko on 2/2/16.
 */
//TODO company name migration
public class Repository {
    private MongoClient dataSource;

    protected static MongoDatabase database;

    protected Config config;

    public Repository(){}
    public Repository(Config config) throws Exception {
        this.config = config;

        initDatabase();
        initDatabaseSchema();
    }

    private void initDatabase() throws Exception {
        String host = config.getString("mongo.host");
        int port = config.getInteger("mongo.port");
        String dbName = config.getString("mongo.db.name");
        dataSource = new MongoClient(host, port);
        database = dataSource.getDatabase(dbName);
        initDatabaseSchema();
    }

    private void initDatabaseSchema() {
        List<String> databaseNames = dataSource.getDatabaseNames();
        if (!databaseNames.contains("traccar")) {
            User admin = new User();
            admin.setId(getId(CollectionName.user));
            admin.setName("admin");
            admin.setEmail("admin");
            admin.setAdmin(true);
            admin.setPassword("admin");
            new SessionRepository().addUser(admin);

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

    protected synchronized long getId(String model) {
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

    private void mockData(long userId) {
        if (config.getBoolean("database.mock")) {
            Device device = new Device();
            device.setName("test1");
            device.setUniqueId("123456789012345");
            new DeviceRepository().addDevice(device);
            new DeviceRepository().linkDevice(userId, device.getId());

            Position position = new Position();
            position.setDeviceId(device.getId());

            position.setTime(JsonConverter.parseDate("2015-05-22T12:00:01.000Z"));
            position.setServerTime(JsonConverter.parseDate("2015-05-22T12:00:01.000Z"));
            position.setLatitude(-36.8785803);
            position.setLongitude(174.7281713);
            position.setSpeed(1311.234123);
            new PositionRepository().addPosition(position);

            position.setTime(JsonConverter.parseDate("2015-05-22T12:00:02.000Z"));
            position.setLatitude(-36.8870932);
            position.setLongitude(174.7473116);
            position.setSpeed(1311.234123);
            new PositionRepository().addPosition(position);

            position.setTime(JsonConverter.parseDate("2015-05-22T12:00:03.000Z"));
            position.setLatitude(-36.8932371);
            position.setLongitude(174.7743053);
            position.setSpeed(1311.234123);
            new PositionRepository().addPosition(position);

            new PositionRepository().updateLatestPosition(position);
        }
    }


}
