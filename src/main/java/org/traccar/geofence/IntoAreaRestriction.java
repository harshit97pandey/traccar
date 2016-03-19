package org.traccar.geofence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.bson.Document;
import org.traccar.database.mongo.PolygonRepository;
import org.traccar.model.Polygon;
import org.traccar.model.Position;
import org.traccar.rest.utils.PolygonUtil;

/**
 * Created by niko on 3/19/16.
 */
public class IntoAreaRestriction extends RestrictionUnit{
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
        condition.append("-");
        if(chainCondition) {
            condition.append("and");
        } else {
            condition.append("or");
        }
        condition.append("-");

        condition.append(RestrictionType.INTO_AREA);

        condition.append("->polygonId:");
        condition.append(polygonId);
        return condition;
    }

    @Override
    @JsonIgnore
    public Document getDocument() {
        return new Document("chainCondition", chainCondition)
                .append("polygonId", polygonId)
                .append("restrictionType", RestrictionType.INTO_AREA);
    }

    public long getPolygonId() {
        return polygonId;
    }

    public void setPolygonId(long polygonId) {
        this.polygonId = polygonId;
    }
}
