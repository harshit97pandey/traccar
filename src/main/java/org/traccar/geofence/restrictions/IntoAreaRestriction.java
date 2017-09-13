package org.traccar.geofence.restrictions;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.bson.Document;
import org.traccar.database.mongo.PolygonRepository;
import org.traccar.geofence.restrictions.RestrictionType;
import org.traccar.geofence.restrictions.RestrictionUnit;
import org.traccar.model.Polygon;
import org.traccar.model.Position;
import org.traccar.rest.utils.PolygonUtil;

/**
 * Created by niko on 3/19/16.
 */
public class IntoAreaRestriction extends RestrictionUnit {
    private long polygonId;

    {
        restrictionType = RestrictionType.INTO_AREA;
    }

    @Override
    public Boolean test(Position position) {
        Polygon polygon = new PolygonRepository().getPolygon(polygonId);

        return PolygonUtil.contains(polygon.getId(), position.getLatitude(), position.getLongitude());
    }

    @Override
    public StringBuilder appendConditionAndGet(StringBuilder condition) {
        super.appendConditionAndGet(condition);
        condition.append("@polygonId:");
        condition.append(polygonId);
        return condition;
    }

    @Override
    @JsonIgnore
    public Document getDocument() {
        return super.getDocument().append("polygonId", polygonId);
    }

    public long getPolygonId() {
        return polygonId;
    }

    public void setPolygonId(long polygonId) {
        this.polygonId = polygonId;
    }
}
