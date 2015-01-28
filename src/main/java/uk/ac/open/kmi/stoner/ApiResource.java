package uk.ac.open.kmi.stoner;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.jena.riot.writer.NTriplesWriter;

import uk.ac.open.kmi.stoner.sparql.QueryParameter;
import uk.ac.open.kmi.stoner.sparql.Specification;
import uk.ac.open.kmi.stoner.sparql.VariablesBinder;
import uk.ac.open.kmi.stoner.store.Store;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;

@Path("{id:([^/]+)}/api")
public class ApiResource extends AbstractResource {

	private Response performQuery(String id,
			MultivaluedMap<String, String> parameters) {
		try {
			Store store = getDataStore();
			if (!store.existsSpec(id)) {
				return Response.status(404).build();
			}

			Specification specification = store.loadSpec(id);
			VariablesBinder binder = new VariablesBinder(specification);

			List<String> missing = new ArrayList<String>();
			for (QueryParameter qp : specification.getParameters()) {
				if (parameters.containsKey(qp.getName())) {
					List<String> values = parameters.get(qp.getName());
					binder.bind(qp.getName(), values.get(0));
				} else if (!qp.isOptional()) {
					missing.add(qp.getName());
				}
			}

			if (!missing.isEmpty()) {
				StringBuilder ms = new StringBuilder();
				ms.append("Missing mandatory query parameters: ");
				for (String p : missing) {
					ms.append(p);
					ms.append("\t");
				}
				ms.append("\n");
				return Response.status(400).entity(ms.toString()).build();
			}

			Query q = binder.toQuery();
			QueryExecution qe = QueryExecutionFactory.sparqlService(
					specification.getEndpoint(), q);
			Object entity;
			ResponseBuilder rb;
			if (q.isSelectType()) {
				ResultSet rs = qe.execSelect();
				entity = ResultSetFormatter.asText(rs);
				rb = Response.ok(entity);
			} else if (q.isConstructType()) {
				StringWriter sw = new StringWriter();
				NTriplesWriter.write(sw, qe.execConstructTriples());
				rb = Response.ok().entity(sw);
			} else if (q.isAskType()) {
				boolean result = qe.execAsk();
				if (result) {
					rb = Response.ok("True");
				} else {
					rb = Response.noContent().entity("False");
				}
			} else if (q.isDescribeType()) {
				StringWriter sw = new StringWriter();
				NTriplesWriter.write(sw, qe.execDescribeTriples());
				rb = Response.ok().entity(sw);
			} else {
				rb = Response.serverError().entity(
						"Unsupported query type: " + q.getQueryType());
			}
			addHeaders(rb, id);

			return rb.build();
		} catch (IOException e) {
			throw new WebApplicationException(e);
		}
	}

	@POST
	@Consumes("application/x-www-form-urlencoded")
	public Response post(@PathParam("id") String id,
			MultivaluedMap<String, String> form) {
		return performQuery(id, form);
	}

	@GET
	public Response get(@PathParam("id") String id) {
		return performQuery(id, requestUri.getQueryParameters());
	}
}
