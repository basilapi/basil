/*
 * Copyright (c) 2021. Enrico Daga and Luca Panziera
 *
 * MLicensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.github.basilapi.basil.rendering;

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
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.resultset.CSVOutput;
import org.apache.jena.sparql.resultset.JSONOutput;
import org.apache.jena.sparql.resultset.XMLOutput;

public class ResultSetRenderer extends Renderer<ResultSet> {

	public ResultSetRenderer(ResultSet input) {
		super(input);
	}

	private InputStream asRdfStream(MediaType type, String g, Map<String, String> pref) throws CannotRenderException {
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			String uri = g;
			if (uri.indexOf('#') > -1) {
				uri = uri.substring(0, uri.lastIndexOf('#'));
			}

			// See Issue #5
			RDFFormat f = null;
			if (MoreMediaType.RDFXML_TYPE.equals(type)) {
				f = RDFFormat.RDFXML;
			}else if (MoreMediaType.NTRIPLES_TYPE.equals(type)) {
				f = RDFFormat.NTRIPLES_UTF8;
			}else if (MoreMediaType.RDFJSON_TYPE.equals(type)) {
				f = RDFFormat.RDFJSON;
			}else if (MoreMediaType.APPLICATION_TURTLE_TYPE.equals(type) ||
					MoreMediaType.TEXT_TURTLE_TYPE.equals(type)) {
				f = RDFFormat.TURTLE_FLAT;
			}else if (MoreMediaType.RDFXML_TYPE.equals(type)) {
				f = RDFFormat.RDFXML;
			}
			
			if (f != null) {
				RDFStreamer.stream(baos, getInput(), f, new SimpleTripleAdapter(uri + "#"));
				return new ByteArrayInputStream(baos.toByteArray());
			}
			
			if (MoreMediaType.TEXT_X_NQUADS_TYPE.equals(type)) {
				f = RDFFormat.NQUADS_UTF8;
				RDFStreamer.streamQuads(baos, getInput(), f, new SimpleQuadAdapter(uri + "#"));
				return new ByteArrayInputStream(baos.toByteArray());
			}
		} catch (Throwable e) {
			throw new CannotRenderException("An error occurred: " + e.getMessage(), e);
		}
		throw new CannotRenderException(getInput(), type);
	}

	@Override
	public String render(MediaType type, String g, Map<String, String> pref) throws CannotRenderException {
		// rdf formats
		if (MoreMediaType.isRDF(type)) {
			try {
				return IOUtils.toString(asRdfStream(type, g, pref));
			} catch (IOException e) {
				throw new CannotRenderException(e);
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

		throw new CannotRenderException(getInput(), type);
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
