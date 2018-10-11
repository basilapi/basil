package uk.ac.open.kmi.basil.rest.core;

import java.io.IOException;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Variant;

import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.subject.Subject;
import org.secnod.shiro.jaxrs.Auth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.jena.query.QueryParseException;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

import uk.ac.open.kmi.basil.core.InvocationResult;
import uk.ac.open.kmi.basil.core.exceptions.ApiInvocationException;
import uk.ac.open.kmi.basil.core.exceptions.SpecificationParsingException;
import uk.ac.open.kmi.basil.doc.Doc.Field;
import uk.ac.open.kmi.basil.rendering.CannotRenderException;
import uk.ac.open.kmi.basil.rendering.MoreMediaType;
import uk.ac.open.kmi.basil.rendering.Renderer;
import uk.ac.open.kmi.basil.rendering.RendererFactory;
import uk.ac.open.kmi.basil.rest.auth.AuthResource;
import uk.ac.open.kmi.basil.rest.msg.SimpleMessage;
import uk.ac.open.kmi.basil.sparql.Specification;
import uk.ac.open.kmi.basil.view.Items;
import uk.ac.open.kmi.basil.view.View;
import uk.ac.open.kmi.basil.view.Views;

@Path("{id}")
@Api(value = "/basil", description = "BASIL operations")
public class ApiResource extends AbstractResource {

	private static Logger log = LoggerFactory.getLogger(ApiResource.class);

	@SuppressWarnings("unchecked")
	private Response performQuery(String id, MultivaluedMap<String, String> parameters, String extension) {
		log.trace("API execution.");
		try {
			InvocationResult r = getApiManager().invokeApi(id, parameters);
			MediaType type = null;
			// If we have an extension
			if (!extension.equals("")) {
				// remove dot
				extension = extension.substring(1);
				type = getFromExtension(extension);
				log.trace("API execution. Ext: {} Type: {}", extension, type);
				// No extension, check if the extension is the name of a view
				if (type == null) {
					Views views = getApiManager().listViews(id);
					if (views.exists(extension)) {

						View view = views.byName(extension);
						log.trace("API execution. View: {}", view);
						StringWriter writer = new StringWriter();
						Items data = null;
						if (r.getResult() instanceof ResultSet) {
							data = Items.create((ResultSet) r.getResult());
						} else if (r.getResult() instanceof Model) {
							data = Items.create((((Model) r.getResult()).getGraph().find(null, null, null)));
						} else if (r.getResult() instanceof Boolean) {
							data = Items.create((Boolean) r.getResult());
						} else if (r.getResult() instanceof List) {
							data = Items.create((List<Map<String, String>>) r.getResult());
						}
						view.getEngine().exec(writer, view.getTemplate(), data);

						// Yeah!
						ResponseBuilder rb = Response.ok(writer.toString());
						addHeaders(rb, id);
						rb.header("Content-Type", view.getMimeType() + "; charset=utf-8");
						log.trace("API execution. View executed. Returning response.");
						return rb.build();
					}
				}
				// Still found nothing?
				if (type == null) {
					log.trace("API execution. View not found.");
					return packError(Response.status(404), "Not found\n").build();
				}
			}
			// No extension, look for best acceptable
			if (type == null) {
				type = getBestAcceptable();
			}
			// No best acceptable
			if (type == null) {
				log.trace("API execution. Not acceptable.");
				return buildNotAcceptable();
			}
			log.trace("API execution. Prepare response.");
			Map<String, String> prefixMap = r.getQuery().getPrefixMapping().getNsPrefixMap();
			Renderer<?> renderer = RendererFactory.getRenderer(r.getResult());
			ResponseBuilder rb = Response.ok()
					.entity(renderer.stream(type, requestUri.getRequestUri().toString(), prefixMap));
			addHeaders(rb, id);
			rb.header("Content-Type", type.withCharset("UTF-8").toString());
			log.trace("API execution. Return response.");
			return rb.build();
		} catch (CannotRenderException e) {
			log.trace("API execution. Cannot prepare response (not acceptable).");
			return buildNotAcceptable();
		} catch (Exception e) {
			// Response r =
			// Response.serverError().entity(e.getMessage()).build();
			log.error("ERROR while query execution", e);
			return packError(Response.serverError(), e).build();
		}
	}

	private Response buildNotAcceptable() {
		return packError(Response.notAcceptable(Variant.mediaTypes(MediaType.TEXT_PLAIN_TYPE).add().build()),
				"Not acceptable").build();
	}

	public MediaType getFromExtension(String ext) {
		if (MoreMediaType.extensions.containsKey(ext)) {
			return MoreMediaType.extensions.get(ext);
		} else {
			return null;
		}
	}

