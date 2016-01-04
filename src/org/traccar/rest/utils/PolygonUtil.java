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
        double a = 6378137.0d;
        double b = 6356752.3142d;
        double e = 0.00669437999014;

        double lat = (Math.PI * a * (1- e)) /
                 (180 * Math.pow(1 - e * Math.pow(Math.sin(Math.toRadians(latitude)), 2),3/2));
        double lo  = (Math.PI * a * Math.cos(Math.toRadians(longitude))) /
                (180 * Math.pow(1 - e * Math.pow(Math.sin(Math.toRadians(longitude)), 2),1/2));

        java.awt.Polygon shape = new java.awt.Polygon();

        for (Point point : polygon.getCoordinates()) {
            shape.addPoint(point.getLatitude().intValue(), point.getLongitude().intValue());
        }

        boolean contains = shape.contains(lat, lo);
        return contains;
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
