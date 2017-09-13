package org.traccar.geofence;

import org.traccar.geofence.restrictions.RestrictionUnit;

import java.util.Date;
import java.util.List;

/**
 * Created by niko on 1/3/16.
 */
public class Notification {
    private long id;

    private List<RestrictionUnit> restrictionUnits;

    private long deviceId;

    private Date creationDate;

    private long positionId;

    private boolean seen;

    private boolean canceled;

    private Date cancelDate;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public List<RestrictionUnit> getRestrictionUnits() {
        return restrictionUnits;
    }

    public void setRestrictionUnits(List<RestrictionUnit> restrictionUnits) {
        this.restrictionUnits = restrictionUnits;
    }

    public long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(long deviceId) {
        this.deviceId = deviceId;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public long getPositionId() {
        return positionId;
    }

    public void setPositionId(long positionId) {
        this.positionId = positionId;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public boolean isCanceled() {
        return canceled;
    }

    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }

    public Date getCancelDate() {
        return cancelDate;
    }

    public void setCancelDate(Date cancelDate) {
        this.cancelDate = cancelDate;
    }
}