package uk.ac.open.kmi.basil.rest.core;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Iterator;
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
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Variant;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.out.JsonLDWriter;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.PrefixMapNull;
import org.apache.jena.riot.system.PrefixMapStd;
import org.apache.jena.riot.writer.NQuadsWriter;
import org.apache.jena.riot.writer.NTriplesWriter;
import org.apache.jena.riot.writer.RDFJSONWriter;
import org.apache.jena.riot.writer.RDFXMLPlainWriter;
import org.apache.jena.riot.writer.TurtleWriter;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.subject.Subject;
import org.secnod.shiro.jaxrs.Auth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.open.kmi.basil.core.InvocationResult;
import uk.ac.open.kmi.basil.core.exceptions.SpecificationParsingException;
import uk.ac.open.kmi.basil.rendering.SimpleTripleAdapter;
import uk.ac.open.kmi.basil.rendering.RDFStreamer;
import uk.ac.open.kmi.basil.doc.Doc.Field;
import uk.ac.open.kmi.basil.rest.auth.AuthResource;
import uk.ac.open.kmi.basil.sparql.Specification;
import uk.ac.open.kmi.basil.view.Items;
import uk.ac.open.kmi.basil.view.View;
import uk.ac.open.kmi.basil.view.Views;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.QueryParseException;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.core.DatasetGraphFactory;
import com.hp.hpl.jena.sparql.core.DatasetGraphOne;
import com.hp.hpl.jena.sparql.resultset.CSVOutput;
import com.hp.hpl.jena.sparql.resultset.JSONOutput;
import com.hp.hpl.jena.sparql.resultset.XMLOutput;
import com.hp.hpl.jena.sparql.util.Context;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

@Path("{id}")
@Api(value = "/basil", description = "BASIL operations")
public class ApiResource extends AbstractResource {

	private static Logger log = LoggerFactory
			.getLogger(ApiResource.class);

