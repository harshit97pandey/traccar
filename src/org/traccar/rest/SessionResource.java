package org.traccar.rest;

import org.traccar.database.mongo.SessionRepository;
import org.traccar.model.User;
import org.traccar.rest.utils.UserPassport;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.traccar.rest.utils.SessionUtil.USER_ID_KEY;
/**
 * Created by niko on 11/26/15.
 */
@Path("/session")
@Produces(MediaType.APPLICATION_JSON)
public class SessionResource {

    public static Map<String, Long> sessions = new ConcurrentHashMap<>();
    @javax.ws.rs.core.Context
    HttpServletRequest req;

    @GET
    public Response session() throws SQLException, IOException {
        UserPassport userPassport = new UserPassport();

        Long userId = (Long) req.getSession().getAttribute(USER_ID_KEY);
        if (userId != null) {
            SessionRepository sessionRepository = new SessionRepository();
            userPassport.setUser(sessionRepository.getUser(userId));
            userPassport.setServer(sessionRepository.getServer());
            userPassport.setValid(Boolean.TRUE);
        }

        return Response.ok().entity(userPassport).build();
    }

    @POST
    public Response add(
            @FormParam("email") String email,
            @FormParam("password") String password) throws SQLException, IOException {
        User user = new SessionRepository().login(email, password);
        if (user != null) {
            req.getSession().setAttribute(USER_ID_KEY, user.getId());
            sessions.put(req.getSession().getId(), user.getId());
            return Response.ok().entity(user).build();
        } else {
            throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED).build());
        }
    }


    @DELETE
    public Response remove() {
        req.getSession().removeAttribute(USER_ID_KEY);
        return Response.noContent().build();
    }
}