/*
 * Copyright (c) 2021. Enrico Daga and Luca Panziera
 *
 * MLicensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.github.basilapi.basil.rest.auth;

import java.net.HttpURLConnection;
import java.net.URI;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import io.github.basilapi.basil.core.auth.User;
import io.github.basilapi.basil.rest.core.AbstractResource;
import io.github.basilapi.basil.rest.core.Headers;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.secnod.shiro.jaxrs.Auth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import io.github.basilapi.basil.rest.msg.ErrorMessage;

/**
 * Created by Luca Panziera on 26/06/15.
 */
@Path("auth")
public class AuthResource extends AbstractResource {
    public static final String CURRENT_USER_KEY = "currentUser";
    @Context
    protected UriInfo requestUri;

	private static Logger log = LoggerFactory
			.getLogger(AuthResource.class);
	
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
        } catch(IncorrectCredentialsException | UnknownAccountException ice){
        	log.warn("Authentication failed: {}", ice.getMessage());
        	return Response.status(Status.FORBIDDEN).entity(new ErrorMessage(ice).asJSON()).build();
        } catch (Exception e) {
        	log.error("An error occurred", e);
            return Response.serverError().entity(new ErrorMessage(e).asJSON()).build();
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
        	log.error("An error occurred", e);
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
                return Response.ok().entity(gson.toJson(getUserManager().getUser(username))).build();
            }

        } catch (Exception e) {
        	log.error("An error occurred", e);
            JsonObject m = new JsonObject();
            m.add("message", new JsonPrimitive(e.getMessage()));
            return Response.serverError().entity(m.toString()).build();
        }
        return Response.status(HttpURLConnection.HTTP_FORBIDDEN)
                .header(Headers.Error, "User must be authenticated").build();
    }

}
