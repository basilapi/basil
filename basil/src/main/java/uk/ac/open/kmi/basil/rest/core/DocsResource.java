package uk.ac.open.kmi.basil.rest.core;

import java.io.IOException;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.subject.Subject;
import org.secnod.shiro.jaxrs.Auth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.open.kmi.basil.doc.Doc;
import uk.ac.open.kmi.basil.doc.Doc.Field;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

/**
 * 
 * @author enridaga
 *
 */
@Path("{id}/docs")
@Api(value = "/basil", description = "BASIL operations")
public class DocsResource extends AbstractResource {
    private Logger log = LoggerFactory.getLogger(DocsResource.class);

	@GET
	@Produces("text/plain")
	@ApiOperation(value = "API documentation")
    @ApiResponses(value = {
    		@ApiResponse(code = 200, message = "OK"),
    		@ApiResponse(code = 500, message = "Internal error") ,
    		@ApiResponse(code = 204, message = "No content")
    })
	public Response get(@PathParam("id") String id) {
		log.trace("Calling GET docs with id: {}",id);
		try {
			if (getApiManager().getSpecification(id) == null) {
				return Response.status(404).build();
			}
			Doc doc = getApiManager().getDoc(id);
			ResponseBuilder builder;
			if(doc.isEmpty()){
				builder = Response.noContent();
			}else{
				builder = Response.ok(doc.get(Field.DESCRIPTION));
				builder.header(Headers.Name, doc.get(Doc.Field.NAME));
			}
			addHeaders(builder, id);
			return builder.build();
		} catch (IOException e) {
			throw new WebApplicationException(e);
		}
	}

	@DELETE
	@ApiOperation(value = "Delete API documentation")
    @ApiResponses(value = {
    		@ApiResponse(code = 500, message = "Internal error") ,
    		@ApiResponse(code = 403, message = "Forbidden") ,
    		@ApiResponse(code = 204, message = "Deleted. No content")
    })
	public Response delete(@PathParam("id") String id, @Auth Subject subject) {
		log.trace("Calling DELETE docs with id: {}",id);
		try {
			subject.checkPermission(id + ":write");

			if (getApiManager().getSpecification(id) == null) {
				return Response.status(404).build();
			}
			boolean success = getApiManager().deleteDoc(id);
			ResponseBuilder builder;
			if(success){
				builder = Response.noContent();
			}else{
				builder = Response.serverError();
			}
			addHeaders(builder, id);
			return builder.build();
		} catch (AuthorizationException e) {
			log.trace("Not authorized");
			return Response.status(Status.FORBIDDEN).entity(e.getMessage()).build();
		} catch (IOException e) {
			throw new WebApplicationException(e);
		}
	}
	
	@PUT
	@Produces("text/plain")
    @ApiOperation(value = "To create a new doc file (plain text) and/or set a name for the API",
            notes = "The operation returns the resource URI of the doc file")
    @ApiResponses(value = { @ApiResponse(code = 400, message = "Body cannot be empty"),
            @ApiResponse(code = 201, message = "Doc file created"),
            @ApiResponse(code = 403, message = "Forbidden") ,
            @ApiResponse(code = 409, message = "API does not exists (create the API first).") ,
            @ApiResponse(code = 500, message = "Internal error") })
	public Response put(
            @ApiParam(value = "ID of the API specification", required = true)
            @PathParam("id") String id,
            @ApiParam(value = "Name of the API", required = false)
            @QueryParam("name") String name,
            @ApiParam(value = "Description of the API", required = false)
			String body,
			@Auth Subject subject
	) {
		log.trace("Calling PUT docs with id: {} name: {}",id, name);
		try {
			log.trace("Body is: {}",body);
			subject.checkRole(id);
			if (!getApiManager().existsSpec(id)) {
				return Response.status(409).entity("API does not exists (create the API first).").build();
			}
			if(name == null){
				name = getParameterOrHeader("name");
			}
			getApiManager().createDoc(id, name, body);
			ResponseBuilder builder;
			builder = Response.created(requestUri.getBaseUriBuilder().path(id).path("docs").build());
			addHeaders(builder, id);
			return builder.build();
		} catch (AuthorizationException e) {
			log.trace("Not authorized");
			return Response.status(Status.FORBIDDEN).entity(e.getMessage()).build();
		} catch (Exception e) {
			log.error("An error occurred",e);
			return Response.serverError().entity(e.getMessage()).build();
		}
	}

}
