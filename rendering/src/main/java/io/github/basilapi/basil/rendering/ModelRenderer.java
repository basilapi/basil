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
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Map;

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

				// Specify type as XML attribute
				p.append(" ");
				p.append("type=\"");
				if (n.isBlank()) {
					p.append("bnode");
				} else {
					p.append("uri");
				}
				p.append("\"");

				p.append(">");
				p.append(n.toString());
				p.append("</");
				p.append(v);
				p.append(">");

				// predicate
				p.append("\n\t\t\t");
				v = "predicate";
				n = t.getPredicate();
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
				n = t.getObject();
				p.append("<");
				p.append(v);

				// Specify type as XML attribute
				p.append(" ");
				p.append("type=\"");
				if (n.isBlank()) {
					p.append("bnode");
				} else if (n.isURI()) {
					p.append("uri");
				} else {
					// Literal
					p.append("literal");
				}
				p.append("\"");

				if (n.isLiteral()) {
					String lang = n.getLiteralLanguage();
					if (lang != null && !"".equals(lang)) {
						p.append(" ");
						p.append("lang=\"");
						p.append(lang);
						p.append("\"");
					}
					String datatype = n.getLiteralDatatypeURI();
					if (datatype != null && !"".equals(datatype)) {
						p.append(" ");
						p.append("datatype=\"");
						p.append(datatype);
						p.append("\"");
					}
				}
				p.append(">");
				if (n.isBlank()) {
					p.append(n.getBlankNodeLabel());
				} else if (n.isURI()) {
					p.append(n.getURI());
				} else {
					// Literal
					p.append(n.getLiteralLexicalForm());
				}
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
			vars.add(new JsonPrimitive("subject_type"));
			vars.add(new JsonPrimitive("object_type"));
			vars.add(new JsonPrimitive("object_datatype"));
			vars.add(new JsonPrimitive("object_lang"));
			o.add("vars", vars);
			JsonArray items = new JsonArray();
			while (tr.hasNext()) {
				Triple t = tr.next();
				JsonObject item = new JsonObject();
				item.add("subject", new JsonPrimitive(t.getSubject().toString()));
				item.add("predicate", new JsonPrimitive(t.getPredicate().toString()));
				
				String ostring;
				if (t.getObject().isBlank()) {
					ostring = t.getObject().getBlankNodeLabel();
				} else if (t.getObject().isURI()) {
					ostring = t.getObject().getURI();
				} else {
					// Literal
					ostring = t.getObject().getLiteralLexicalForm();
				}
				item.add("object", new JsonPrimitive(ostring));
				
				item.add("subject_type", new JsonPrimitive(t.getSubject().isBlank() ? "bnode" : "uri"));
				item.add("object_type", new JsonPrimitive(
						t.getObject().isBlank() ? "bnode" : (t.getObject().isLiteral() ? "literal" : "uri")));
				item.add("object_datatype",
						new JsonPrimitive(t.getObject().isLiteral() ? t.getObject().getLiteralDatatypeURI() : ""));
				item.add("object_lang",
						new JsonPrimitive(t.getObject().isLiteral() ? t.getObject().getLiteralLanguage() : ""));
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
				new CannotRenderException();
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
			DatasetGraph dg = DatasetGraphFactory.createMemFixed();
			dg.addGraph(NodeFactory.createURI(graphName), getInput().getGraph());
			writer.write(w, dg, PrefixMapNull.empty, null, Context.emptyContext);
			return w.toString();
		}

		// application/sparql-results+json
		// application/sparql-results+xml
		// text/csv
		// text/tsv
		if (MoreMediaType.SPARQL_RESULTS_XML_TYPE.equals(type) || MoreMediaType.SPARQL_RESULTS_JSON_TYPE.equals(type)
				|| MoreMediaType.TEXT_CSV_TYPE.equals(type) || MoreMediaType.TEXT_TSV_TYPE.equals(type)) {
			return RendererFactory.getRenderer(ResultSetFactory.makeResults(getInput())).render(type, graphName, pref);
		}

		throw new CannotRenderException(getInput(), type);
	}
}
