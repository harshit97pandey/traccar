package org.traccar.rest.utils;

import org.traccar.Context;
import org.traccar.database.DataManager;
import org.traccar.database.mongo.MongoDataManager;
import org.traccar.model.Point;
import org.traccar.model.Polygon;

/**
 * Created by niko on 12/24/15.
 */
public class PolygonUtil {

    public static Boolean contains(Polygon polygon, Double latitude, Double longitude) {
        java.awt.Polygon shape = new java.awt.Polygon();

        for (Point point : polygon.getCoordinates()) {
            shape.addPoint(point.getLatitude().intValue(), point.getLongitude().intValue());
        }

        return shape.contains(latitude, longitude);
    }

    public static Boolean contains(long polygonId, Double latitude, Double longitude) {
        DataManager dataManager = Context.getDataManager();
        if (dataManager instanceof MongoDataManager) {
            MongoDataManager mongoDataManager = (MongoDataManager)dataManager;
            Polygon polygon = mongoDataManager.getPolygon(polygonId);

            return contains(polygon, latitude, longitude);
        }
        throw new RuntimeException("Not Implemented");
    }
}
