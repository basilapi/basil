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

package io.github.basilapi.basil.sparql;

import org.apache.jena.query.QueryException;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Specification implements Serializable {
	private static final long serialVersionUID = 9010724117224824994L;

	private String endpoint;
	private String query;
	private Map<String, QueryParameter> variablesParameters = new HashMap<String, QueryParameter>();
	private Map<String, String> parametersVariables = new HashMap<String, String>();

	private boolean isUpdate = false;

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
		this.isUpdate = false;
		this.query = query;
	}
	
	void setUpdate(String update) {
		this.isUpdate = true;
		this.query = update;
	}

	public Collection<QueryParameter> getParameters() {
		return Collections.unmodifiableCollection(variablesParameters.values());
	}

	void map(String variable, QueryParameter parameter) {
		this.variablesParameters.put(variable, parameter);
		this.parametersVariables.put(parameter.getName(), variable);
	}

	public QueryParameter getParameter(String name) {
		return variablesParameters.get(parametersVariables.get(name));
	}

	public QueryParameter getParameterOfVariable(String variable) {
		return variablesParameters.get(variable);
	}

	public String getVariableOfParameter(String param) {
		return parametersVariables.get(param);
	}

	public boolean hasParameter(String name) {
		return parametersVariables.containsKey(name);
	}

	public boolean hasVariable(String name) {
		return variablesParameters.containsKey(name);
	}

	public boolean isUpdate() {
		return isUpdate;
	}

	public String getExpandedQuery(){
		String expandedQuery = null;
		try {
			org.apache.jena.query.Query q = QueryFactory.create(getQuery());
			q.setPrefixMapping(null);
			expandedQuery = q.toString();
		} catch (QueryException qe) {
			// may be update
			try {
				UpdateRequest q = UpdateFactory.create(getQuery());
				q.setPrefixMapping(null);
				expandedQuery = q.toString();
			} catch (QueryException qe2) {
				// some parameterized queries are not supported by the SPARQL 1.1 parser (e.g.
				// Insert data { ?_uri ...)
				// In those cases we just keep the original syntax
				expandedQuery = getQuery();
			}

		}
		return expandedQuery;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof Specification && ((Specification) obj).getEndpoint().equals(this.getEndpoint()) && ((Specification) obj).getQuery().equals(this.getQuery());
	}
}