	/**
	 * 
	 * @return null if none of the exisitng variants are acceptable
	 */
	private MediaType getBestAcceptable() {
		// This list is sorted by the client preference
		List<MediaType> acceptHeaders = requestHeaders.getAcceptableMediaTypes();
		log.debug("Acceptable media types: {}", acceptHeaders);
		if (acceptHeaders == null || acceptHeaders.size() == 0) {
			// Default type is text/plain
			return MediaType.TEXT_PLAIN_TYPE;
		}

		for (MediaType mt : acceptHeaders) {
			String qValue = mt.getParameters().get("q");
			if (qValue != null && Double.valueOf(qValue).doubleValue() == 0.0) {
				break;
			}

			for (MediaType variant : MoreMediaType.MediaTypes) {
				if (variant.isCompatible(mt)) {
					return variant;
				}
			}

		}
		return null;
	}

	@Path("api{ext}")
	@POST
	@Consumes("application/x-www-form-urlencoded")
	@ApiOperation(value = "Invoke API")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 500, message = "Internal error") })
	public Response postExt(
			@ApiParam(value = "ID of the API specification", required = true) @PathParam("id") String id,
			@ApiParam(value = "Extension of the output data format (e.g., .json, .xml)") @PathParam("ext") String extension,
			@ApiParam(value = "Input parameters") MultivaluedMap<String, String> form) {
		log.trace("Called POST with extension.");
		return performQuery(id, form, extension);
	}

	@Path("api{ext}")
	@GET
	@ApiOperation(value = "Invoke API with a specific extension")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 500, message = "Internal error") })
	public Response getExt(@ApiParam(value = "ID of the API specification", required = true) @PathParam("id") String id,
			@ApiParam(value = "Extension of the output data format (e.g., .json, .xml)", required = false) @PathParam("ext") String extension) {
		log.trace("Called GET with extension.");
		return performQuery(id, requestUri.getQueryParameters(), extension);
	}

	@Path("api")
	@GET
	@ApiOperation(value = "Invoke API")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 500, message = "Internal error") })
	public Response get(@ApiParam(value = "ID of the API specification", required = true) @PathParam("id") String id) {
		log.trace("Called GET /api");
		return performQuery(id, requestUri.getQueryParameters(), "");
	}

	@Path("direct")
	@GET
	@ApiOperation(value = "Returns a 303 redirect, querying the endpoint")
	@ApiResponses(value = { @ApiResponse(code = 303, message = "See other"),
			@ApiResponse(code = 500, message = "Internal error") })
	public Response direct(
			@ApiParam(value = "ID of the API specification", required = true) @PathParam("id") String id) {
		log.trace("Called GET /direct");
		try {
			String s = getApiManager().redirectUrl(id, requestUri.getQueryParameters());
			ResponseBuilder response;
			response = Response.seeOther(URI.create(s));
			addHeaders(response, id);
			return response.build();
		} catch (IOException | ApiInvocationException e) {
			log.error("Error", e);
			return packError(Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR), e).build();
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
	@Path("spec")
	@Produces("application/json")
	@ApiOperation(value = "Update existing API specification", response = URI.class)
	@ApiResponses(value = { @ApiResponse(code = 400, message = "Body cannot be empty"),
			@ApiResponse(code = 200, message = "Specification updated"),
			@ApiResponse(code = 403, message = "Forbidden"), @ApiResponse(code = 500, message = "Internal error") })
	public Response replaceSpec(
			@ApiParam(value = "ID of the API specification", required = true) @PathParam(value = "id") String id,
			@ApiParam(value = "SPARQL Endpoint", required = false) @QueryParam(value = "endpoint") String endpoint,
			@ApiParam(value = "SPARQL query that substitutes the API specification", required = true) String body,
			@Auth Subject subject) {
		log.trace("Called PUT with id: {}", id);
		try {
			if(!isAuthenticated()){
				throw new AuthorizationException("Not authenticated");
			}
			subject.checkRole(id);
			getApiManager().replaceSpecification(id, body);
			endpoint = getParameterOrHeader("endpoint");
			getApiManager().replaceSpecification(id, endpoint, body);
			ResponseBuilder response;
			URI spec = requestUri.getBaseUriBuilder().path(id).path("spec").build();
			log.info("Replaced spec at: {}", spec);
			response = Response.ok(spec).entity(new SimpleMessage("Replaced", spec.toString()).asJSON());
			addHeaders(response, id);
			return response.build();
		} catch (AuthorizationException e) {
			log.trace("Not authorized");
			return packError(Response.status(Status.FORBIDDEN), e).build();
		} catch (QueryParseException | SpecificationParsingException e) {
			return packError(Response.status(HttpURLConnection.HTTP_BAD_REQUEST), e).build();
		} catch (Exception e) {
			return packError(Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR), e).build();
		}
	}

	/**
	 * Redirect to /spec
	 *
	 * @param id
	 * @return
	 */
	@GET
	public Response redirectToSpec(@PathParam(value = "id") String id) {
		// If requests HTML, go to API Docs instead
		ResponseBuilder builder = Response.status(303);
		addHeaders(builder, id);
		if (requestHeaders.getAcceptableMediaTypes().contains(MediaType.TEXT_HTML_TYPE)) {
			return builder.location(requestUri.getBaseUriBuilder().path(id).path("api-docs").build()).build();
		}
		return builder.location(requestUri.getBaseUriBuilder().path(id).path("spec").build()).build();
	}

	/**
	 * Gets the spec of an API.
	 *
	 * @param id
	 * @return
	 */
	@GET
	@Path("spec")
	@Produces("text/plain")
	@ApiOperation(value = "Get the API specification")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "Specification not found"),
			@ApiResponse(code = 200, message = "Specification found"),
			@ApiResponse(code = 500, message = "Internal error") })
	public Response getSpec(
			@ApiParam(value = "ID of the API specification", required = true) @PathParam(value = "id") String id) {
		log.trace("Called GET spec with id: {}", id);
		try {

			Specification spec = getApiManager().getSpecification(id);
			if (spec == null) {
				return Response.status(Response.Status.NOT_FOUND).build();
			}
			ResponseBuilder response = Response.ok();
			response.header(Headers.Name, getApiManager().getDoc(id).get(Field.NAME));
			response.header(Headers.Endpoint, spec.getEndpoint());
			addHeaders(response, id);
			response.entity(spec.getQuery());

			return response.build();
		} catch (Exception e) {
			log.error("An error occurred", e);
			return packError(Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR), e).build();
		}
	}

	/**
	 * Delete an API
	 *
	 * @param id
	 * @return
	 */
	@DELETE
	@Produces("application/json")
	@ApiOperation(value = "Delete API specification")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "Specification not found"),
			@ApiResponse(code = 200, message = "Specification deleted"),
			@ApiResponse(code = 403, message = "Forbidden"), @ApiResponse(code = 500, message = "Internal error") })
	public Response deleteSpec(
			@ApiParam(value = "ID of the API specification", required = true) @PathParam(value = "id") String id,
			@Auth Subject subject) {
		log.trace("Called DELETE spec with id: {}", id);
		try {
			if(!isAuthenticated()){
				throw new AuthorizationException("Not authenticated");
			}
			subject.checkRole(id);
			getApiManager().deleteApi(id);
			URI spec = requestUri.getBaseUriBuilder().path(id).path("spec").build();
			ResponseBuilder response;
			response = Response.ok().entity(new SimpleMessage("Deleted", spec.toString()).asJSON());
			addHeaders(response, id);
			return response.build();
		} catch (AuthorizationException e) {
			log.trace("Not authorized");
			return packError(Response.status(Status.FORBIDDEN), e).build();
		} catch (Exception e) {
			return packError(Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR), e).build();
		}

	}

	/**
	 * Gets a new clone of an API.
	 *
	 * @param id
	 * @return
	 */
	@GET
	@Path("clone")
	@Produces("application/json")
	@ApiOperation(value = "Get a clone of an API")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "Specification not found"),
			@ApiResponse(code = 200, message = "API clones"), @ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal error") })
	public Response getClone(
			@ApiParam(value = "ID of the API specification", required = true) @PathParam(value = "id") String id,
			@Auth Subject subject) {
		log.trace("Called GET clone with id: {}", id);
		try {
			if (isAuthenticated()) {
				String username = (String) subject.getSession().getAttribute(AuthResource.CURRENT_USER_KEY);
				String newId = getApiManager().cloneSpecification(username, id);
				if (newId == null) {
					return Response.status(Response.Status.NOT_FOUND).build();
				}
				ResponseBuilder response;
				URI spec = requestUri.getBaseUriBuilder().path(newId).path("spec").build();
				log.info("Cloned spec at: {}", spec);
				response = Response.ok(spec).entity(new SimpleMessage("Cloned", spec.toString()).asJSON());
				addHeaders(response, newId);
				return response.build();
			}
		} catch (Exception e) {
			log.error("An error occurred", e);
			return packError(Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR), e).build();
		}
		return packError(Response.status(HttpURLConnection.HTTP_FORBIDDEN), "User must be authenticated").build();
	}
}
