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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import io.github.basilapi.basil.alias.AliasUtils;
import io.github.basilapi.basil.alias.BadAliasException;
import org.apache.commons.lang3.StringUtils;
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
@Path("{id}/alias")
@Api(value = "/basil", description = "BASIL operations")
public class AliasResource extends AbstractResource {
	private Logger log = LoggerFactory.getLogger(AliasResource.class);

	@GET
	@Produces("text/plain")
	@ApiOperation(value = "List API aliases")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Not Found"),
			@ApiResponse(code = 500, message = "Internal error"),
			@ApiResponse(code = 204, message = "No content")
	})
	public Response get(@PathParam("id") String id) {
		log.trace("Calling GET alias with id: {}", id);
		try {
			try {
				// supports alias
				id = getApiId(id); 
			}catch(IOException e) {
				return packError(Response.status(404), "Not Found").build();
			}
			Set<String> alias = getApiManager().getAlias(id);
			ResponseBuilder builder;
			if (alias.isEmpty()) {
				builder = Response.noContent();
			} else {
				builder = Response.ok(StringUtils.join(alias.iterator(), "\n"));
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
	@ApiOperation(value = "Delete all API alias")
	@ApiResponses(value = {
			@ApiResponse(code = 500, message = "Internal error"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 204, message = "Deleted. No content")
	})
	public Response delete(@PathParam("id") String id, @Auth Subject subject) {
		log.trace("Calling DELETE alias for API id: {}", id);
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
			boolean success = getApiManager().deleteAlias(id);
			if(success) {
				// Clear alias cache
				getAliasCache().removeAll(id);
			}
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
	@ApiOperation(value = "To create or update an alias list for the API",
			notes = "The operation returns the resource URI of the alias list")
	@ApiResponses(value = { @ApiResponse(code = 400, message = "Body cannot be empty"),
			@ApiResponse(code = 201, message = "Alias list created"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 400, message = "Malformed body"),
			@ApiResponse(code = 409, message = "API does not exists (create the API first)."),
			@ApiResponse(code = 500, message = "Internal error") })
	public Response put(
			@ApiParam(value = "ID of the API specification", required = true) @PathParam("id") String id,
			@ApiParam(value = "Alias list for the API (newline separated)", required = true) String body,
			@Auth Subject subject
			) {
		log.trace("Calling PUT credentials with id: {}", id);
		try {
			if(!isAuthenticated()){
				throw new AuthorizationException("Not authenticated");
			}
			log.info("Body is: {}", body);
			try {
				// supports alias
				id = getApiId(id); 
			}catch(IOException e) {
				return packError(Response.status(404), "Not Found").build();
			}

			subject.checkRole(id);
			if (!getApiManager().existsSpec(id)) {
				return Response.status(409).entity("API does not exists (create the API first).").build();
			}
			Set<String> alias = new HashSet<String>(Arrays.asList(StringUtils.split(body.trim(), "\n")));
			AliasUtils.test(alias);
			getApiManager().createAlias(id, alias);
			ResponseBuilder builder;
			JsonObject m = new JsonObject();
			URI aliasUri = requestUri.getBaseUriBuilder().path(id).path("alias").build();
			m.add("message", new JsonPrimitive("Created: " + aliasUri));
			m.add("location", new JsonPrimitive(aliasUri.toString()));
			builder = Response.created(aliasUri);
			builder.entity(m.toString());
			addHeaders(builder, id);
			return builder.build();
		} catch (AuthorizationException e) {
			log.trace("Not authorized");
			return packError(Response.status(Status.FORBIDDEN), e).build();
		} catch (BadAliasException e) {
			log.error("An error occurred", e);
			return packError(Response.status(Status.BAD_REQUEST), e).build();
		} catch (Exception e) {
			log.error("An error occurred", e);
			return packError(Response.serverError(), e).build();
		}
	}

}
