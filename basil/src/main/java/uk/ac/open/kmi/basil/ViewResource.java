package uk.ac.open.kmi.basil;

import com.wordnik.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.basil.view.Engine;
import uk.ac.open.kmi.basil.view.View;
import uk.ac.open.kmi.basil.view.Views;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import java.io.IOException;
import java.util.List;

@Path("{id}/view")
@Api(value = "/basil", description = "BASIL operations")
public class ViewResource extends AbstractResource {
	private Logger log = LoggerFactory.getLogger(ViewResource.class);

	@PUT
	@Path("{name}")
	@Produces("text/plain")
    @ApiOperation(value = "Create a new API view",
            notes = "The operation returns the resource URI of the API view")
    @ApiResponses(value = { @ApiResponse(code = 400, message = "Body cannot be empty"),
            @ApiResponse(code = 201, message = "View created"),
            @ApiResponse(code = 500, message = "Internal error") })
	public Response put(
            @ApiParam(value = "ID of the API specification", required = true)
            @PathParam("id") String id,
            @ApiParam(value = "Media type of the view output (e.g., text/html)", required = true)
			@QueryParam("type") @DefaultValue("text/html") String type,
            @ApiParam(value = "Name of the view", required = true)
            @PathParam("name") String name,
            @ApiParam(value = "Media type of the view", required = true)
            @HeaderParam("Content-Type") String contentType,
            @ApiParam(value = "Template of the view", required = true)
            String body) {
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

			Views views = getApiManager().listViews(id);
			boolean created = true;
			if (views.exists(name)) {
				created = false;
			}
			getApiManager().createView(id, type, name, body, engine);
			if (created) {
				return Response.created(
						requestUri.getBaseUriBuilder().path(id).path(name).build())
						.build();
			} else {
				return Response.ok().build();
			}
		} catch (Exception e) {
			return Response.serverError().entity(e).build();
		}
	}

	@GET
	@Produces({"text/plain", "text/html"})
    @ApiOperation(value = "Get the list of available views of an API",response = List.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 500, message = "Internal error") }
    )
    public Response listViews(
            @ApiParam(value = "ID of the API specification", required = true)
            @PathParam("id") String id) {
		try {
			Views views = getApiManager().listViews(id);
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
	@Path("{name}")
	@ApiOperation(value = "See an API view")
    @ApiResponses(value = { @ApiResponse(code = 404, message = "API view not found"),
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 500, message = "Internal error") })
    public Response spec(
            @ApiParam(value = "ID of the API specification", required = true)
            @PathParam("id") String id,
            @ApiParam(value = "Name of the view", required = true)
			@PathParam("name") String name) {
		try {
			View view = getApiManager().getView(id, name);
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
	@Path("{name}")
	@ApiOperation(value = "Delete API view")
    @ApiResponses(value = { @ApiResponse(code = 404, message = "API view not found"),
            @ApiResponse(code = 200, message = "API view deleted"),
            @ApiResponse(code = 500, message = "Internal error") })
	public Response delete(
            @ApiParam(value = "ID of the API specification", required = true)
            @PathParam("id") String id,
            @ApiParam(value = "Name of the view", required = true)
			@PathParam("name") String name) {
		try {
			getApiManager().deleteView(id, name);
			log.debug("View deleted: {}:{} ", id, name);
			return Response.noContent().build();
		} catch (IOException e) {
			log.error("", e);
			return Response.serverError().build();
		}
	}
}
