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

    public Document getDocument(){
        return new Document("chainCondition", chainCondition)
                .append("restrictionType", restrictionType);
    }


    public abstract Boolean test(Position position);

    public StringBuilder appendConditionAndGet(StringBuilder condition) {

        if(chainCondition) {
            condition.append("[and]");
        } else {
            condition.append("[or]");
        }
        condition.append("[restriction]");

        condition.append(restrictionType);

        return condition;
    }

}