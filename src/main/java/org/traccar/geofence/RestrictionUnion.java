package org.traccar.geofence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.bson.Document;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by niko on 3/19/16.
 */
public class RestrictionUnion {

    private LinkedList<RestrictionUnit> units;

    private boolean enabled;

    private String companyName;

    public Boolean test(){
        return false;
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
