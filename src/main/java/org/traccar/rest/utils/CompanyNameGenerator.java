package org.traccar.rest.utils;

import org.traccar.model.User;

/**
 * Created by niko on 2/27/16.
 */
public class CompanyNameGenerator {
    public static String generate(User user){
        if (user.isPersonal()) {
            return new StringBuilder()
                    .append("company")
                    .append("-")
                    .append(user.getId())
                    .toString();
        }
        throw new RuntimeException("User Must be personal");
    }
}
