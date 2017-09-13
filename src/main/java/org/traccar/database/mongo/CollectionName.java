package org.traccar.database.mongo;

/**
 * Created by Niko on 12/1/2015.
 */
public interface CollectionName {
    String server = "server";
    String device = "device";
    String position = "position";
    String userDevice = "user_device";
    String user = "user";
    String idGenerators = "id_generators";
    String polygon = "polygons";
    String notifications = "notifications";
    String restrictions = "restrictions";
}
