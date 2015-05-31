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
	 * Method to generate API Ids.
	 * <p/>
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

	protected Response doPUT(String id, String body) {
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
	 * List stones
	 *
	 * @return
	 */
	@GET
	@Produces("text/plain")
	@ApiOperation(value = "Get the list of available API specification", response = List.class)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 500, message = "Internal error")}
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
}
