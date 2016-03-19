package org.traccar.geofence;

import org.bson.Document;
import org.traccar.model.Position;

/**
 * Created by niko on 3/19/16.
 */
public class SpeedRestriction extends RestrictionUnit {

    private Double speedLimit;

    {
        restrictionType = RestrictionType.SPEED_EXCEED;
    }
    @Override
    public Boolean test(Position position) {
        return null;
    }

    @Override
    public StringBuilder appendConditionAndGet(StringBuilder condition) {
        return null;
    }

    @Override
    public Document getDocument() {
        return null;
    }

    public Double getSpeedLimit() {
        return speedLimit;
    }

    public void setSpeedLimit(Double speedLimit) {
        this.speedLimit = speedLimit;
    }
}
