package org.traccar.rest.utils;

import javax.servlet.http.HttpServletRequest;
import java.security.AccessControlException;

import static org.traccar.web.BaseServlet.USER_ID_KEY;

/**
 * Created by niko on 11/28/15.
 */
public class SessionUtil {
    public static long getUserId(HttpServletRequest req) throws Exception  {
        Long userId = (Long) req.getSession().getAttribute(USER_ID_KEY);
        if (userId == null) {
            throw new AccessControlException("User not logged in");
        }
        return userId;
    }
}
