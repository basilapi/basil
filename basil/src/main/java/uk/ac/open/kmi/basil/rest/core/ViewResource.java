package uk.ac.open.kmi.basil;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.wordnik.swagger.annotations.*;
import org.apache.shiro.subject.Subject;
import org.secnod.shiro.jaxrs.Auth;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.open.kmi.basil.view.Engine;
import uk.ac.open.kmi.basil.view.View;
import uk.ac.open.kmi.basil.view.Views;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

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
			JsonArray arr = new JsonArray();
			for(String n : views.getNames()){
				View v = views.byName(n);
				JsonObject o = new JsonObject();
				o.add("id", new JsonPrimitive(n));
				o.add("extension", new JsonPrimitive(n));
				o.add("engine", new JsonPrimitive(v.getEngine().name()));
				o.add("Content-Type", new JsonPrimitive(v.getMimeType()));
				arr.add(o);
			}
			return Response.ok(arr.toString()).build();
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
