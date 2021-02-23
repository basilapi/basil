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

package io.github.basilapi.basil.rest.core;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import io.github.basilapi.basil.doc.Doc;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.subject.Subject;
import org.secnod.shiro.jaxrs.Auth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

/**
 * 
 * @author enridaga
 *
 */
@Path("{id}/docs")
@Api(value = "/basil", description = "BASIL operations")
public class DocsResource extends AbstractResource {
	private Logger log = LoggerFactory.getLogger(DocsResource.class);

	@GET
	@Produces("text/plain")
	@ApiOperation(value = "API documentation")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Not Found"),
			@ApiResponse(code = 500, message = "Internal error"),
			@ApiResponse(code = 204, message = "No content")
	})
	public Response get(@PathParam("id") String id) {
		log.trace("Calling GET docs with id: {}", id);
		try {
			try {
				// supports alias
				id = getApiId(id); 
			}catch(IOException e) {
				return packError(Response.status(404), "Not Found").build();
			}
			Doc doc = getApiManager().getDoc(id);
			ResponseBuilder builder;
			if (doc.isEmpty()) {
				builder = Response.noContent();
			} else {
				builder = Response.ok(doc.get(Doc.Field.DESCRIPTION));
				builder.header(Headers.Name, doc.get(Doc.Field.NAME));
			}
			addHeaders(builder, id);
			return builder.build();
		} catch (IOException e) {
			log.error("", e);
			return packError(Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR)
					, e).build();
		}
	}

	@DELETE
	@ApiOperation(value = "Delete API documentation")
	@ApiResponses(value = {
			@ApiResponse(code = 500, message = "Internal error"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 404, message = "Not Found"),
			@ApiResponse(code = 204, message = "Deleted. No content")
	})
	public Response delete(@PathParam("id") String id, @Auth Subject subject) {
		log.trace("Calling DELETE docs with id: {}", id);
		try {
			if(!isAuthenticated()){
				throw new AuthorizationException("Not authenticated");
			}
			try {
				// supports alias
				id = getApiId(id); 
			}catch(IOException e) {
				return packError(Response.status(404), "Not Found").build();
			}
			subject.checkRole(id); // is the creator
			boolean success = getApiManager().deleteDoc(id);
			ResponseBuilder builder;
			if (success) {
				builder = Response.noContent();
			} else {
				builder = Response.serverError();
			}
			addHeaders(builder, id);
			return builder.build();
		} catch (AuthorizationException e) {
			log.trace("Not authorized");
			return packError(Response.status(Status.FORBIDDEN), e).build();
		} catch (IOException e) {
			log.error("", e);
			return packError(Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR)
					, e).build();
		}
	}

	@PUT
	@Produces("text/plain")
	@ApiOperation(value = "To create a new doc file (plain text) and/or set a name for the API",
			notes = "The operation returns the resource URI of the doc file")
	@ApiResponses(value = { @ApiResponse(code = 400, message = "Body cannot be empty"),
			@ApiResponse(code = 201, message = "Doc file created"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 404, message = "API not found"),
			@ApiResponse(code = 409, message = "API does not exists (create the API first)."),
			@ApiResponse(code = 500, message = "Internal error") })
	public Response put(
			@ApiParam(value = "ID of the API specification", required = true) @PathParam("id") String id,
			@ApiParam(value = "Name of the API", required = false) @QueryParam("name") String name,
			@ApiParam(value = "Description of the API", required = false) String body,
			@Auth Subject subject
			) {
		log.trace("Calling PUT docs with id: {} name: {}", id, name);
		try {
			if(!isAuthenticated()){
				throw new AuthorizationException("Not authenticated");
			}
			log.trace("Body is: {}", body);
			try {
				// supports alias
				id = getApiId(id); 
			}catch(IOException e) {
				return packError(Response.status(404), "API Not Found").build();
			}
			subject.checkRole(id);
			if (name == null) {
				name = getParameterOrHeader("name");
			}
			getApiManager().createDoc(id, name, body);
			ResponseBuilder builder;
			JsonObject m = new JsonObject();
			URI docs = requestUri.getBaseUriBuilder().path(id).path("docs").build();
			m.add("message", new JsonPrimitive("Created: " + docs));
			m.add("location", new JsonPrimitive(docs.toString()));
			builder = Response.created(docs);
			builder.entity(m.toString());
			addHeaders(builder, id);
			return builder.build();
		} catch (AuthorizationException e) {
			log.trace("Not authorized");
			return packError(Response.status(Status.FORBIDDEN), e).build();
		} catch (Exception e) {
			log.error("An error occurred", e);
			return packError(Response.serverError(), e).build();
		}
	}

}
