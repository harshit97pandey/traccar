package org.traccar.geofence;

/**
 * Created by niko on 1/6/16.
 */
public class Message {

    private String type;

    private Object body;

    public Message(){}

    public Message(String type, Object body) {
        this.type = type;
        this.body = body;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Object getBody() {
        return body;
    }

    public void setBody(Object body) {
        this.body = body;
    }
}
