package org.traccar.database.mongo;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.traccar.geofence.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by niko on 3/19/16.
 */
public class RestrictionRepository extends Repository {

    private RestrictionUnit getUnit(Document document) {
        Integer restrictionType = document.getInteger("restrictionType");
        switch (restrictionType) {
            case 1:
                IntoAreaRestriction r = new IntoAreaRestriction();
                r.setPolygonId(document.getLong("polygonId"));
                r.restrictionType = restrictionType;
                r.chainCondition = document.getBoolean("chainCondition");
                return r;
            case 2:
                OutOfAreaRestriction r1 = new OutOfAreaRestriction();
                r1.restrictionType = restrictionType;
                r1.chainCondition = document.getBoolean("chainCondition");
                r1.setPolygonId(document.getLong("polygonId"));
                return r1;
            case 3:
                SpeedRestriction sr = new SpeedRestriction();
                sr.setSpeedLimit(document.getDouble("speedLimit"));
                sr.restrictionType = restrictionType;
                sr.chainCondition = document.getBoolean("chainCondition");
            case 4:
                DistanceRestriction dr = new DistanceRestriction();
                dr.setDistanceLimit(document.getDouble("distanceLimit"));
                dr.restrictionType = restrictionType;
                dr.chainCondition = document.getBoolean("chainCondition");
                return dr;
            default:
                return null;
        }
    }

    private LinkedList<RestrictionUnit> getUnits(Document document) {
        List<Document> restrictionsDocs = (List<Document>)document.get("restrictionUnits");
        LinkedList<RestrictionUnit> restrictionUnits = new LinkedList<>();
        for (Document d : restrictionsDocs) {
            restrictionUnits.add(getUnit(d));
        }
        return restrictionUnits;
    }
    public List<RestrictionUnion> getRestrictions(String companyName) {
        MongoCollection<Document> collection = database.getCollection(CollectionName.restrictions);
        MongoCursor<Document> cursor = collection.find(new Document("companyName", companyName)).iterator();

        List<RestrictionUnion> restrictionUnions = new ArrayList<>();
        while (cursor.hasNext()) {
            Document doc = cursor.next();
            RestrictionUnion union = new RestrictionUnion();

            ObjectId id = doc.getObjectId("_id");
            union.idHex = id.toHexString();
            union.setEnabled(doc.getBoolean("enabled"));
            union.setCompanyName(doc.getString("companyName"));
            union.setUnits(getUnits(doc));

            restrictionUnions.add(union);
        }

        return restrictionUnions;
    }

    public RestrictionUnion add(RestrictionUnion restrictionUnion) {
        MongoCollection<Document> collection = database.getCollection(CollectionName.restrictions);

        Document document =
                new Document()
                .append("enabled", restrictionUnion.isEnabled())
                .append("companyName", restrictionUnion.getCompanyName())
                .append("restrictionUnits", restrictionUnion.getDocument());

        collection.insertOne(document);
        ObjectId id = document.getObjectId("_id");
        restrictionUnion.idHex = id.toHexString();
        return restrictionUnion;
    }

    public Boolean applyToDevice(long deviceId, String restrictionUnionId) {
        MongoCollection<Document> collection = database.getCollection(CollectionName.device);
        Document device = collection.find(new Document("id", deviceId)).first();
        List<String> restrictionUnions;
        if (device.containsKey("restrictions")) {
            restrictionUnions = device.get("restrictions", List.class);
            if ( ! restrictionUnions.contains(restrictionUnionId)) {
                restrictionUnions.add(restrictionUnionId);
            }

        } else {
            restrictionUnions = new ArrayList<>();
            restrictionUnions.add(restrictionUnionId);
        }
        database.getCollection(CollectionName.device).updateOne(new Document("id", deviceId),
                new Document("$set", new Document("restrictions", restrictionUnions)));

        return true;
    }

    public Boolean ignoreFromDevice(long deviceId, String restrictionUnionId) {
        MongoCollection<Document> collection = database.getCollection(CollectionName.device);
        Document device = collection.find(new Document("id", deviceId)).first();
        List<String> restrictionUnions;
        if (device.containsKey("restrictions")) {
            restrictionUnions = device.get("restrictions", List.class);
            if (restrictionUnions.contains(restrictionUnionId)) {
                restrictionUnions.remove(restrictionUnionId);
                database.getCollection(CollectionName.device).updateOne(new Document("id", deviceId),
                        new Document("$set", new Document("restrictions", restrictionUnions)));
            }

        }

        return true;
    }

    public List<RestrictionUnion> getDeviceRestrictions(long deviceId) {
        MongoCollection<Document> collection = database.getCollection(CollectionName.device);
        MongoCollection<Document> restrictionCollection = database.getCollection(CollectionName.restrictions);

        List<RestrictionUnion> restrictionUnions = new ArrayList<>();

        Document device = collection.find(new Document("id", deviceId)).first();
        if (device.containsKey("restrictions")) {
            List<String> restrictionUnionIds = device.get("restrictions", List.class);
            for (String idHex : restrictionUnionIds) {
                Document doc = restrictionCollection.find(new Document("_id", new ObjectId(idHex))).first();

                RestrictionUnion union = new RestrictionUnion();

                ObjectId id = doc.getObjectId("_id");
                union.idHex = id.toHexString();
                union.setEnabled(doc.getBoolean("enabled"));
                union.setCompanyName(doc.getString("companyName"));
                union.setUnits(getUnits(doc));

                restrictionUnions.add(union);
            }

        }
        return restrictionUnions;
    }
}
