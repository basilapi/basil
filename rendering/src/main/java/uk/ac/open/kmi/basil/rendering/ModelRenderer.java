package uk.ac.open.kmi.basil.rendering;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Map;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.DatasetGraphOne;
import org.apache.jena.sparql.util.Context;

public class ModelRenderer extends Renderer<Model> {

	public ModelRenderer(Model input) {
		super(input);
	}

	@Override
	public InputStream stream(MediaType type, String graphName, Map<String, String> pref) throws CannotRenderException {
		return new ByteArrayInputStream(render(type, graphName, pref).getBytes());
	}

	@Override
	public String render(MediaType type, String graphName, Map<String, String> pref) throws CannotRenderException {
		// text/plain
		if (MediaType.TEXT_PLAIN_TYPE.equals(type) || MoreMediaType.NTRIPLES_TYPE.equals(type)) {
			StringWriter sw = new StringWriter();
			NTriplesWriter.write(sw, getInput().getGraph().find(null, null, null));
			return sw.toString();
		}

		// xml
		if (MediaType.TEXT_XML_TYPE.equals(type) || MediaType.APPLICATION_XML_TYPE.equals(type)) {
			Iterator<Triple> tr = getInput().getGraph().find(null, null, null);
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
			Iterator<Triple> tr = getInput().getGraph().find(null, null, null);
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
			RDFJSONWriter.output(baos, getInput().getGraph());
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
			p.putAll(pref);
			jw.write(w, new DatasetGraphOne(getInput().getGraph()), p, null, Context.emptyContext);
			return w.toString();
		}

		// application/rdf+xml
		if (MoreMediaType.RDFXML_TYPE.equals(type)) {
			StringWriter w = new StringWriter();
			RDFXMLPlainWriter writer = new RDFXMLPlainWriter();
			PrefixMap p = new PrefixMapStd();
			p.putAll(pref);
			writer.write(w, getInput().getGraph(), p, "", Context.emptyContext);
			return w.toString();
		}

		// turtle
		if (MoreMediaType.TEXT_TURTLE_TYPE.equals(type) || MoreMediaType.APPLICATION_TURTLE_TYPE.equals(type)) {
			StringWriter w = new StringWriter();
			TurtleWriter writer = new TurtleWriter();
			PrefixMap p = new PrefixMapStd();
			p.putAll(pref);
			writer.write(w, getInput().getGraph(), p, "", Context.emptyContext);
			return w.toString();
		}

		// text/x-nquads
		if (MoreMediaType.TEXT_X_NQUADS_TYPE.equals(type)) {
			StringWriter w = new StringWriter();
			NQuadsWriter writer = new NQuadsWriter();
			DatasetGraph dg = DatasetGraphFactory.createMem();
			dg.addGraph(NodeFactory.createURI(graphName), getInput().getGraph());
			writer.write(w, dg, PrefixMapNull.empty, null, Context.emptyContext);
			return w.toString();
		}
		
		// application/sparql-results+json
		// application/sparql-results+xml
		// text/csv
		// text/tsv
		if (MoreMediaType.SPARQL_RESULTS_XML_TYPE.equals(type)||
				MoreMediaType.SPARQL_RESULTS_JSON_TYPE.equals(type)||
				MoreMediaType.TEXT_CSV_TYPE.equals(type)||
				MoreMediaType.TEXT_TSV_TYPE.equals(type)) {
			return RendererFactory.getRenderer(ResultSetFactory.makeResults(getInput())).render(type, graphName, pref);
		}

		throw new CannotRenderException(getInput(), type);
	}
}
