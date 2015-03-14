package uk.ac.open.kmi.basil;

import com.wordnik.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.basil.sparql.Specification;
import uk.ac.open.kmi.basil.sparql.SpecificationFactory;
import uk.ac.open.kmi.basil.store.Store;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.UUID;

@Path("/")
@Api(value = "/basil", description = "BASIL operations")
public class SpecificationResource extends AbstractResource {

	private static Logger log = LoggerFactory
			.getLogger(SpecificationResource.class);

	/**
	 * Creates a new Stone
	 * 
	 * @param body
	 * @return
	 */
	@PUT
	@Produces("text/plain")
    @ApiOperation(value = "Create a new API specification",
            notes = "The operation returns the resource URI of the API specification",
            response = URI.class)
    @ApiResponses(value = { @ApiResponse(code = 400, message = "Body cannot be empty"),
            @ApiResponse(code = 201, message = "Specification created"),
            @ApiResponse(code = 500, message = "Internal error") })
	public Response put(@ApiParam(value = "SPARQL endpoint of the data source")
                            @QueryParam("endpoint") String endpoint,
                        @ApiParam(value = "SPARQL query that defines the API specification", required = true)
                        String body) {
		log.trace("Called PUT");
		String id = shortUUID();
		return doPUT(id, body);
	}

	private Response doPUT(String id, String body) {
		try {
			if (body.equals("")) {
				return Response.status(HttpURLConnection.HTTP_BAD_REQUEST)
						.header(Headers.Error, "Body cannot be empty").build();
			}

			String endpoint = getParameterOrHeader(
					Headers.asParameter(Headers.Endpoint), true);

			Specification specification = SpecificationFactory.create(endpoint,
					body);
			Store data = getDataStore();
			boolean created = true;
			if (data.existsSpec(id)) {
				created = false;
			}
			try {
				data.saveSpec(id, specification);
			} catch (IOException e) {
				throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
			}

			URI api = requestUri.getBaseUriBuilder().path(id).build();

			ResponseBuilder response;
			if (created) {
				URI spec = requestUri.getBaseUriBuilder().path(id).path("spec")
						.build();
				log.info("Created  spec at: {}", spec);
				response = Response.created(api).entity(
						"Created: " + api.toString());
			} else {
				URI spec = requestUri.getBaseUriBuilder().path(id).path("spec").build();
				log.info("Replaced spec at: {}", spec);
				response = Response.ok(spec).entity(
						"Replaced: " + spec.toString() + "\n");
			}

			addHeaders(response, id);

			return response.build();
		} catch (WebApplicationException e) {
			log.error("", e);
			throw e;
		}
	}

	/**
	 * Replace the spec of an API with a new version.
	 * 
	 * @param id
	 * @param body
	 * @return
	 */
	@PUT
	@Path("{id:([^/]+)}")
	@Produces("text/plain")
    @ApiOperation(value = "Update existing API specification",
            response = URI.class)
    @ApiResponses(value = { @ApiResponse(code = 400, message = "Body cannot be empty"),
            @ApiResponse(code = 200, message = "Specification updated"),
            @ApiResponse(code = 500, message = "Internal error") })
	public Response replaceSpec(
            @ApiParam(value = "ID of the API specification", required = true)
            @PathParam(value = "id") String id,
            @ApiParam(value = "SPARQL query that substitutes the API specification", required = true)
            String body) {
		log.trace("Called PUT with id: {}", id);
		return doPUT(id, body);
	}

	/**
	 * Gets the spec of an API.
	 * 
	 * @param id
	 * @return
	 */
	@GET
	@Path("{id:([^/]+)} ")
	@Produces("text/plain")
    @ApiOperation(value = "Get the API specification")
    @ApiResponses(value = { @ApiResponse(code = 404, message = "Specification not found"),
            @ApiResponse(code = 200, message = "Specification found"),
            @ApiResponse(code = 500, message = "Internal error") })
	public Response getSpec(
            @ApiParam(value = "ID of the API specification", required = true)
            @PathParam(value = "id") String id) {
		log.trace("Called GET spec with id: {}", id);
		try {

			Store store = getDataStore();
			if (!store.existsSpec(id)) {
				return Response.status(Status.NOT_FOUND).build();
			}

			Specification spec = store.loadSpec(id);
			ResponseBuilder response = Response.ok();
			response.header(Headers.Endpoint, spec.getEndpoint());
			addHeaders(response, id);
			response.entity(spec.getQuery());

			return response.build();
		} catch (Exception e) {
			log.error("An error occurred", e);
			throw new WebApplicationException(Response
					.status(HttpURLConnection.HTTP_INTERNAL_ERROR)
					.entity(e.getMessage()).build());
		}
	}

	/**
	 * Delete an API
	 * 
	 * @param id
	 * @return
	 */
	@DELETE
	@Path("{id:([^/]+)}")
	@Produces("text/plain")
    @ApiOperation(value = "Delete API specification")
    @ApiResponses(value = { @ApiResponse(code = 404, message = "Specification not found"),
            @ApiResponse(code = 200, message = "Specification deleted"),
            @ApiResponse(code = 500, message = "Internal error") })
	public Response deleteSpec(
            @ApiParam(value = "ID of the API specification", required = true)
            @PathParam(value = "id") String id) {
		log.trace("Called DELETE spec with id: {}", id);

		// XXX Not Implemented
		return Response.status(501).entity("Not implemented yet\n").build();

	}

	/**
	 * List stones
	 * 
	 * @return
	 */
	@GET
	@Produces("text/plain")
    @ApiOperation(value = "Get the list of available API specification",response = List.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 500, message = "Internal error") }
            )
    public String list() {
		log.trace("Called GET");
		Store data = getDataStore();
		StringBuilder sb = new StringBuilder();
		for (String stone : data.listSpecs()) {
			sb.append(requestUri.getBaseUriBuilder().path(stone));
			sb.append("\n");
		}
		return sb.toString();
	}

	/**
	 * Method to generate API Ids.
	 * 
	 * XXX Not sure this is good, but it's an option - enridaga
	 * 
	 * @return
	 * @see https://gist.github.com/LeeSanghoon/5811136
	 */
	public static String shortUUID() {
		UUID uuid = UUID.randomUUID();
		long l = ByteBuffer.wrap(uuid.toString().getBytes()).getLong();
		return Long.toString(l, Character.MAX_RADIX);
	}
}
