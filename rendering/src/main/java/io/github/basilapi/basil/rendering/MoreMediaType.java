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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;

public class MoreMediaType {
	public static final MediaType RDFXML_TYPE = new MediaType("application",
			"rdf+xml");
	public static final MediaType RDFJSON_TYPE = new MediaType("application",
			"rdf+json");
	public static final MediaType JSONLD_TYPE = new MediaType("application",
			"ld+json");
	public static final MediaType TEXT_TURTLE_TYPE = new MediaType("text",
			"turtle");
	public static final MediaType APPLICATION_TURTLE_TYPE = new MediaType(
			"application", "turtle");
	public static final MediaType NTRIPLES_TYPE = new MediaType("application",
			"n-triples");
	public static final MediaType TEXT_X_NQUADS_TYPE = new MediaType("text",
			"x-nquads");
	public static final MediaType TEXT_TSV_TYPE = new MediaType("text",
			"tsv");
	public static final MediaType TEXT_CSV_TYPE = new MediaType("text",
			"csv");
	public static final MediaType SPARQL_RESULTS_XML_TYPE = new MediaType(
			"application", "sparql-results+xml");
	public static final MediaType SPARQL_RESULTS_JSON_TYPE = new MediaType(
			"application", "sparql-results+json");

	public final static Map<String, MediaType> extensions = new HashMap<String, MediaType>() {
		private static final long serialVersionUID = 1L;
		public MediaType remove(Object key) {
			throw new UnsupportedOperationException();
		}
	};
	
	public final static boolean isRDF(MediaType type){
		return (MoreMediaType.RDFJSON_TYPE.equals(type) || MoreMediaType.JSONLD_TYPE.equals(type) ||
		MoreMediaType.RDFXML_TYPE.equals(type) || MoreMediaType.TEXT_TURTLE_TYPE.equals(type)
		|| MoreMediaType.APPLICATION_TURTLE_TYPE.equals(type) ||
		MoreMediaType.NTRIPLES_TYPE.equals(type) ||
		MoreMediaType.TEXT_X_NQUADS_TYPE.equals(type));
	}
	
	static {
		extensions.put("txt", MediaType.TEXT_PLAIN_TYPE);
		extensions.put("xml", MediaType.APPLICATION_XML_TYPE);
		extensions.put("rdf", MoreMediaType.RDFXML_TYPE);
		extensions.put("json", MediaType.APPLICATION_JSON_TYPE);
		extensions.put("jrdf", MoreMediaType.RDFJSON_TYPE);
		extensions.put("sparql-json", MoreMediaType.SPARQL_RESULTS_JSON_TYPE);
		extensions.put("sparql-xml", MoreMediaType.SPARQL_RESULTS_XML_TYPE);
		extensions.put("ttl", MoreMediaType.TEXT_TURTLE_TYPE);
		extensions.put("nt", MoreMediaType.NTRIPLES_TYPE);
		extensions.put("nq", MoreMediaType.TEXT_X_NQUADS_TYPE);
		extensions.put("jsonld", MoreMediaType.JSONLD_TYPE);
		extensions.put("tsv", MoreMediaType.TEXT_TSV_TYPE);
		extensions.put("csv", MoreMediaType.TEXT_CSV_TYPE);
	}

	 public final static List<MediaType> MediaTypes = Arrays.asList(MediaType.TEXT_PLAIN_TYPE,
				MoreMediaType.NTRIPLES_TYPE, MediaType.TEXT_XML_TYPE,
				MediaType.APPLICATION_XML_TYPE,
				MediaType.APPLICATION_JSON_TYPE, MoreMediaType.RDFJSON_TYPE,
				MoreMediaType.RDFXML_TYPE,
				MoreMediaType.APPLICATION_TURTLE_TYPE,
				MoreMediaType.TEXT_TURTLE_TYPE,
				MoreMediaType.TEXT_X_NQUADS_TYPE,
				MoreMediaType.SPARQL_RESULTS_JSON_TYPE,
				MoreMediaType.SPARQL_RESULTS_XML_TYPE,
				MoreMediaType.TEXT_CSV_TYPE, MoreMediaType.TEXT_TSV_TYPE);
}
