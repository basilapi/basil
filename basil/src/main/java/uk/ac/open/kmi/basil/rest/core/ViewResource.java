package uk.ac.open.kmi.basil.rest.core;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.wordnik.swagger.annotations.*;
import org.apache.shiro.subject.Subject;
import org.secnod.shiro.jaxrs.Auth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.basil.view.Engine;
import uk.ac.open.kmi.basil.view.View;
import uk.ac.open.kmi.basil.view.Views;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import java.io.IOException;
import java.net.URI;
import java.util.List;

@Path("{id}/view")
@Api(value = "/basil", description = "BASIL operations")
public class ViewResource extends AbstractResource {
	private Logger log = LoggerFactory.getLogger(ViewResource.class);

	@PUT
	@Path("{name}")
	@Produces("application/json")
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
			String body,
			@Auth Subject subject) {
		try {
			subject.checkRole(id);
			Engine engine;
			// Content-type
			if (requestHeaders.getMediaType() == null) {
				engine = Engine.MUSTACHE;
			} else {
				String mediaType = requestHeaders.getMediaType().getType();
				engine = Engine.byContentType(mediaType);
				if (engine == null) {
					JsonObject m = new JsonObject();
					m.add("message", new JsonPrimitive("Unsupported media type: " + mediaType));
					m.add("unsupportedMediaType", new JsonPrimitive(mediaType));
					return Response.serverError()
							.entity(m.toString())
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
	@Produces("application/json")
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
			Gson gson = new Gson();
			return Response.ok(gson.toJson(views.getNames())).build();
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
	@Produces("application/json")
	@ApiOperation(value = "Delete API view")
    @ApiResponses(value = { @ApiResponse(code = 404, message = "API view not found"),
            @ApiResponse(code = 200, message = "API view deleted"),
            @ApiResponse(code = 500, message = "Internal error") })
	public Response delete(
            @ApiParam(value = "ID of the API specification", required = true)
            @PathParam("id") String id,
            @ApiParam(value = "Name of the view", required = true)
			@PathParam("name") String name,
			@Auth Subject subject) {
		try {
			subject.checkRole(id);
			getApiManager().deleteView(id, name);
			log.debug("View deleted: {}:{} ", id, name);
			URI view = requestUri.getBaseUriBuilder().path(id).path("view").path("name").build();
			JsonObject m = new JsonObject();
			m.add("message", new JsonPrimitive("View deleted: " + view.toString()));
			m.add("location", new JsonPrimitive(view.toString()));
			return Response.ok().entity(m.toString()).build();
		} catch (IOException e) {
			log.error("", e);
			return Response.serverError().build();
		}
	}
}
