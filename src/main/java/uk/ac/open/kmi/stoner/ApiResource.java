package uk.ac.open.kmi.stoner;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Variant;

import org.apache.jena.riot.writer.NTriplesWriter;

import uk.ac.open.kmi.stoner.sparql.QueryParameter;
import uk.ac.open.kmi.stoner.sparql.Specification;
import uk.ac.open.kmi.stoner.sparql.VariablesBinder;
import uk.ac.open.kmi.stoner.store.Store;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.RDFNode;

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
			Object entity = null;

			MediaType type = getBestAcceptable();
			if (type == null) {
				return buildNotAcceptable();
			}

			if (q.isSelectType()) {
				entity = prepareEntity(type, qe.execSelect());
			} else if (q.isConstructType()) {
				entity = prepareEntity(type, qe.execConstructTriples());
			} else if (q.isAskType()) {
				entity = prepareEntity(type, qe.execAsk());
			} else if (q.isDescribeType()) {
				entity = prepareEntity(type, qe.execDescribeTriples());
			} else {
				return Response.serverError()
						.entity("Unsupported query type: " + q.getQueryType())
						.build();
			}

			// If entity is null then format is not acceptable
			// ie we don't have an implementation of that object/type map
			if (entity == null) {
				return buildNotAcceptable();
			}
			ResponseBuilder rb;
			rb = Response.ok().entity(entity);
			addHeaders(rb, id);

			return rb.build();
		} catch (IOException e) {
			throw new WebApplicationException(e);
		}
	}

	private Response buildNotAcceptable() {
		return Response.notAcceptable(
				Variant.mediaTypes(MediaType.TEXT_PLAIN_TYPE).add().build())
				.build();
	}

	private Object prepareEntity(MediaType type, boolean b) {

		// text/plain
		if (MediaType.TEXT_PLAIN_TYPE.equals(type)) {
			if (b) {
				return "True";
			} else {
				return "False";
			}
		}

		// xml
		if (MediaType.TEXT_XML_TYPE.equals(type)
				|| MediaType.APPLICATION_XML_TYPE.equals(type)) {
			StringBuilder p = new StringBuilder();
			p.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
			p.append("\n");
			p.append("<data>");
			p.append("\n\t");
			p.append("<vars>");
			// boolean
			p.append("\n\t\t");
			p.append("<var>");
			p.append("boolean");
			p.append("</var>");

			p.append("\n\t");
			p.append("</vars>");

			p.append("\n\t");
			p.append("<item>");

			p.append("\n\t\t");
			String v = "boolean";

			p.append("<");
			p.append(v);
			p.append(" ");
			p.append("type=\"");
			p.append("boolean");
			p.append("\"");
			p.append(">");
			if (b)
				p.append("true");
			else
				p.append("false");
			p.append("</");
			p.append(v);
			p.append(">");

			p.append("\n\t");
			p.append("</item>");

			p.append("\n");
			p.append("</data>");
			p.append("\n");
			return p.toString();
		}

		return null;
	}

	private Object prepareEntity(MediaType type, Iterator<Triple> tr) {
		// text/plain
		if (MediaType.TEXT_PLAIN_TYPE.equals(type)) {
			StringWriter sw = new StringWriter();
			NTriplesWriter.write(sw, tr);
			return sw;
		}

		// xml
		if (MediaType.TEXT_XML_TYPE.equals(type)
				|| MediaType.APPLICATION_XML_TYPE.equals(type)) {
			StringBuilder p = new StringBuilder();
			p.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
			p.append("\n");
			p.append("<data>");
			p.append("\n\t");
			p.append("<vars>");
			// subject
			p.append("\n\t\t");
			p.append("<var>");
			p.append("subject");
			p.append("</var>");
			// predicate
			p.append("\n\t\t");
			p.append("<var>");
			p.append("predicate");
			p.append("</var>");
			// object
			p.append("\n\t\t");
			p.append("<var>");
			p.append("object");
			p.append("</var>");
			p.append("\n\t");
			p.append("</vars>");
			while (tr.hasNext()) {
				Triple t = tr.next();
				p.append("\n\t");
				p.append("<item>");
				// subject
				p.append("\n\t\t");
				String v = "subject";
				Node n = t.getSubject();
				p.append("<");
				p.append(v);
				p.append(" ");
				p.append("type=\"");
				if (n.isBlank()) {
					p.append("anon");
				} else if (n.isLiteral()) {
					p.append("literal");
				} else if (n.isURI()) {
					p.append("uri");
				}
				p.append("\"");
				p.append(">");
				p.append(n.toString());
				p.append("</");
				p.append(v);
				p.append(">");

				// predicate
				p.append("\n\t\t");
				v = "predicate";
				n = t.getSubject();
				p.append("<");
				p.append(v);
				p.append(" ");
				p.append("type=\"");
				if (n.isBlank()) {
					p.append("anon");
				} else if (n.isLiteral()) {
					p.append("literal");
				} else if (n.isURI()) {
					p.append("uri");
				}
				p.append("\"");
				p.append(">");
				p.append(n.toString());
				p.append("</");
				p.append(v);
				p.append(">");

				// object
				p.append("\n\t\t");
				v = "object";
				n = t.getSubject();
				p.append("<");
				p.append(v);
				p.append(" ");
				p.append("type=\"");
				if (n.isBlank()) {
					p.append("anon");
				} else if (n.isLiteral()) {
					p.append("literal");
				} else if (n.isURI()) {
					p.append("uri");
				}
				p.append("\"");
				p.append(">");
				p.append(n.toString());
				p.append("</");
				p.append(v);
				p.append(">");

				p.append("\n\t");
				p.append("</item>");
			}
			p.append("\n");
			p.append("</data>");
			p.append("\n");
			return p.toString();
		}

		return null;
	}

	private Object prepareEntity(MediaType type, ResultSet rs) {
		// text/plain
		if (MediaType.TEXT_PLAIN_TYPE.equals(type)) {
			return ResultSetFormatter.asText(rs);
		}

		// xml
		if (MediaType.TEXT_XML_TYPE.equals(type)
				|| MediaType.APPLICATION_XML_TYPE.equals(type)) {
			StringBuilder p = new StringBuilder();
			p.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
			p.append("\n");
			p.append("<data>");
			p.append("\n\t");
			p.append("<vars>");
			for (String v : rs.getResultVars()) {
				p.append("\n\t\t");
				p.append("<var>");
				p.append(v);
				p.append("</var>");
			}
			p.append("\n\t");
			p.append("</vars>");
			while (rs.hasNext()) {
				QuerySolution r = rs.next();
				p.append("\n\t");
				p.append("<item>");
				Iterator<String> vn = r.varNames();
				while (vn.hasNext()) {
					p.append("\n\t\t");
					String v = vn.next();
					RDFNode n = r.get(v);
					p.append("<");
					p.append(v);
					p.append(" ");
					p.append("type=\"");
					if (n.isAnon()) {
						p.append("anon");
					} else if (n.isLiteral()) {
						p.append("literal");
					} else if (n.isURIResource()) {
						p.append("uri");
					} else if (n.isResource()) {
						p.append("resource");
					}
					p.append("\"");
					p.append(">");
					p.append(n.toString());
					p.append("</");
					p.append(v);
					p.append(">");
				}
				p.append("\n\t");
				p.append("</item>");
			}
			p.append("\n");
			p.append("</data>");
			p.append("\n");
			return p.toString();
		}
		return null;
	}

	private List<MediaType> getAvailableVariants() {
		return Arrays.asList(new MediaType[] { MediaType.TEXT_PLAIN_TYPE,
				MediaType.TEXT_XML_TYPE, MediaType.APPLICATION_XML_TYPE,

		});
	}

	/**
	 * 
	 * @return null if none of the exisitng variants are acceptable
	 */
	private MediaType getBestAcceptable() {
		// This list is sorted by the client preference
		List<MediaType> acceptHeaders = requestHeaders
				.getAcceptableMediaTypes();
		if (acceptHeaders == null || acceptHeaders.size() == 0) {
			// Default type is text/plain
			return MediaType.TEXT_PLAIN_TYPE;
		}

		for (MediaType mt : acceptHeaders) {
			String qValue = mt.getParameters().get("q");
			if (qValue != null && Double.valueOf(qValue).doubleValue() == 0.0) {
				break;
			}

			for (MediaType variant : getAvailableVariants()) {
				if (variant.isCompatible(mt)) {
					return variant;
				}
			}
			
		}
		return null;
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
