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

    public static String getTypeName(Integer code) {
        switch (code) {
            case 1: return "OUT_OF_AREA";
            case 2: return "INTO_AREA";
            case 3: return "SPEED_EXCEED";
            case 4: return "DISTANCE_EXCEED";
            case 5: return "DATE_RESTRICTION";
            default: return null;
        }
    }
}
