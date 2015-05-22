package uk.ac.open.kmi.basil;

import com.wordnik.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.basil.doc.Doc;
import uk.ac.open.kmi.basil.doc.Doc.Field;
import uk.ac.open.kmi.basil.store.Store;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import java.io.IOException;

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
		log.trace("Calling GET. id={}",id);
		try {
			Store store = getDataStore();
			if (!store.existsSpec(id)) {
				return Response.status(404).build();
			}
			Doc doc = getDataStore().loadDoc(id);
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
    		@ApiResponse(code = 204, message = "Deleted. No content")
    })
	public Response delete(@PathParam("id") String id) {
		log.trace("Calling DELETE. id={}",id);
		try {
			Store store = getDataStore();
			if (!store.existsSpec(id)) {
				return Response.status(404).build();
			}
			boolean success = getDataStore().deleteDoc(id);
			ResponseBuilder builder;
			if(success){
				builder = Response.noContent();
			}else{
				builder = Response.serverError();
			}
			addHeaders(builder, id);
			return builder.build();
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
            @ApiResponse(code = 409, message = "API does not exists (create the API first).") ,
            @ApiResponse(code = 500, message = "Internal error") })
	public Response put(
            @ApiParam(value = "ID of the API specification", required = true)
            @PathParam("id") String id,
            @ApiParam(value = "Name of the API", required = false)
            @QueryParam("name") String name,
            @ApiParam(value = "Description of the API", required = false)
            String body) {
		log.trace("Calling PUT. id={} name={}",id, name);
		try {
			log.trace("Body is: {}",body);
			Store data = getDataStore();
			if(!data.existsSpec(id)){
				return Response.status(409).entity("API does not exists (create the API first).").build();
			}
			Doc doc = data.loadDoc(id);
			boolean created = true;
			if (!doc.isEmpty()) {
				created = false;
			}
			doc.set(Field.NAME, name);
			doc.set(Field.DESCRIPTION, body);
			data.saveDoc(id, doc);
			ResponseBuilder builder;
			if (created) {
				builder = Response.created(
						requestUri.getBaseUriBuilder().path(id).path(name).build())
						;
			} else {
				builder = Response.ok();
			}
			addHeaders(builder, id);
			return builder.build();
		} catch (Exception e) {
			return Response.serverError().entity(e).build();
		}
	}

}
