package org.traccar.database.mongo;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import org.bson.Document;
import org.traccar.geofence.IntoAreaRestriction;
import org.traccar.geofence.RestrictionUnion;
import org.traccar.geofence.RestrictionUnit;

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
        return restrictionUnion;
    }

    public List<RestrictionUnion> getDeviceRestrictions(long deviceId) {
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

           /* RestrictionUnit r = new RestrictionUnit();
            r.setPolygonId(polygon);
            r.setRestrictionType(RestrictionType.INTO_AREA);

            restrictions.add(r);*/
        }
        return null;
    }
}
