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
import java.nio.charset.StandardCharsets;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.resultset.JSONOutput;
import org.apache.jena.sparql.resultset.XMLOutput;
import org.apache.jena.vocabulary.RDF;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class BooleanRenderer extends Renderer<Boolean> {

	public BooleanRenderer(Boolean input) {
		super(input);
	}

	@Override
	public InputStream stream(MediaType type, String graphName, Map<String, String> pref) throws CannotRenderException {
		return new ByteArrayInputStream(render(type, graphName, pref).getBytes(StandardCharsets.UTF_8));
	}

	@Override
	public String render(MediaType type, String graphName, Map<String, String> pref) throws CannotRenderException {

		if (MoreMediaType.isRDF(type)) {
			Model bm = ModelFactory.createDefaultModel();
			bm.addLiteral(bm.createResource(),RDF.value, (boolean) getInput());
			return new ModelRenderer(bm).render(type, graphName, pref);
		}

		// text/plain
		if (MediaType.TEXT_PLAIN_TYPE.equals(type)) {
			if (getInput()) {
				return "True\n";
			} else {
				return "False\n";
			}
		}

		// xml
		if (MediaType.TEXT_XML_TYPE.equals(type) || MediaType.APPLICATION_XML_TYPE.equals(type)) {
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
			if (getInput())
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
			o.add("bool", new JsonPrimitive(getInput()));
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
		if(MoreMediaType.TEXT_CSV_TYPE.equals(type) || MoreMediaType.TEXT_TSV_TYPE.equals(type)){
			return Boolean.toString(getInput());
		}
		
		throw new CannotRenderException("Cannot render media type " + type.toString());
	}
}
