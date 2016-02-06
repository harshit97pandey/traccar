package org.traccar.geofence;

/**
 * Created by niko on 1/4/16.
 */
public class Alert {
    public String type;

    public Object message;


    public Alert(){}

    public Alert(String type, Object message) {
        this.type = type;
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Object getMessage() {
        return message;
    }

    public void setMessage(Object message) {
        this.message = message;
    }
}
