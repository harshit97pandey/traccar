package org.traccar.database.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import org.bson.Document;
import org.traccar.model.Point;
import org.traccar.model.Polygon;
import org.traccar.model.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by niko on 2/2/16.
 */
public class PolygonRepository extends Repository {


    public void linkPolygon(long polygonId, long deviceId) {
        MongoCollection<Document> collection = database.getCollection(CollectionName.device);
        Document device = collection.find(new Document("id", deviceId)).first();
        List<Long> polygons;
        if (device.containsKey("polygons")) {
            polygons = device.get("polygons", List.class);
            if ( ! polygons.contains(polygonId)) {
                polygons.add(polygonId);
            }

        } else {
            polygons = new ArrayList<>();
            polygons.add(polygonId);
        }
        database.getCollection(CollectionName.device).updateOne(new Document("id", deviceId),
                new Document("$set", new Document("polygons", polygons)));
        //TODO refresh device
    }

    public void unlinkPolygon(long polygonId, long deviceId) {
        MongoCollection<Document> collection = database.getCollection(CollectionName.device);
        Document device = collection.find(new Document("id", deviceId)).first();
        if (device.containsKey("polygons")) {
            List<Long> polygons = device.get("polygons", List.class);
            if (polygons.contains(polygonId)) {
                polygons.remove(polygonId);
                database.getCollection(CollectionName.device).updateOne(new Document("id", deviceId),
                        new Document("$set", new Document("polygons", polygons)));
                //TODO refresh device
            }
        }
    }

    public Polygon addPolygon(Polygon polygon) {

        MongoCollection<Document> collection = database.getCollection(CollectionName.polygon);

        long id = getId(CollectionName.polygon);
        polygon.setId(id);

        List<Document> coordinates = new ArrayList<>();
        for (Point point : polygon.getCoordinates()){
            Document pointDocument = new Document()
                    .append("latitude", point.getLatitude())
                    .append("longitude", point.getLongitude());
            coordinates.add(pointDocument);
        }
        Document doc = new Document()
                .append("id", polygon.getId())
                .append("type", polygon.getType())
                .append("name", polygon.getName())
                .append("coordinates", coordinates);

        collection.insertOne(doc);
        return polygon;
    }

    public Polygon updatePolygon(Polygon polygon) {
        List<Document> coordinates = new ArrayList<>();
        for (Point point : polygon.getCoordinates()){
            Document pointDocument = new Document()
                    .append("latitude", point.getLatitude())
                    .append("longitude", point.getLongitude());
            coordinates.add(pointDocument);
        }
        Document doc = new Document()
                .append("type", polygon.getType())
                .append("name", polygon.getName())
                .append("coordinates", coordinates);

        database.getCollection(CollectionName.polygon).updateOne(new BasicDBObject("id", polygon.getId()),
                new Document("$set", new Document("type", polygon.getType())
                        .append("name", polygon.getName())
                        .append("coordinates", coordinates)));

        return polygon;
        //TODO update device
    }

    public void removePolygon(long polygonId) {
        database.getCollection(CollectionName.polygon).findOneAndDelete(new BasicDBObject("id", polygonId));

        MongoCollection<Document> collection = database.getCollection(CollectionName.device);
        MongoCursor<Document> iterator = collection.find(new Document("polygons", polygonId)).iterator();
        while (iterator.hasNext()) {
            Document device = iterator.next();
            List<Long> polygons = device.get("polygons", List.class);
            polygons.remove(polygonId);
            database.getCollection(CollectionName.device).updateOne(new Document("id", device.getLong("id")),
                    new Document("$set", new Document("polygons", polygons)));
            //TODO update device
        }
    }
    public List<Polygon> getPolygons(User u){
        List<Polygon> polygons = new ArrayList<>();
        MongoCollection<Document> collection = database.getCollection(CollectionName.polygon);

        MongoCursor<Document> iterator = collection
                .find(new BasicDBObject("company", u.getCompany()))
                .iterator();
        while (iterator.hasNext()) {
            Document next = iterator.next();
            Polygon polygon = new Polygon();
            polygon.setId(next.getLong("id"));
            polygon.setType(next.getString("type"));
            polygon.setName(next.getString("name"));
            if (next.containsKey("company")) {
                polygon.setCompany(next.getString("company"));
            }

            List<Document> coordinates = (List<Document>)next.get("coordinates");
            List<Point> points = new ArrayList<>();
            for(Document document : coordinates) {
                Point point = new Point();
                point.setLatitude(document.getDouble("latitude"));
                point.setLongitude(document.getDouble("longitude"));

                points.add(point);
            }
            polygon.setCoordinates(points);


            polygons.add(polygon);
        }

        return polygons;
    }

    public Polygon getPolygon(long polygonId){
        MongoCollection<Document> collection = database.getCollection(CollectionName.polygon);

        Document document = collection.find(new Document("id", polygonId)).limit(1).first();
        if (document != null) {
            Polygon polygon = new Polygon();
            polygon.setId(document.getLong("id"));
            polygon.setType(document.getString("type"));
            polygon.setName(document.getString("name"));

            List<Document> coordinates = (List<Document>)document.get("coordinates");
            List<Point> points = new ArrayList<>();
            for(Document c : coordinates) {
                Point point = new Point();
                point.setLatitude(c.getDouble("latitude"));
                point.setLongitude(c.getDouble("longitude"));

                points.add(point);
            }
            polygon.setCoordinates(points);
            return polygon;
        }

        return null;
    }

}
