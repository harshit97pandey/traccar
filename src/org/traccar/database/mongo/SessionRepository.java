package org.traccar.database.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.BsonDouble;
import org.bson.Document;
import org.traccar.Config;
import org.traccar.database.IdentityManager;
import org.traccar.geofence.Notification;
import org.traccar.geofence.Restriction;
import org.traccar.geofence.RestrictionType;
import org.traccar.geofence.RestrictionUnit;
import org.traccar.helper.Log;
import org.traccar.model.*;
import org.traccar.rest.PositionEventEndpoint;
import org.traccar.web.JsonConverter;

import java.sql.SQLException;
import java.util.*;

/**
 * Created by Niko on 11/30/2015.
 */
public class SessionRepository extends Repository{

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

    public void updateLocation(long userId, String map, Integer zoom, Double latitude, Double longitude) {
        database.getCollection(CollectionName.user).updateOne(new Document("id", userId),
                new Document("$set", new Document("map", map)
                        .append("zoom", zoom)
                        .append("latitude", latitude)
                        .append("longitude",longitude)));
    }

    public void updateLanguage(long userId, String language) {
        database.getCollection(CollectionName.user).updateOne(new Document("id", userId),
                new Document("$set", new Document("language", language)));
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
