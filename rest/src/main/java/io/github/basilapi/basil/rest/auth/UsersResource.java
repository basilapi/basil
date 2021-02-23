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

import java.net.URI;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import io.github.basilapi.basil.core.auth.User;
import io.github.basilapi.basil.doc.Doc;
import io.github.basilapi.basil.rest.core.AbstractResource;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * Created by Luca Panziera on 01/07/15.
 */

@Path("users")
public class UsersResource extends AbstractResource {

    @Context
    protected UriInfo requestUri;

    @POST
    @Consumes("application/json")
    @Produces("application/json")
    public Response createUser(String body) {
        Gson gson = new Gson();
        User user = gson.fromJson(body, User.class);
        try {
            getUserManager().createUser(user);
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
            Set<String> apiIds = getUserManager().getUserApis(username);
            JsonArray r = new JsonArray();
            for (String api : apiIds) {
            	JsonObject object = new JsonObject();
    			Doc doc = getApiManager().getDoc(api);
    			object.add("id", new JsonPrimitive(api));
    			object.add("name", new JsonPrimitive(String.valueOf(doc.get(Doc.Field.NAME))));
    			object.add("createdby", new JsonPrimitive(username));
    			//object.add("description", new JsonPrimitive(doc.get(Field.DESCRIPTION)));
    			object.add("location", new JsonPrimitive(String.valueOf(requestUri.getBaseUriBuilder().path(api))));
                r.add(object);
            }
            return Response.ok().entity(r.toString()).build();
        } catch (Exception e) {
            JsonObject m = new JsonObject();
            m.add("message", new JsonPrimitive(e.getMessage()));
            return Response.serverError().entity(m.toString()).build();
        }
    }

}
