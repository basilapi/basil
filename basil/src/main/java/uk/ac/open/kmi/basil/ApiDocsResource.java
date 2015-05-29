package uk.ac.open.kmi.basil;

import com.wordnik.swagger.annotations.*;
import org.json.simple.JSONObject;
import uk.ac.open.kmi.basil.doc.Doc;
import uk.ac.open.kmi.basil.sparql.Specification;
import uk.ac.open.kmi.basil.store.Store;
import uk.ac.open.kmi.basil.swagger.SwaggerJsonBuilder;
import uk.ac.open.kmi.basil.swagger.SwaggerUIBuilder;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import java.io.IOException;


@Path("{id}/api-docs")
@Api(value = "/basil", description = "BASIL operations")
public class ApiDocsResource extends AbstractResource {
	
	@GET
	@Produces({"application/json", "text/html", "*/*"})
	@ApiOperation(value = "Get API Swagger Description")
	@ApiResponses(value = {
    		@ApiResponse(code = 200, message = "OK"),
    		@ApiResponse(code = 500, message = "Internal error") 
    })
	public Response get(@PathParam("id") String id,
						@ApiParam(value = "Accepted Media Type", allowableValues = "application/json,text/html")
						@HeaderParam("Accept") String accept) {
		try {
			if (!isValidId(id)) {
				return Response.status(400).build();
			}
			Store store = getDataStore();
			if (!store.existsSpec(id)) {
				return Response.status(404).build();
			}
			 if (accept.contains("text/html")) {
				String msg = SwaggerUIBuilder.build(requestUri);
				ResponseBuilder builder = Response.ok(msg);
				addHeaders(builder, id);
				return builder.build();
			}else if (accept.contains("application/json") || accept.contains("*/*")) {
				Specification specification = store.loadSpec(id);
				Doc docs = store.loadDoc(id);
				JSONObject o = SwaggerJsonBuilder.build(id, specification, docs, requestUri.getBaseUri().toString());
				ResponseBuilder builder = Response.ok(o.toJSONString());
				addHeaders(builder, id);
				return builder.build();
			} 
			return Response.status(406).build();
		} catch (IOException e) {
			throw new WebApplicationException(e);
		}
	}
}
