package uk.ac.open.kmi.stoner.sparql;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Specification implements Serializable {
	private static final long serialVersionUID = 9010724117224824994L;

	private String endpoint;
	private String query;
	private Set<QueryParameter> parameters;

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

	public Set<QueryParameter> getParameters() {
		return Collections.unmodifiableSet(parameters);
	}

	void setParameters(Set<QueryParameter> parameters) {
		this.parameters = new HashSet<QueryParameter>();
		this.parameters.addAll(parameters);
	}
}
