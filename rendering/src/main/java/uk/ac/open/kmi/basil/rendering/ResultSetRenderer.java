package uk.ac.open.kmi.basil.rendering;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.jena.riot.RDFFormat;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.sparql.resultset.CSVOutput;
import com.hp.hpl.jena.sparql.resultset.JSONOutput;
import com.hp.hpl.jena.sparql.resultset.XMLOutput;

public class ResultSetRenderer extends Renderer<ResultSet> {

	public ResultSetRenderer(ResultSet input) {
		super(input);
	}

	private InputStream asRdfStream(MediaType type, String g, Map<String, String> pref) throws CannotRenderException {
		// XXX Only RDF/XML, for the moment. See Issue #5
		if (MoreMediaType.RDFXML_TYPE.equals(type)) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			String uri = g;
			if (uri.indexOf('#') > -1) {
				uri = uri.substring(0, uri.lastIndexOf('#'));
			}
			RDFStreamer.stream(baos, getInput(), RDFFormat.RDFXML, new SimpleTripleAdapter(uri + "#"));
			return new ByteArrayInputStream(baos.toByteArray());
		}

		throw new CannotRenderException();
	}

	@Override
	public String render(MediaType type, String g, Map<String, String> pref) throws CannotRenderException {
		// rdf formats
		if (MoreMediaType.isRDF(type)) {
			try {
				return IOUtils.toString(asRdfStream(type, g, pref));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// text/plain
		if (MediaType.TEXT_PLAIN_TYPE.equals(type)) {
			return ResultSetFormatter.asText(getInput());
		}

		// xml
		if (MediaType.TEXT_XML_TYPE.equals(type) || MediaType.APPLICATION_XML_TYPE.equals(type)) {
			StringBuilder p = new StringBuilder();
			p.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
			p.append("\n");
			p.append("<data>");
			p.append("\n\t");
			p.append("<vars>");
			for (String v : getInput().getResultVars()) {
				p.append("\n\t\t");
				p.append("<var>");
				p.append(v);
				p.append("</var>");
			}
			p.append("\n\t");
			p.append("</vars>");
			p.append("\n\t");
			p.append("<items>");
			while (getInput().hasNext()) {
				QuerySolution r = getInput().next();
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
			for (String v : getInput().getResultVars()) {
				vars.add(new JsonPrimitive(v));
			}
			o.add("vars", vars);
			JsonArray items = new JsonArray();
			while (getInput().hasNext()) {
				QuerySolution t = getInput().next();
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
			xOut.format(baos, getInput());
			return new String(baos.toByteArray());
		}

		// sparql-results+json
		if (MoreMediaType.SPARQL_RESULTS_JSON_TYPE.equals(type)) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			JSONOutput xOut = new JSONOutput();
			xOut.format(baos, getInput());
			return new String(baos.toByteArray());
		}

		// text/csv
		if (MoreMediaType.TEXT_CSV_TYPE.equals(type)) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			CSVOutput xOut = new CSVOutput();
			xOut.format(baos, getInput());
			return new String(baos.toByteArray());
		}

		// text/tsv
		if (MoreMediaType.TEXT_TSV_TYPE.equals(type)) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			CSVOutput xOut = new CSVOutput();
			xOut.format(baos, getInput());
			return new String(baos.toByteArray());
		}

		throw new CannotRenderException();
	}

	@Override
	public InputStream stream(MediaType type, String g, Map<String, String> pref) throws CannotRenderException {
		// rdf formats
		if (MoreMediaType.isRDF(type)) {
			return asRdfStream(type, g, pref);
		}
		return new ByteArrayInputStream(render(type, g, pref).getBytes());
	}
}
