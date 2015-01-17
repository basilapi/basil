package uk.ac.open.kmi.stoner;

import java.nio.ByteBuffer;
import java.util.UUID;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/")
public class SpecificationResource {

	private static Logger log = LoggerFactory
			.getLogger(SpecificationResource.class);

	@Context
	HttpHeaders requestHeaders;

	@Context
	UriInfo requestUri;

	/**
	 * Creates a new API
	 * 
	 * @param body
	 * @return
	 */
	@PUT
	@Produces("text/plain")
	public Response put(String body) {
		log.trace("Called PUT");

		// If body is empty
		if (body.equals("")) {
			return Response.serverError()
					.header(Headers.Error, "Body cannot be empty").build();
		}
		
		// XXX Not Implemented
		return Response.status(501).entity("Not implemented yet\n").build();
		
//		return Response.created(
//				URI.create(requestUri.getBaseUri() + shortUUID())).build();
	}

	/**
	 * Replace the spec of an API with a new version.
	 * 
	 * @param id
	 * @param body
	 * @return
	 */
	@PUT
	@Path("{id:(.+)}/spec")
	@Produces("text/plain")
	public Response replaceSpec(@PathParam(value = "id") String id, String body) {
		log.trace("Called PUT with id: {}", id);

		// XXX Not Implemented
		return Response.status(501).entity("Not implemented yet\n").build();
		
	}
	
	/**
	 * Gets the spec of an API.
	 * 
	 * @param id
	 * @return
	 */
	@GET
	@Path("{id:(.+)}/spec")
	@Produces("text/plain")
	public Response getSpec(@PathParam(value = "id") String id) {
		log.trace("Called GET spec with id: {}", id);

		// XXX Not Implemented
		return Response.status(501).entity("Not implemented yet\n").build();
		
	}
	
	/**
	 * Updates the spec of an API
	 * @param id
	 * @return
	 */
	@POST
	@Path("{id:(.+)}/spec")
	@Produces("text/plain")
	public Response updateSpec(@PathParam(value = "id") String id) {
		log.trace("Called POST spec with id: {}", id);

		// XXX Not Implemented
		return Response.status(501).entity("Not implemented yet\n").build();
		
	}
	
	/**
	 * Delete an API
	 * 
	 * @param id
	 * @return
	 */
	@DELETE
	@Path("{id:(.+)}/spec")
	@Produces("text/plain")
	public Response deleteSpec(@PathParam(value = "id") String id) {
		log.trace("Called DELETE spec with id: {}", id);

		// XXX Not Implemented
		return Response.status(501).entity("Not implemented yet\n").build();
		
	}

	/**
	 * List APIs
	 * 
	 * @return
	 */
	@GET
	@Produces("text/plain")
	public Response list() {
		log.trace("Called GET");
		
		// XXX Not Implemented
		return Response.status(501).entity("Not implemented yet\n").build();
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
