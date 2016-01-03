package org.traccar.rest.utils;

import org.traccar.model.Server;
import org.traccar.model.User;

/**
 * Created by niko on 1/3/16.
 */
public class UserPassport {

    private boolean valid;

    private User user;

    private Server server;

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }
}
