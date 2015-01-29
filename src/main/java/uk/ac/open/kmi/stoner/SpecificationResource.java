package uk.ac.open.kmi.stoner;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.UUID;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.open.kmi.stoner.sparql.Specification;
import uk.ac.open.kmi.stoner.sparql.SpecificationFactory;
import uk.ac.open.kmi.stoner.store.Store;

@Path("/")
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
	public Response put(@QueryParam("endpoint") String endpoint, String body) {
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

			String endpoint = getStonerParameter(
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
		} catch (Exception e) {
			log.error("", e);
			return Response.serverError().entity(e.getMessage()).build();
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
	@Path("{id:([^/]+)}/spec")
	@Produces("text/plain")
	public Response replaceSpec(@PathParam(value = "id") String id, String body) {
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
	@Path("{id:([^/]+)}/spec")
	@Produces("text/plain")
	public Response getSpec(@PathParam(value = "id") String id) {
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
	 * Gets the stone (redirects to api).
	 * 
	 * @param id
	 * @return
	 */
	@GET
	@Path("{id:([^/]+)}")
	@Produces("text/plain")
	public Response getId(@PathParam(value = "id") String id) {
		log.trace("Called GET with id: {}", id);
		ResponseBuilder r = Response.seeOther(requestUri.getBaseUriBuilder()
				.path(id).path("api").build());
		addHeaders(r, id);
		return r.build();
	}

	/**
	 * Updates the spec of an API
	 * 
	 * @param id
	 * @return
	 */
	@POST
	@Path("{id:([^/]+)}/spec")
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
	@Path("{id:([^/]+)}/spec")
	@Produces("text/plain")
	public Response deleteSpec(@PathParam(value = "id") String id) {
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
