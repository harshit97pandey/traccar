package org.traccar.database.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import org.bson.BsonDouble;
import org.bson.BsonInt64;
import org.bson.Document;
import org.traccar.geofence.Restriction;
import org.traccar.model.Position;

import java.util.*;

/**
 * Created by niko on 2/2/16.
 */
public class PositionRepository extends Repository{

    private static Map<Long, Position> parkingDevices = new HashMap<>();

    public Collection<Position> getPositions(long deviceId, Date from, Date to, Integer stopTime) {

        BasicDBObject queryObject = new BasicDBObject("deviceId", deviceId)
                .append("fixTime", new BasicDBObject("$gte", from).append("$lte", to));
        if (stopTime != null) {
            queryObject.append("calculatedStopTime",new BasicDBObject("$gte", stopTime));
        }

        MongoCursor<Document> cursor = database.getCollection(CollectionName.position).find(queryObject).sort(new BasicDBObject("fixTime", -1)).iterator();

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

            if (next.containsKey("calculatedDistance")) {
                position.setCalculatedDistance(next.getDouble("calculatedDistance"));
            }
            if (next.containsKey("calculatedStopTime")) {
                position.setCalculatedStopTime(next.getLong("calculatedStopTime"));
            }
            positions.add(position);
        }
        return positions;
    }

    public void addPosition(Position position) {


        MongoCollection<Document> collection = database.getCollection(CollectionName.position);
        Document lastPosition = collection.find(
                new BasicDBObject("deviceId", position.getDeviceId())
                        .append("fixTime", new BasicDBObject("$lte", position.getFixTime()))).sort(new BasicDBObject("fixTime", -1)).limit(1).first();
        double calculatedDistance = 0D;
        if (lastPosition != null) {
            if (lastPosition.containsKey("calculatedDistance")) {
                //calculatedDistance = lastPosition.getDouble("calculatedDistance");

                //TODO altitude
                //Get last position coordinates
                Double lastLongitude = lastPosition.getDouble("longitude");
                Double lastLatitude = lastPosition.getDouble("latitude");
                Double lastAltitude = lastPosition.getDouble("altitude");

                Long earthRadius = 6371000L; // metres
                double φ1 = Math.toRadians(lastLatitude);
                double φ2 = Math.toRadians(position.getLatitude());
                double Δφ = Math.toRadians(position.getLatitude() - lastLatitude);
                double Δλ = Math.toRadians(position.getLongitude() - lastLongitude);

                double a = Math.sin(Δφ/2) * Math.sin(Δφ/2) +
                        Math.cos(φ1) * Math.cos(φ2) *
                                Math.sin(Δλ/2) * Math.sin(Δλ/2);
                double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

                calculatedDistance = earthRadius * c;
            }
        }

        //
        calculateStopTime(position);

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
                .append("calculatedDistance", calculatedDistance)
                .append("calculatedStopTime", position.getCalculatedStopTime())
                .append("attributes", position.getAttributes());

        //Set calculated distance
        position.setCalculatedDistance(calculatedDistance);
        collection.insertOne(doc);



        //check restrictions
        new Restriction(position).apply();
    }
    private void calculateStopTime(Position position){
        Double speed = position.getSpeed();
        if(speed.equals(new Double(0))) {
            synchronized (parkingDevices){
                if(parkingDevices.containsKey(position.getDeviceId())) {
                    Position last = parkingDevices.get(position.getDeviceId());
                    long calculateStopTime = (position.getDeviceTime().getTime() - last.getDeviceTime().getTime()) / 1000;
                    position.setCalculatedStopTime(calculateStopTime);
                    parkingDevices.put(position.getDeviceId(), position);
                } else {
                    parkingDevices.put(position.getDeviceId(), position);
                }
            }
        } else {
            synchronized (parkingDevices) {
                if(parkingDevices.containsKey(position.getDeviceId())) {
                    parkingDevices.remove(position.getDeviceId());
                }
            }
        }
    }
    public void updateLatestPosition(Position position) {
        database.getCollection(CollectionName.device)
                .findOneAndUpdate(new BasicDBObject("id", position.getDeviceId()),
                        new BasicDBObject("$set", new BasicDBObject("positionId", position.getId())));
    }

    public Collection<Position> getLatestPositions() {
        List<Long> deviceIds = new ArrayList<>();
        MongoCollection<Document> device = database.getCollection(CollectionName.device);
        MongoCursor<Document> iterator = device.find().projection(new Document("_id", 0).append("id", 1)).iterator();
        while (iterator.hasNext()) {
            Document next = iterator.next();

            deviceIds.add(next.getLong("id"));
        }

        MongoCollection<Document> collection = database.getCollection(CollectionName.position);
        List<Position> positions = new ArrayList<>();
        for (Long deviceId : deviceIds) {

            Document document = collection
                    .find(new BasicDBObject("deviceId", deviceId))
                    .sort(new BasicDBObject("fixTime", -1)).first();

            if (document != null && !document.isEmpty()) {
                Position position = new Position();
                position.setId(document.getLong("id"));
                position.setDeviceId(document.getLong("deviceId"));
                position.setProtocol(document.getString("protocol"));
                position.setServerTime(document.getDate("serverTime"));
                position.setDeviceTime(document.getDate("deviceTime"));
                position.setFixTime(document.getDate("fixTime"));
                position.setOutdated(document.getBoolean("outdated", false));
                position.setValid(document.getBoolean("valid"));
                position.setLatitude(document.getDouble("latitude"));
                position.setLongitude(document.getDouble("longitude"));
                position.setAltitude(document.getDouble("altitude"));
                position.setSpeed(document.getDouble("speed"));
                position.setCourse(document.getDouble("course"));
                position.setAddress(document.getString("address"));
                if (document.containsKey("calculatedDistance")) {
                    position.setCalculatedDistance(document.getDouble("calculatedDistance"));
                }
                if (document.containsKey("calculatedStopTime")) {
                    position.setCalculatedStopTime(document.getLong("calculatedStopTime"));
                }
                positions.add(position);
            }
        }
        return positions;
    }

}
