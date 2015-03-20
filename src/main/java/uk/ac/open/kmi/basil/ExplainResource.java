package uk.ac.open.kmi.basil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import uk.ac.open.kmi.basil.sparql.QueryParameter;
import uk.ac.open.kmi.basil.sparql.Specification;
import uk.ac.open.kmi.basil.sparql.VariablesBinder;
import uk.ac.open.kmi.basil.store.Store;

import com.hp.hpl.jena.query.Query;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

@Path("{id:([^/]+)}/explain")
@Api(value = "/basil", description = "BASIL operations")
public class ExplainResource extends AbstractResource {

	@GET
	@Produces("text/plain")
	@ApiOperation(value = "Explain API invokation")
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
			VariablesBinder binder = new VariablesBinder(specification);

			List<String> missing = new ArrayList<String>();
			for (QueryParameter qp : specification.getParameters()) {
				if (requestUri.getQueryParameters().containsKey(qp.getName())) {
					List<String> values = requestUri.getQueryParameters().get(qp.getName());
					binder.bind(qp.getName(), values.get(0));
				} else if (!qp.isOptional()) {
					missing.add(qp.getName());
				}
			}

			Query q = binder.toQuery();
			ResponseBuilder builder = Response.ok(q.toString());
			addHeaders(builder, id);
			builder.header(Headers.Endpoint, specification.getEndpoint());
			return builder.build();
		} catch (IOException e) {
			throw new WebApplicationException(e);
		}
	}
}
