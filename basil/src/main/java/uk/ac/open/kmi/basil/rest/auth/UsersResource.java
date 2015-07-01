package uk.ac.open.kmi.basil.rest.auth;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import uk.ac.open.kmi.basil.core.auth.JDBCUserManager;
import uk.ac.open.kmi.basil.core.auth.User;
import uk.ac.open.kmi.basil.core.auth.UserManager;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Set;

/**
 * Created by Luca Panziera on 01/07/15.
 */

@Path("users")
public class UsersResource {

    @Context
    protected UriInfo requestUri;
    UserManager userManager = new JDBCUserManager();

    @POST
    @Consumes("application/json")
    @Produces("application/json")
    public Response createUser(String body) {
        Gson gson = new Gson();
        User user = gson.fromJson(body, User.class);
        try {
            userManager.createUser(user);
            URI userUri = requestUri.getBaseUriBuilder().path("users").path(user.getUsername()).build();
            JsonObject m = new JsonObject();
            m.add("message", new JsonPrimitive("Created user: " + user.getUsername()));
            m.add("user", new JsonPrimitive(userUri.toASCIIString()));
            return Response.created(userUri).entity(m.toString()).build();
        } catch (Exception e) {
            JsonObject m = new JsonObject();
            m.add("message", new JsonPrimitive(e.getMessage()));
            return Response.serverError().entity(m.toString()).build();
        }
    }

    // METHOD NOT SAFE
//    @Path("{username}")
//    @GET
//    @Produces("application/json")
//    public Response getUserInfo(@PathParam("username") String username) {
//        try {
//            Gson gson = new Gson();
//            return Response.ok().entity(gson.toJson(userManager.getUser(username))).build();
//        } catch (Exception e) {
//            JsonObject m = new JsonObject();
//            m.add("message", new JsonPrimitive(e.getMessage()));
//            return Response.serverError().entity(m.toString()).build();
//        }
//    }

    @Path("{username}/apis")
    @GET
    @Produces("application/json")
    public Response getUserApis(@PathParam("username") String username) {
        try {
            Set<String> apiIds = userManager.getUserApis(username);
            JsonArray r = new JsonArray();
            for (String api : apiIds) {
                r.add(new JsonPrimitive(String.valueOf(requestUri.getBaseUriBuilder().path(api))));
            }
            return Response.ok().entity(r.toString()).build();
        } catch (Exception e) {
            JsonObject m = new JsonObject();
            m.add("message", new JsonPrimitive(e.getMessage()));
            return Response.serverError().entity(m.toString()).build();
        }
    }

}
