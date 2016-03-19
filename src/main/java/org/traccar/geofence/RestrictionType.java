package org.traccar.geofence;

/**
 * Created by niko on 12/26/15.
 */
public interface RestrictionType {
    Integer INTO_AREA = 1;

    Integer OUT_OF_AREA = 2;

    Integer SPEED_EXCEED = 3;

    Integer DISTANCE_EXCEED = 4;

    Integer DATE_RESTRICTION = 5;
}