	@SuppressWarnings("unchecked")
	private Response performQuery(String id,
			MultivaluedMap<String, String> parameters, String extension) {
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
						rb.header("Content-Type", view.getMimeType()
								+ "; charset=utf-8");
						log.trace("API execution. View executed. Returning response.");
						return rb.build();
					}
				}
				// Still found nothing?
				if (type == null) {
					log.trace("API execution. View not found.");
					return packError(Response.status(404),"Not found\n").build();
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
			Object entity = null;
			Map<String,String> prefixMap = r.getQuery().getPrefixMapping().getNsPrefixMap();
			if (r.getResult() instanceof ResultSet) {
				entity = prepareEntity(type, (ResultSet) r.getResult());
			} else if (r.getResult() instanceof Model) {
				entity = prepareEntity(type, (Model) r.getResult(), prefixMap);
			} else if (r.getResult() instanceof Boolean) {
				if (MoreMediaType.isRDF(type)) {
					entity = prepareEntity(type, ResultSetFormatter.toModel((Boolean) r.getResult()), prefixMap);
				}else{
					entity = prepareEntity(type, (Boolean) r.getResult());
				}
			}
			// If entity is null then format is not acceptable
			// ie we don't have an implementation of that object/type map
			if (entity == null) {
				log.trace("API execution. Cannot prepare response (not acceptable).");
				return buildNotAcceptable();
			}
			ResponseBuilder rb;
			rb = Response.ok().entity(entity);
			addHeaders(rb, id);
			rb.header("Content-Type", type.withCharset("UTF-8").toString());
			log.trace("API execution. Return response.");
			return rb.build();
		} catch (Exception e) {
			//Response r = Response.serverError().entity(e.getMessage()).build();
			log.error("ERROR while query execution",e);
			return packError(Response.serverError(),e).build();
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
				return "True\n";
			} else {
				return "False\n";
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
			p.append("<items>");

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

			p.append("\n\t");
			p.append("</items>");

			p.append("\n");
			p.append("</data>");
			p.append("\n");
			return p.toString();
		}

		if (MediaType.APPLICATION_JSON_TYPE.equals(type)) {
			JsonObject o = new JsonObject();
			o.add("bool", new JsonPrimitive(b));
			return o.toString() + "\n";
		}

		// sparql-results+xml
		if (MoreMediaType.SPARQL_RESULTS_XML_TYPE.equals(type)) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			XMLOutput xOut = new XMLOutput(null);
			xOut.format(baos, b);
			return new String(baos.toByteArray());
		}

		// sparql-results+json
		if (MoreMediaType.SPARQL_RESULTS_JSON_TYPE.equals(type)) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			JSONOutput xOut = new JSONOutput();
			xOut.format(baos, b);
			return new String(baos.toByteArray());
		}
		
		return null;
	}

	private Object prepareEntity(MediaType type, Model model,
			Map<String, String> prefixes) {

		// text/plain
		if (MediaType.TEXT_PLAIN_TYPE.equals(type)
				|| MoreMediaType.NTRIPLES_TYPE.equals(type)) {
			StringWriter sw = new StringWriter();
			NTriplesWriter.write(sw, model.getGraph().find(null, null, null));
			return sw.toString();
		}

		// xml
		if (MediaType.TEXT_XML_TYPE.equals(type)
				|| MediaType.APPLICATION_XML_TYPE.equals(type)) {
			Iterator<Triple> tr = model.getGraph().find(null, null, null);
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
			p.append("\n\t");
			p.append("<items>");
			while (tr.hasNext()) {
				Triple t = tr.next();
				p.append("\n\t\t");
				p.append("<item>");
				// subject
				p.append("\n\t\t\t");
				String v = "subject";
				Node n = t.getSubject();
				p.append("<");
				p.append(v);
				p.append(">");
				p.append(n.toString());
				p.append("</");
				p.append(v);
				p.append(">");

				// predicate
				p.append("\n\t\t\t");
				v = "predicate";
				n = t.getSubject();
				p.append("<");
				p.append(v);
				p.append(">");
				p.append(n.toString());
				p.append("</");
				p.append(v);
				p.append(">");

				// object
				p.append("\n\t\t\t");
				v = "object";
				n = t.getSubject();
				p.append("<");
				p.append(v);
				p.append(">");
				p.append(n.toString());
				p.append("</");
				p.append(v);
				p.append(">");

				p.append("\n\t\t");
				p.append("</item>");
			}

			p.append("\n\t");
			p.append("</items>");
			p.append("\n");
			p.append("</data>");
			p.append("\n");
			return p.toString();
		}

		// application/json
		if (MediaType.APPLICATION_JSON_TYPE.equals(type)) {
			Iterator<Triple> tr = model.getGraph().find(null, null, null);
			JsonObject o = new JsonObject();
			JsonArray vars = new JsonArray();
			vars.add(new JsonPrimitive("subject"));
			vars.add(new JsonPrimitive("predicate"));
			vars.add(new JsonPrimitive("object"));
			o.add("vars", vars);
			JsonArray items = new JsonArray();
			while (tr.hasNext()) {
				Triple t = tr.next();
				JsonObject item = new JsonObject();
				item.add("subject", new JsonPrimitive(t.getSubject().toString()));
				item.add("predicate", new JsonPrimitive(t.getPredicate().toString()));
				item.add("object", new JsonPrimitive(t.getObject().toString()));
				items.add(item);
			}
			o.add("items", items);
			return o.toString() + "\n";
		}

		// application/rdf+json
		if (MoreMediaType.RDFJSON_TYPE.equals(type)) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			RDFJSONWriter.output(baos, model.getGraph());
			try {
				return new String(baos.toByteArray(), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				// This will never happen
				new WebApplicationException(500);
			}
		}

		// application/ld+json
		if (MoreMediaType.JSONLD_TYPE.equals(type)) {
			StringWriter w = new StringWriter();
			JsonLDWriter jw = new JsonLDWriter(RDFFormat.JSONLD_PRETTY);
			PrefixMap p = new PrefixMapStd();
			p.putAll(prefixes);
			jw.write(w, new DatasetGraphOne(model.getGraph()), p, null,
					Context.emptyContext);
			return w.toString();
		}

		// application/rdf+xml
		if (MoreMediaType.RDFXML_TYPE.equals(type)) {
			StringWriter w = new StringWriter();
			RDFXMLPlainWriter writer = new RDFXMLPlainWriter();
			PrefixMap p = new PrefixMapStd();
			p.putAll(prefixes);
			writer.write(w, model.getGraph(), p, "", Context.emptyContext);
			return w.toString();
		}

		// turtle
		if (MoreMediaType.TEXT_TURTLE_TYPE.equals(type)
				|| MoreMediaType.APPLICATION_TURTLE_TYPE.equals(type)) {
			StringWriter w = new StringWriter();
			TurtleWriter writer = new TurtleWriter();
			PrefixMap p = new PrefixMapStd();
			p.putAll(prefixes);
			writer.write(w, model.getGraph(), p, "", Context.emptyContext);
			return w.toString();
		}

		// text/x-nquads
		if (MoreMediaType.TEXT_X_NQUADS_TYPE.equals(type)) {
			StringWriter w = new StringWriter();
			NQuadsWriter writer = new NQuadsWriter();
			DatasetGraph dg = DatasetGraphFactory.createMem();
			dg.addGraph(NodeFactory.createURI(requestUri.getRequestUri()
					.toString()), model.getGraph());
			writer.write(w, dg, PrefixMapNull.empty, null, Context.emptyContext);
			return w.toString();
		}

		return null;
	}

	private Object prepareEntity(MediaType type, ResultSet rs) {
		
		// rdf formats
		if(MoreMediaType.isRDF(type)){
			// XXX Only RDF/XML, for the moment. See Issue #5
			if(MoreMediaType.RDFXML_TYPE.equals(type)){
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				String uri = requestUri.getRequestUri().toString();
				if(uri.indexOf('#')>-1){
					uri = uri.substring(0,uri.lastIndexOf('#'));
				}
				RDFStreamer.stream(baos, rs, RDFFormat.RDFXML, new SimpleTripleAdapter(uri + "#"));
				return new ByteArrayInputStream(baos.toByteArray());
			}
		}
		
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
			p.append("\n\t");
			p.append("<items>");
			while (rs.hasNext()) {
				QuerySolution r = rs.next();
				p.append("\n\t\t");
				p.append("<item>");
				Iterator<String> vn = r.varNames();
				while (vn.hasNext()) {
					p.append("\n\t\t\t");
					String v = vn.next();
					RDFNode n = r.get(v);
					p.append("<");
					p.append(v);
					p.append(">");
					p.append(n.toString());
					p.append("</");
					p.append(v);
					p.append(">");
				}
				p.append("\n\t\t");
				p.append("</item>");
			}
			p.append("\n\t");
			p.append("</items>");
			p.append("\n");
			p.append("</data>");
			p.append("\n");
			return p.toString();
		}

		// application/json
		if (MediaType.APPLICATION_JSON_TYPE.equals(type)) {
			JsonObject o = new JsonObject();
			JsonArray vars = new JsonArray();
			for (String v : rs.getResultVars()) {
				vars.add(new JsonPrimitive(v));
			}
			o.add("vars", vars);
			JsonArray items = new JsonArray();
			while (rs.hasNext()) {
				QuerySolution t = rs.next();
				JsonObject item = new JsonObject();
				Iterator<String> vi = t.varNames();
				while (vi.hasNext()) {
					String vn = vi.next();
					RDFNode n = t.get(vn);
					item.add(vn, new JsonPrimitive(n.toString()));
				}
				items.add(item);
			}
			o.add("items", items);
			return o.toString() + "\n";
		}

		// sparql-results+xml
		if (MoreMediaType.SPARQL_RESULTS_XML_TYPE.equals(type)) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			XMLOutput xOut = new XMLOutput(null);
			xOut.format(baos, rs);
			return new String(baos.toByteArray());
		}

		// sparql-results+json
		if (MoreMediaType.SPARQL_RESULTS_JSON_TYPE.equals(type)) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			JSONOutput xOut = new JSONOutput();
			xOut.format(baos, rs);
			return new String(baos.toByteArray());
		}

		// text/csv
		if (MoreMediaType.TEXT_CSV_TYPE.equals(type)) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			CSVOutput xOut = new CSVOutput();
			xOut.format(baos, rs);
			return new String(baos.toByteArray());
		}

		// text/tsv
		if (MoreMediaType.TEXT_TSV_TYPE.equals(type)) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			CSVOutput xOut = new CSVOutput();
			xOut.format(baos, rs);
			return new String(baos.toByteArray());
		}

		return null;
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
		List<MediaType> acceptHeaders = requestHeaders
				.getAcceptableMediaTypes();
		log.debug("Acceptable media types: {}",acceptHeaders);
		if (acceptHeaders == null || acceptHeaders.size() == 0) {
			// Default type is text/plain
			return MediaType.TEXT_PLAIN_TYPE;
		}

		for (MediaType mt : acceptHeaders) {
			String qValue = mt.getParameters().get("q");
			if (qValue != null && Double.valueOf(qValue).doubleValue() == 0.0) {
				break;
			}

			for (MediaType variant : MoreMediaType.mediaTypes) {
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
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 500, message = "Internal error") }
    )
	public Response postExt(
			@ApiParam(value = "ID of the API specification", required = true)
            @PathParam("id") String id,
            @ApiParam(value = "Extension of the output data format (e.g., .json, .xml)")
            @PathParam("ext") String extension,
            @ApiParam(value = "Input parameters")
            MultivaluedMap<String, String> form) {
		log.trace("Called POST with extension.");
		return performQuery(id, form, extension);
	}

	@Path("api{ext}")
	@GET
	@ApiOperation(value = "Invoke API with a specific extension")
	@ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 500, message = "Internal error") }
    )
	public Response getExt(
			@ApiParam(value = "ID of the API specification", required = true)
            @PathParam("id") String id,
			@ApiParam(value = "Extension of the output data format (e.g., .json, .xml)", required = false)
			@PathParam("ext") String extension) {
		log.trace("Called GET with extension.");
		return performQuery(id, requestUri.getQueryParameters(), extension);
	}

	@Path("api")
	@GET
	@ApiOperation(value = "Invoke API")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 500, message = "Internal error")}
	)
	public Response get(
			@ApiParam(value = "ID of the API specification", required = true)
			@PathParam("id") String id
	) {
		log.trace("Called GET.");
		return performQuery(id, requestUri.getQueryParameters(), "");
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
	@ApiOperation(value = "Update existing API specification",
			response = URI.class)
	@ApiResponses(value = {@ApiResponse(code = 400, message = "Body cannot be empty"),
			@ApiResponse(code = 200, message = "Specification updated"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal error")})
	public Response replaceSpec(
			@ApiParam(value = "ID of the API specification", required = true)
			@PathParam(value = "id") String id,
			@ApiParam(value = "SPARQL Endpoint", required = false)
			@QueryParam(value = "endpoint") String endpoint,
			@ApiParam(value = "SPARQL query that substitutes the API specification", required = true)
			String body,
			@Auth Subject subject) {
		log.trace("Called PUT with id: {}", id);
		try {
			subject.checkRole(id);
			getApiManager().replaceSpecification(id, body);
			endpoint = getParameterOrHeader("endpoint");
			getApiManager().replaceSpecification(id, endpoint, body);
			ResponseBuilder response;
			URI spec = requestUri.getBaseUriBuilder().path(id).path("spec").build();
			log.info("Replaced spec at: {}", spec);
			JsonObject m = new JsonObject();
			m.add("message", new JsonPrimitive("Replaced: " + spec.toString()));
			m.add("location", new JsonPrimitive(spec.toString()));
			response = Response.ok(spec).entity(m.toString());
			addHeaders(response, id);
			return response.build();
		} catch (AuthorizationException e) {
			log.trace("Not authorized");
			return packError(Response.status(Status.FORBIDDEN),e).build();
		} catch (QueryParseException|SpecificationParsingException e) {
			return packError(Response.status(HttpURLConnection.HTTP_BAD_REQUEST)
					, e).build();
		} catch (Exception e) {
			return packError(Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR)
					, e).build();
		}
	}

	/**
	 * Redirect to /spec
	 *
	 * @param id
	 * @return
	 */
	@GET
	public Response redirectToSpec(
			@PathParam(value = "id") String id) {
		// If requests HTML, go to API Docs instead
		ResponseBuilder builder = Response.status(303);
		addHeaders(builder, id);
		if(requestHeaders
				.getAcceptableMediaTypes().contains(MediaType.TEXT_HTML_TYPE)){
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
	@ApiResponses(value = {@ApiResponse(code = 404, message = "Specification not found"),
			@ApiResponse(code = 200, message = "Specification found"),
			@ApiResponse(code = 500, message = "Internal error")})
	public Response getSpec(
			@ApiParam(value = "ID of the API specification", required = true)
			@PathParam(value = "id") String id) {
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
			return packError(Response
					.status(HttpURLConnection.HTTP_INTERNAL_ERROR)
					,e).build();
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
	@ApiResponses(value = {@ApiResponse(code = 404, message = "Specification not found"),
			@ApiResponse(code = 200, message = "Specification deleted"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal error")})
	public Response deleteSpec(
			@ApiParam(value = "ID of the API specification", required = true)
			@PathParam(value = "id") String id,
			@Auth Subject subject) {
		log.trace("Called DELETE spec with id: {}", id);
		try {
			subject.checkRole(id);
			getApiManager().deleteApi(id);
			URI spec = requestUri.getBaseUriBuilder().path(id).path("spec").build();
			ResponseBuilder response;
			JsonObject m = new JsonObject();
			m.add("message", new JsonPrimitive("Deleted: " + spec.toString()));
			m.add("location", new JsonPrimitive(spec.toString()));
			response = Response.ok().entity(m.toString());
			addHeaders(response, id);

			return response.build();
		} catch (AuthorizationException e) {
			log.trace("Not authorized");
			return packError(Response.status(Status.FORBIDDEN),e).build();
		} catch (Exception e) {
			return packError(Response
					.status(HttpURLConnection.HTTP_INTERNAL_ERROR),e).build();
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
	@ApiResponses(value = {@ApiResponse(code = 404, message = "Specification not found"),
			@ApiResponse(code = 200, message = "API clones"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal error")})
	public Response getClone(
			@ApiParam(value = "ID of the API specification", required = true)
			@PathParam(value = "id") String id,
			@Auth Subject subject) {
		log.trace("Called GET clone with id: {}", id);
		try {
			if (subject.isAuthenticated()) {
				String username = (String) subject.getSession().getAttribute(AuthResource.CURRENT_USER_KEY);
				String newId = getApiManager().cloneSpecification(username, id);
				if (newId == null) {
					return Response.status(Response.Status.NOT_FOUND).build();
				}
				ResponseBuilder response;
				URI spec = requestUri.getBaseUriBuilder().path(newId).path("spec").build();
				log.info("Cloned spec at: {}", spec);
				JsonObject m = new JsonObject();
				m.add("message", new JsonPrimitive("Cloned at: " + spec.toString()));
				m.add("location", new JsonPrimitive(spec.toString()));
				response = Response.ok(spec).entity(m.toString());
				addHeaders(response, newId);

				return response.build();
			}
		} catch (Exception e) {
			log.error("An error occurred", e);
			return packError(Response
					.status(HttpURLConnection.HTTP_INTERNAL_ERROR)
					,e).build();
		}
		return packError(Response.status(HttpURLConnection.HTTP_FORBIDDEN)
				, "User must be authenticated").build();
	}
}
