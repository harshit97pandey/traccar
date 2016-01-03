package org.traccar.geofence;

import java.util.Date;

/**
 * Created by niko on 1/3/16.
 */
public class Notification {
    private long id;

    private RestrictionUnit restrictionUnit;

    private long deviceId;

    private Date creationDate;

    private long positionId;

    private long polygonId;

    private String polygonName;

    private boolean seen;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public RestrictionUnit getRestrictionUnit() {
        return restrictionUnit;
    }

    public void setRestrictionUnit(RestrictionUnit restrictionUnit) {
        this.restrictionUnit = restrictionUnit;
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

    public long getPolygonId() {
        return polygonId;
    }

    public void setPolygonId(long polygonId) {
        this.polygonId = polygonId;
    }

    public String getPolygonName() {
        return polygonName;
    }

    public void setPolygonName(String polygonName) {
        this.polygonName = polygonName;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }
}