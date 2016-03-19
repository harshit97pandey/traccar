package org.traccar.geofence;

import org.bson.Document;
import org.traccar.model.Polygon;
import org.traccar.model.Position;
import org.traccar.rest.utils.PolygonUtil;

import java.util.function.BiFunction;

/**
 * Created by niko on 12/26/15.
 */
public abstract class RestrictionUnit {

    public boolean chainCondition;

    public Integer restrictionType;

    public abstract Document getDocument();


    public abstract Boolean test(Position position);

    public abstract StringBuilder appendConditionAndGet(StringBuilder condition);

}