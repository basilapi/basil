package uk.ac.open.kmi.basil.rest.auth;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import uk.ac.open.kmi.basil.core.auth.JDBCUserManager;
import uk.ac.open.kmi.basil.core.auth.User;
import uk.ac.open.kmi.basil.core.auth.UserManager;
import uk.ac.open.kmi.basil.core.auth.exceptions.UserCreationException;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;

/**
 * Created by Luca Panziera on 26/06/15.
 */
@Path("auth")
public class AuthResource {
    @Context
    protected UriInfo requestUri;


    UserManager userManager = new JDBCUserManager();

    @POST
    @Path("users")
    @Consumes("application/json")
    @Produces("application/json")
    public Response createUser(String body) {
        Gson gson = new Gson();
        User user = gson.fromJson(body, User.class);
        try {
            userManager.createUser(user);
        } catch (UserCreationException e) {
            JsonObject m = new JsonObject();
            m.add("message", new JsonPrimitive(e.getMessage()));
            return Response.serverError().entity(m.toString()).build();
        }
        URI userUri = requestUri.getBaseUriBuilder().path("auth").path("users").path(user.getUsername()).build();
        JsonObject m = new JsonObject();
        m.add("message", new JsonPrimitive("Created user: " + user.getUsername()));
        m.add("location", new JsonPrimitive(userUri.toASCIIString()));
        return Response.created(userUri).entity(m.toString()).build();
    }
}
