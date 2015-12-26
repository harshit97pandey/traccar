package org.traccar.rest;

import org.traccar.Context;
import org.traccar.model.Server;
import org.traccar.rest.utils.SessionUtil;
import org.traccar.web.JsonConverter;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Created by niko on 11/28/15.
 */
@Path("server")
@Produces(MediaType.APPLICATION_JSON)
public class ServerResource {

    @javax.ws.rs.core.Context
    HttpServletRequest req;

    @GET
    public Response get() throws SQLException, IOException {
     return Response.ok().entity(Context.getDataManager().getServer()).build();
    }

    @PUT
    public Response update(Server server) throws Exception {
        Context.getPermissionsManager().checkAdmin(SessionUtil.getUserId(req));
        Context.getDataManager().updateServer(server);
        Context.getPermissionsManager().refresh();
        return Response.ok(server).build();
    }
}
