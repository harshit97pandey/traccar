package org.traccar.geofence.restrictions;

import org.bson.Document;
import org.traccar.geofence.restrictions.RestrictionType;
import org.traccar.geofence.restrictions.RestrictionUnit;
import org.traccar.model.Position;

/**
 * Created by niko on 3/19/16.
 */
public class DateRestriction extends RestrictionUnit {

    private boolean periodical;

    private Integer dayOfWeek;

    private Integer interval;

    {
        restrictionType = RestrictionType.DATE_RESTRICTION;
    }
    @Override
    public Document getDocument() {
        return null;
    }

    @Override
    public Boolean test(Position position) {
        return null;
    }

    @Override
    public StringBuilder appendConditionAndGet(StringBuilder condition) {
        return null;
    }


    public boolean isPeriodical() {
        return periodical;
    }

    public void setPeriodical(boolean periodical) {
        this.periodical = periodical;
    }

    public Integer getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(Integer dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public Integer getInterval() {
        return interval;
    }

    public void setInterval(Integer interval) {
        this.interval = interval;
    }
}
