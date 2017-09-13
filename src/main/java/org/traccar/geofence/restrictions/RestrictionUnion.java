package org.traccar.geofence.restrictions;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.bson.Document;
import org.traccar.geofence.restrictions.RestrictionUnit;
import org.traccar.model.Position;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by niko on 3/19/16.
 */
public class RestrictionUnion {

    public String idHex;

    private LinkedList<RestrictionUnit> units;

    private boolean enabled;

    private String companyName;

    public Boolean test(Position position){
        Boolean result = units.get(0).test(position);
        Predicate<Boolean> predicate = a -> result;
        for (int i= 1; i < units.size(); i++) {
            RestrictionUnit unit = units.get(i);
            boolean lResult = unit.test(position);

            if (unit.chainCondition) {
                predicate = predicate.and(a -> lResult);
            } else {
                predicate = predicate.or(a -> lResult);
            }
        }
        return predicate.test(result);
    }

    @JsonIgnore
    public List<Document> getDocument() {

        return units.stream().map(a -> a.getDocument()).collect(Collectors.toList());

    }

    @JsonIgnore
    public String getConditionString() {
        StringBuilder condition = new StringBuilder("C");

        units.forEach(a -> a.appendConditionAndGet(condition));

        return condition.toString();
    }
    public LinkedList<RestrictionUnit> getUnits() {
        return units;
    }


    public void setUnits(LinkedList<RestrictionUnit> units) {
        this.units = units;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }
}
