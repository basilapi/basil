package uk.ac.open.kmi.stoner.sparql;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Specification implements Serializable {
	private static final long serialVersionUID = 9010724117224824994L;

	private String endpoint;
	private String query;
	private Map<String, QueryParameter> mappings = new HashMap<String, QueryParameter>();

	public String getEndpoint() {
		return endpoint;
	}

	void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	public String getQuery() {
		return query;
	}

	void setQuery(String query) {
		this.query = query;
	}

	public Collection<QueryParameter> getParameters() {
		return Collections.unmodifiableCollection(mappings.values());
	}

	void map(String variable, QueryParameter parameter) {
		this.mappings.put(variable, parameter);
	}
	
	public QueryParameter getParameter(String name){
		return mappings.get(name);
	}
	
	public boolean hasParameter(String name){
		return mappings.containsKey(name);
	}
}
