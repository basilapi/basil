package uk.ac.open.kmi.basil;

import java.io.IOException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.json.simple.JSONObject;

import uk.ac.open.kmi.basil.doc.Doc;
import uk.ac.open.kmi.basil.sparql.Specification;
import uk.ac.open.kmi.basil.store.Store;
import uk.ac.open.kmi.basil.swagger.SwaggerJsonBuilder;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;


@Path("{id:([^/]+)}/api-docs")
@Api(value = "/basil", description = "BASIL operations")
public class ApiDocsResource extends AbstractResource {
	
	@GET
	@Produces("application/json")
	@ApiOperation(value = "Generates API Swagger Description")
    @ApiResponses(value = {
    		@ApiResponse(code = 200, message = "OK"),
    		@ApiResponse(code = 500, message = "Internal error") 
    })
	public Response get(@PathParam("id") String id) {
		try {
			Store store = getDataStore();
			if (!store.existsSpec(id)) {
				return Response.status(404).build();
			}

			Specification specification = store.loadSpec(id);
			Doc docs = store.loadDoc(id);
			JSONObject o = SwaggerJsonBuilder.build(id, specification, docs, requestUri.getBaseUri().toString());
			ResponseBuilder builder = Response.ok(o.toJSONString());
			addHeaders(builder, id);
			return builder.build();
		} catch (IOException e) {
			throw new WebApplicationException(e);
		}
	}
}
