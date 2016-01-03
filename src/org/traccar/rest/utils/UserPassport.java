package org.traccar.rest.utils;

import org.traccar.model.User;

/**
 * Created by niko on 1/3/16.
 */
public class UserPassport {

    private boolean valid;

    private User user;

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
}
