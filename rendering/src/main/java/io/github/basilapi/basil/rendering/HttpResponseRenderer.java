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

import org.apache.http.HttpResponse;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class HttpResponseRenderer extends Renderer<HttpResponse> {

	public HttpResponseRenderer(HttpResponse input) {
		super(input);
	}

	@Override
	public InputStream stream(MediaType type, String graphName, Map<String, String> pref) throws CannotRenderException {
		return new ByteArrayInputStream(render(type, graphName, pref).getBytes(StandardCharsets.UTF_8));
	}

	@Override
	public String render(MediaType type, String graphName, Map<String, String> pref) throws CannotRenderException {
		
		HttpResponse response = (HttpResponse) getInput();
		
		if (MoreMediaType.isRDF(type)) {
			return new ModelRenderer(asModel(response)).render(type, graphName, pref);
		}

		// text/plain
		if (MediaType.TEXT_PLAIN_TYPE.equals(type)) {
			return response.getStatusLine().toString();
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
			p.append("status");
			p.append("</var>");

			p.append("\n\t");
			p.append("</vars>");

			p.append("\n\t");
			p.append("<items>");

			p.append("\n\t");
			p.append("<item>");

			p.append("\n\t\t");
			String v = "status";

			p.append("<");
			p.append(v);
			p.append(" ");
			p.append("type=\"");
			p.append("string");
			p.append("\"");
			p.append(">");

			p.append(response.getStatusLine());

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
			o.add("status", new JsonPrimitive(response.getStatusLine().getStatusCode()));
			o.add("message", new JsonPrimitive(response.getStatusLine().getReasonPhrase()));
			return o.toString() + "\n";
		}

		// application/sparql-results+json
		// application/sparql-results+xml
		// text/csv
		// text/tsv
		if (MoreMediaType.SPARQL_RESULTS_XML_TYPE.equals(type) || MoreMediaType.SPARQL_RESULTS_JSON_TYPE.equals(type)
				|| MoreMediaType.TEXT_CSV_TYPE.equals(type) || MoreMediaType.TEXT_TSV_TYPE.equals(type)) {
			return RendererFactory.getRenderer(ResultSetFactory.makeResults(asModel(response))).render(type, graphName, pref);
		}

		throw new CannotRenderException("Cannot render media type " + type.toString());
	}
	
	private Model asModel(HttpResponse response) {
		// more can be done clearly... but is it worth it?
		String NS_HTTP = "http://www.w3.org/2011/http#";
		Model bm = ModelFactory.createDefaultModel();
		bm.addLiteral(bm.createResource(), bm.createProperty(NS_HTTP, "statusCodeValue"),
				response.getStatusLine().getStatusCode());
		return bm;
	}
}
