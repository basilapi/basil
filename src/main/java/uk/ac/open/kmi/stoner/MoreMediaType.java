package uk.ac.open.kmi.stoner;

import java.util.HashMap;
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
	public static final MediaType SPARQL_RESULTS_XML_TYPE = new MediaType(
			"application", "sparql-results+xml");
	public static final MediaType SPARQL_RESULTS_JSON_TYPE = new MediaType(
			"application", "sparql-results+json");

	public final static Map<String, MediaType> extensions = new HashMap<String, MediaType>() {
		private static final long serialVersionUID = 1L;
		public MediaType remove(Object key) {
			throw new UnsupportedOperationException();
		};
	};
	static {
		extensions.put("txt", MediaType.TEXT_PLAIN_TYPE);
		extensions.put("xml", MediaType.APPLICATION_XML_TYPE);
		extensions.put("json", MediaType.APPLICATION_JSON_TYPE);
		extensions.put("sparql-json", MoreMediaType.SPARQL_RESULTS_JSON_TYPE);
		extensions.put("sparql-xml", MoreMediaType.SPARQL_RESULTS_XML_TYPE);
		extensions.put("ttl", MoreMediaType.TEXT_TURTLE_TYPE);
		extensions.put("nt", MoreMediaType.NTRIPLES_TYPE);
		extensions.put("nq", MoreMediaType.TEXT_X_NQUADS_TYPE);
		extensions.put("jsonld", MoreMediaType.JSONLD_TYPE);
	}

}
