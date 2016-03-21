package org.traccar.geofence;

import org.bson.Document;
import org.traccar.model.Position;

/**
 * Created by niko on 3/21/16.
 */
public class DistanceRestriction extends RestrictionUnit{

    private double distanceLimit;

    {
        restrictionType = RestrictionType.DISTANCE_EXCEED;
    }

    @Override
    public Document getDocument() {
        return super.getDocument().append("distanceLimit", distanceLimit);
    }

    @Override
    public Boolean test(Position position) {
        return position.getCalculatedDistance() <= distanceLimit;
    }

    @Override
    public StringBuilder appendConditionAndGet(StringBuilder condition) {
        super.appendConditionAndGet(condition);
        condition.append("@distanceLimit:");
        condition.append(distanceLimit);
        return condition;
    }

    public double getDistanceLimit() {
        return distanceLimit;
    }
    public void setDistanceLimit(double distanceLimit) {
        this.distanceLimit = distanceLimit;
    }
}
