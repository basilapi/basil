package uk.ac.open.kmi.stoner;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import uk.ac.open.kmi.stoner.format.Engine;
import uk.ac.open.kmi.stoner.format.Format;
import uk.ac.open.kmi.stoner.format.Formats;
import uk.ac.open.kmi.stoner.store.Store;

@Path("{id:([^/]+)}/format")
public class FormatResource extends AbstractResource {

	@PUT
	@Path("{extension:([^/]+)}")
	@Produces("text/plain")
	public Response put(@PathParam("id") String id,
			@QueryParam("type") String type,
			@PathParam("extension") String extension, String body) {
		try {
			Engine engine;
			// Content-type
			if (requestHeaders.getMediaType() == null) {
				engine = Engine.MUSTACHE;
			} else {
				String mediaType = requestHeaders.getMediaType().getType();
				engine = Engine.byContentType(mediaType);
				if (engine == null) {
					return Response.serverError()
							.entity("Unsupported content type: " + mediaType)
							.build();
				}
			}

			Store data = getDataStore();
			Formats formats = data.loadFormats(id);
			boolean created = true;
			if (formats.supportsExtension(extension)) {
				created = false;
			}
			formats.put(type, extension, body, engine);
			data.saveFormats(id, formats);
			if (created) {
				return Response.created(
						requestUri.getBaseUriBuilder().path(extension).build())
						.build();
			} else {
				return Response.ok().build();
			}
		} catch (Exception e) {
			return Response.serverError().entity(e).build();
		}
	}

	@GET
	@Produces("text/plain")
	public Response listFormats(@PathParam("id") String id) {
		try {
			Formats formats = getDataStore().loadFormats(id);
			if(formats.numberOf() == 0){
				return Response.noContent().build();
			}
			StringBuilder sb = new StringBuilder();
			for (String ext : formats.getExtensions()) {
				sb.append(ext).append("\n");
			}
			return Response.ok(sb.toString()).build();
		} catch (Exception e) {
			return Response.serverError().entity(e.getMessage()).build();
		}
	}

	@GET
	@Path("{extension:([^/]+)}")
	public Response get(@PathParam("id") String id,
			@PathParam("extension") String extension) {
		try {
			Formats formats = getDataStore().loadFormats(id);
			Format format = formats.byExtension(extension);
			if (format == null) {
				return Response.status(404).entity("Not found").build();
			}
			ResponseBuilder builder = Response.ok(format.getTemplate());
			addHeaders(builder, id);
			builder.header("Content-type", format.getEngine().getContentType());
			builder.header(Headers.Type, format.getMimeType());
			return builder.build();
		} catch (Exception e) {
			return Response.serverError().entity(e).build();
		}
	}
}
