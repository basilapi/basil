package uk.ac.open.kmi.basil.rest.auth;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.secnod.shiro.jaxrs.Auth;
import uk.ac.open.kmi.basil.core.auth.JDBCUserManager;
import uk.ac.open.kmi.basil.core.auth.User;
import uk.ac.open.kmi.basil.core.auth.UserManager;
import uk.ac.open.kmi.basil.rest.core.Headers;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.HttpURLConnection;
import java.net.URI;

/**
 * Created by Luca Panziera on 26/06/15.
 */
@Path("auth")
public class AuthResource {
    public static final String CURRENT_USER_KEY = "currentUser";
    @Context
    protected UriInfo requestUri;
    UserManager userManager = new JDBCUserManager();


    @POST
    @Path("login")
    @Consumes("application/json")
    @Produces("application/json")
    public Response login(String body) {
        Gson gson = new Gson();
        User user = gson.fromJson(body, User.class);
        try {
            UsernamePasswordToken token = new UsernamePasswordToken(user.getUsername(), user.getPassword());
            token.setRememberMe(true);
            Subject currentUser = SecurityUtils.getSubject();
            currentUser.login(token);
            Session session = currentUser.getSession();
            session.setAttribute(CURRENT_USER_KEY, user.getUsername());
            URI userUri = requestUri.getBaseUriBuilder().path("users").path(user.getUsername()).build();
            JsonObject m = new JsonObject();
            m.add("message", new JsonPrimitive("Login successful: " + user.getUsername()));
            m.add("user", new JsonPrimitive(userUri.toASCIIString()));
            return Response.created(userUri).entity(m.toString()).build();
        } catch (Exception e) {
            e.printStackTrace();
            JsonObject m = new JsonObject();
            m.add("message", new JsonPrimitive(e.getMessage()));
            return Response.serverError().entity(m.toString()).build();
        }
    }

    @GET
    @Path("logout")
    @Produces("application/json")
    public Response logout() {
        try {
            Subject currentUser = SecurityUtils.getSubject();
            String username = (String) currentUser.getSession().getAttribute(CURRENT_USER_KEY);
            currentUser.logout();
            URI userUri = requestUri.getBaseUriBuilder().path("users").path(username).build();
            JsonObject m = new JsonObject();
            m.add("message", new JsonPrimitive("Logout successful: " + username));
            m.add("location", new JsonPrimitive(userUri.toASCIIString()));
            return Response.ok().entity(m.toString()).build();
        } catch (Exception e) {
            JsonObject m = new JsonObject();
            m.add("message", new JsonPrimitive(e.getMessage()));
            return Response.serverError().entity(m.toString()).build();
        }
    }

    @GET
    @Path("me")
    @Produces("application/json")
    public Response me(@Auth Subject subject) {
        try {
            if (subject.isAuthenticated()) {
                String username = (String) subject.getSession().getAttribute(CURRENT_USER_KEY);
                Gson gson = new Gson();
                return Response.ok().entity(gson.toJson(userManager.getUser(username))).build();
            }

        } catch (Exception e) {
            JsonObject m = new JsonObject();
            m.add("message", new JsonPrimitive(e.getMessage()));
            return Response.serverError().entity(m.toString()).build();
        }
        return Response.status(HttpURLConnection.HTTP_FORBIDDEN)
                .header(Headers.Error, "User must be authenticated").build();
    }

}
