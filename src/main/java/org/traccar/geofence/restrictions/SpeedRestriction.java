package org.traccar.geofence.restrictions;

import org.bson.Document;
import org.traccar.geofence.restrictions.RestrictionType;
import org.traccar.geofence.restrictions.RestrictionUnit;
import org.traccar.model.Position;

/**
 * Created by niko on 3/19/16.
 */
public class SpeedRestriction extends RestrictionUnit {

    private Double speedLimit; //value in knots

    {
        restrictionType = RestrictionType.SPEED_EXCEED;
    }
    @Override
    public Boolean test(Position position) {
        return speedLimit <= position.getSpeed();
    }

    @Override
    public StringBuilder appendConditionAndGet(StringBuilder condition) {
        super.appendConditionAndGet(condition);
        condition.append("@speedLimit:");
        condition.append(speedLimit);
        return condition;
    }

    @Override
    public Document getDocument() {
        return super.getDocument().append("speedLimit", speedLimit);
    }

    public Double getSpeedLimit() {
        return speedLimit;
    }

    public void setSpeedLimit(Double speedLimit) {
        this.speedLimit = speedLimit;
    }
}
