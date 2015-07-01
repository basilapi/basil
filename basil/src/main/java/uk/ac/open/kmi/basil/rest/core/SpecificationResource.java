package uk.ac.open.kmi.basil.rest.core;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.wordnik.swagger.annotations.*;
import org.apache.shiro.subject.Subject;
import org.secnod.shiro.jaxrs.Auth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.basil.rest.auth.AuthResource;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.List;

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
            @ApiResponse(code = 500, message = "Internal error") })
	public Response put(
			@ApiParam(value = "SPARQL Endpoint of the data source", required = false)
			@QueryParam(value = "endpoint") String endpoint,
                        @ApiParam(value = "SPARQL query that defines the API specification", required = true)
						String body,
						@Auth Subject subject) {
		log.trace("Called PUT");
		try {
			if (subject.isAuthenticated()) {
				String username = (String) subject.getSession().getAttribute(AuthResource.CURRENT_USER_KEY);
				endpoint = getParameterOrHeader("endpoint");
				String id = getApiManager().createSpecification(username, endpoint, body);
				URI api = requestUri.getBaseUriBuilder().path(id).build();

				ResponseBuilder response;

				URI spec = requestUri.getBaseUriBuilder().path(id).path("spec")
						.build();
				log.info("Created  spec at: {}", spec);
				JsonObject m = new JsonObject();
				m.add("message", new JsonPrimitive("Created: " + api.toString()));
				m.add("location", new JsonPrimitive(api.toString()));
				response = Response.created(api).entity(m.toString());

				addHeaders(response, id);

				return response.build();
			}
		} catch (Exception e) {
			return Response.status(HttpURLConnection.HTTP_BAD_REQUEST)
					.header(Headers.Error, e.getMessage()).build();
		}
		return Response.status(HttpURLConnection.HTTP_FORBIDDEN)
				.header(Headers.Error, "User must be authenticated").build();
	}


	/**
	 * List APIs
	 *
	 * @return
	 */
	@GET
	@Produces("application/json")
	@ApiOperation(value = "Get the list of available API specifications", response = List.class)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 500, message = "Internal error")}
	)
	public String list() {
		log.trace("Called GET");
		JsonArray r = new JsonArray();
		for (String api : getApiManager().listApis()) {
			r.add(new JsonPrimitive(String.valueOf(requestUri.getBaseUriBuilder().path(api))));
		}
		return r.toString();
	}
}
