package org.traccar.rest;

import org.traccar.Context;
import org.traccar.model.User;
import org.traccar.web.JsonConverter;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.traccar.web.BaseServlet.USER_ID_KEY;

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
        Long userId = (Long) req.getSession().getAttribute(USER_ID_KEY);
        if (userId != null) {
            return Response.ok().entity(Context.getDataManager().getUser(userId)).build();
        } else {
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).build());
        }
    }

    @POST
    public Response add(
            @FormParam("email") String email,
            @FormParam("password") String password) throws SQLException, IOException {
        User user = Context.getDataManager().login(email, password);
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