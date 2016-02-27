package org.traccar.rest;

import org.traccar.Context;
import org.traccar.database.mongo.SessionRepository;
import org.traccar.model.User;
import org.traccar.rest.utils.SessionUtil;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.traccar.rest.utils.SessionUtil.USER_DATA;
import static org.traccar.rest.utils.SessionUtil.USER_ID_KEY;

/**
 * Created by niko on 11/28/15.
 */
@Path("users")
@Produces(MediaType.APPLICATION_JSON)
public class UserResource {

    @javax.ws.rs.core.Context
    HttpServletRequest req;

    @GET
    public Response get() throws Exception {
        Context.getPermissionsManager().checkAdmin(SessionUtil.getUserId(req));

        return Response.ok().entity(new SessionRepository().getUsers()).build();
    }

    @Path("/add")
    @POST
    public Response add(User user) throws Exception {
        Context.getPermissionsManager().checkRegistration(SessionUtil.getUserId(req));

        User actorUser = (User) req.getSession().getAttribute(USER_DATA);
        SessionRepository sessionRepository = new SessionRepository();

        String company = actorUser.getCompany();
        user.setCompany(company == null? null : company);
        sessionRepository.addUser(user);

        //TODO session refresh refactoring
        Context.getPermissionsManager().refresh();
        return Response.ok(user).build();



    }

    @Path("/register")
    @POST
    public Response register(User user) throws Exception {
        SessionRepository sessionRepository = new SessionRepository();
        if ( ! user.isPersonal()) {
            if (sessionRepository.existsCompany(user.getCompany())) {
                return Response
                        .status(Response.Status.FORBIDDEN)
                        .entity("Company Already Exists!")
                        .build();
            }
        }

        sessionRepository.addUser(user);
        Context.getPermissionsManager().refresh();
        return Response.ok(user).build();
    }

    @Path("{id}")
    @PUT
    public Response update(@PathParam("id") long id, User entity) throws Exception {
        if (entity.getAdmin()) {
            Context.getPermissionsManager().checkAdmin(SessionUtil.getUserId(req));
        } else {
            Context.getPermissionsManager().checkUser(SessionUtil.getUserId(req), entity.getId());
        }
        new SessionRepository().updateUser(entity);
        Context.getPermissionsManager().refresh();
        return Response.ok(entity).build();
    }

    @Path("{id}")
    @DELETE
    public Response remove(@PathParam("id") long id) throws Exception {
        Context.getPermissionsManager().checkUser(SessionUtil.getUserId(req), id);
        User user = new User();
        user.setId(id);

        new SessionRepository().removeUser(user);
        Context.getPermissionsManager().refresh();
        return Response.noContent().build();
    }

    @Path("location/update")
    @POST
    public Response updateLocation(
            @FormParam("map") String map,
            @FormParam("zoom") int zoom,
            @FormParam("latitude") double latitude,
            @FormParam("longitude") double longitude) throws Exception {
        long userId = SessionUtil.getUserId(req);
        Context.getPermissionsManager().checkUser(SessionUtil.getUserId(req), userId);

        new SessionRepository().updateLocation(userId, map, zoom, latitude, longitude);

        return Response.ok().build();
    }

    @Path("language/update")
    @POST
    public Response updateLocation(@FormParam("language") String language) throws Exception {
        long userId = SessionUtil.getUserId(req);
        Context.getPermissionsManager().checkUser(SessionUtil.getUserId(req), userId);

        new SessionRepository().updateLanguage(userId, language);
        return Response.ok().build();
    }

}
