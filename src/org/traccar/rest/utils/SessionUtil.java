package org.traccar.rest.utils;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by niko on 11/28/15.
 */
public class SessionUtil {
    public static long getUserId(HttpServletRequest req) throws Exception  {
        /*
        String authorization = req.getHeader(HttpHeaders.Names.AUTHORIZATION);
        if (authorization != null && !authorization.isEmpty()) {
            Map<String, String> authMap = Authorization.parse(authorization);
            String username = authMap.get(Authorization.USERNAME);
            String password = authMap.get(Authorization.PASSWORD);
            User user = Context.getDataManager().login(username, password);
            if (user != null) {
                return user.getId();
            }
        }
        Long userId = (Long) req.getSession().getAttribute(USER_KEY);
        if (userId == null) {
            throw new AccessControlException("User not logged in");
        }*/
        return 1;
    }
}
