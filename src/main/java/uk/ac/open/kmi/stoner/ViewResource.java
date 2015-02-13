package uk.ac.open.kmi.stoner;

import java.io.IOException;

import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.open.kmi.stoner.store.Store;
import uk.ac.open.kmi.stoner.view.Engine;
import uk.ac.open.kmi.stoner.view.View;
import uk.ac.open.kmi.stoner.view.Views;

@Path("{id:([^/]+)}/view")
public class ViewResource extends ApiResource {
	private Logger log = LoggerFactory.getLogger(ViewResource.class);

	@PUT
	@Path("{name:([^/]+)}")
	@Produces("text/plain")
	public Response put(@PathParam("id") String id,
			@QueryParam("type") @DefaultValue("text/html") String type,
			@PathParam("name") String name, String body) {
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
			Views views = data.loadViews(id);
			boolean created = true;
			if (views.exists(name)) {
				created = false;
			}
			views.put(type, name, body, engine);
			data.saveViews(id, views);
			if (created) {
				return Response.created(
						requestUri.getBaseUriBuilder().path(name).build())
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
	public Response listViews(@PathParam("id") String id) {
		try {
			Views views = getDataStore().loadViews(id);
			if (views.numberOf() == 0) {
				return Response.noContent().build();
			}
			StringBuilder sb = new StringBuilder();
			for (String ext : views.getNames()) {
				sb.append(ext).append("\n");
			}
			return Response.ok(sb.toString()).build();
		} catch (Exception e) {
			return Response.serverError().entity(e.getMessage()).build();
		}
	}

	@GET
	@Path("{name:([^/]+)}")
	public Response spec(@PathParam("id") String id,
			@PathParam("name") String name) {
		try {
			Views views = getDataStore().loadViews(id);
			View view = views.byName(name);
			if (view == null) {
				return Response.status(404).entity("Not found").build();
			}
			ResponseBuilder builder = Response.ok(view.getTemplate());
			addHeaders(builder, id);
			builder.header("Content-type", view.getEngine().getContentType());
			builder.header(Headers.Type, view.getMimeType());
			return builder.build();
		} catch (Exception e) {
			return Response.serverError().entity(e).build();
		}
	}

	@DELETE
	@Path("{name:([^/]+)}")
	public Response delete(@PathParam("id") String id,
			@PathParam("name") String name) {
		Views views;
		try {
			views = getDataStore().loadViews(id);
			views.remove(name);
			getDataStore().saveViews(id, views);
			log.debug("View deleted: {}:{} ", id, name);
			return Response.noContent().build();
		} catch (IOException e) {
			log.error("", e);
			return Response.serverError().build();
		}
	}
}
