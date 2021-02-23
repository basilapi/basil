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
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import io.github.basilapi.basil.core.ApiInfo;
import org.apache.jena.query.QueryParseException;
import org.apache.shiro.subject.Subject;
import org.secnod.shiro.jaxrs.Auth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

import io.github.basilapi.basil.rest.auth.AuthResource;
import io.github.basilapi.basil.rest.msg.SimpleMessage;

@Path("/")
@Api(value = "/basil", description = "BASIL operations")
public class SpecificationResource extends AbstractResource {

	private static Logger log = LoggerFactory
			.getLogger(SpecificationResource.class);

	/**
	 * Creates a new API
	 *
	 * @param body
	 * @return
	 */
	@PUT
	@Produces("application/json")
	@ApiOperation(value = "Create a new API specification",
			notes = "The operation returns the resource URI of the API specification",
			response = URI.class)
	@ApiResponses(value = { @ApiResponse(code = 400, message = "Body cannot be empty"),
			@ApiResponse(code = 201, message = "Specification created"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal error") })
	public Response put(
			@ApiParam(value = "SPARQL Endpoint of the data source", required = false) @QueryParam(value = "endpoint") String endpoint,
			@ApiParam(value = "SPARQL query that defines the API specification", required = true) String body,
			@Auth Subject subject) {
		log.trace("Called PUT");
		try {
			if (isAuthenticated()) {
				String username = (String) subject.getSession().getAttribute(AuthResource.CURRENT_USER_KEY);
				endpoint = getParameterOrHeader("endpoint");
				if(log.isTraceEnabled()){
					log.trace("Creating Specification with: \n > Username: {}\n > Endpoint: {}\n > Query: \n\n{}\n\n", new Object[]{username, endpoint, body});
				}
				String id = getApiManager().createSpecification(username, endpoint, body);
				log.trace("Spec created: {}", id);
				URI api = requestUri.getBaseUriBuilder().path(id).build();

				ResponseBuilder response;
				URI spec = requestUri.getBaseUriBuilder().path(id).path("spec")
						.build();
				log.info("Created  spec at: {}", spec);
				response = Response.created(api).entity(new SimpleMessage("Created", api.toString()).asJSON());
				addHeaders(response, id);
				return response.build();
			}
		} catch (QueryParseException e) {
			log.error("An error occurred", e);
			return packError(Response.status(Status.BAD_REQUEST), e).build();

		} catch (Exception e) {
			log.error("An error occurred", e);
			return packError(Response.serverError(), e).build();
		}
		return packError(Response.status(HttpURLConnection.HTTP_FORBIDDEN), "User must be authenticated").build();
	}

	/**
	 * List APIs
	 *
	 * @return
	 * @throws IOException
	 */
	@GET
	@Produces("application/json")
	@ApiOperation(value = "Get the list of available API specifications", response = List.class)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 500, message = "Internal error") }
			)
			public Response list() {
		log.trace("Called GET");
		JsonArray r = new JsonArray();
		try {
			for (String api : getApiManager().listApis()) {
				JsonObject object = new JsonObject();
				ApiInfo info = getApiManager().getInfo(api);
				object.add("id", new JsonPrimitive(api));
				object.add("modified", new JsonPrimitive(info.modified().getTime()));
				object.add("name", new JsonPrimitive(String.valueOf(info.getName())));
				JsonArray array = new JsonArray();
				for(String s:info.alias()) {
					array.add(new JsonPrimitive(s));
				}
				object.add("alias", array);
				String c = getApiManager().getCreatorOfApi(api);
				if (c == null)
					c = "";
				object.add("createdBy", new JsonPrimitive(c)); // TODO
				// object.add("description", new
				// JsonPrimitive(doc.get(Field.DESCRIPTION)));
				object.add("location", new JsonPrimitive(String.valueOf(requestUri.getBaseUriBuilder().path(api))));
				r.add(object);
			}
		} catch (IOException e) {
			log.error("", e);
			return packError(Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR)
					, e).build();
		}
		return Response.ok(r.toString()).build();
	}
}
