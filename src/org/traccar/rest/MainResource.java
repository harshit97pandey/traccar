package org.traccar.rest;

import org.traccar.Context;
import org.traccar.model.User;
import org.traccar.web.JsonConverter;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.StringReader;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.traccar.web.BaseServlet.USER_ID_KEY;

/**
 * Created by niko on 11/26/15.
 */
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class MainResource {

    public static Map<String, Long> sessions = new ConcurrentHashMap<>();
    @javax.ws.rs.core.Context
    HttpServletRequest req;

    @Path("session")
    @GET
    public Response session() throws SQLException, IOException {
        Long userId = (Long) req.getSession().getAttribute(USER_ID_KEY);
        if (userId != null) {

            return ResponseBuilder.getResponse(JsonConverter.objectToJson(
                    Context.getDataManager().getUser(userId)));
        }
        return ResponseBuilder.getResponse(false);
    }

    @Path("login")
    @POST
    public Response logOn(@FormParam("email") String email,
                          @FormParam("password") String password) throws Exception{
        User user = Context.getDataManager().login(
                email, password);
        if (user != null) {
            req.getSession().setAttribute(USER_ID_KEY, user.getId());
            sessions.put(req.getSession().getId(), user.getId());
            return ResponseBuilder.getResponse(JsonConverter.objectToJson(user));
        }
        return ResponseBuilder.getResponse(false);
    }

    @Path("logout")
    @GET
    public Response logout() throws IOException {
        req.getSession().removeAttribute(USER_ID_KEY);
        sessions.remove(req.getSession().getId());
        return ResponseBuilder.getResponse(true);
    }

    @Path("register")
    @POST
    public Response register(User user) throws IOException, ParseException, SQLException {
        Context.getDataManager().addUser(user);
        return ResponseBuilder.getResponse(true);
    }
}