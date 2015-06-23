package uk.ac.open.kmi.basil;

import com.wordnik.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.basil.core.exceptions.SpecificationParsingException;

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

		try {
			String id = getApiManager().createSpecification(endpoint, body);
			URI api = requestUri.getBaseUriBuilder().path(id).build();

			ResponseBuilder response;

			URI spec = requestUri.getBaseUriBuilder().path(id).path("spec")
						.build();
			log.info("Created  spec at: {}", spec);
			response = Response.created(api).entity(
						"Created: " + api.toString());

			addHeaders(response, id);

			return response.build();
		} catch (SpecificationParsingException e) {
			return Response.status(HttpURLConnection.HTTP_BAD_REQUEST)
					.header(Headers.Error, e.getMessage()).build();
		}

	}


	/**
	 * List APIs
	 *
	 * @return
	 */
	@GET
	@Produces("text/plain")
	@ApiOperation(value = "Get the list of available API specifications", response = List.class)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 500, message = "Internal error")}
	)
	public String list() {
		log.trace("Called GET");
		StringBuilder sb = new StringBuilder();
		for (String api : getApiManager().listApis()) {
			sb.append(requestUri.getBaseUriBuilder().path(api));
			sb.append("\n");
		}
		return sb.toString();
	}
}
