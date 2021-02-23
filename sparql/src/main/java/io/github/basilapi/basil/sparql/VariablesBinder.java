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

import org.apache.jena.datatypes.BaseDatatype;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.Query;
import org.apache.jena.update.UpdateRequest;

/**
 * To bind parameter values to variables in the SPARQL query.
 * 
 * @author enridaga
 *
 */
public class VariablesBinder {

	private ParameterizedSparqlString pss;
	private Specification spec;

	/**
	 * Constructor.
	 * 
	 * @param spec
	 * @param bindings
	 *            - Optional as sequence of strings:
	 *            param,value,param2,value2...
	 * @see Specification
	 */
	public VariablesBinder(Specification spec, String... bindings) {
		this.spec = spec;
		this.pss = new ParameterizedSparqlString(spec.getQuery());
		for (int x = 0; x < bindings.length; x += 2) {
			bind(bindings[x], bindings[x + 1]);
		}

	}

	/**
	 * Binds a value to the parameter {@code name}. Delegetes to one of the
	 * specialized methods depending on the associated {@link QueryParameter}.
	 * Defaults to {@link #bindPlainLiteral(String, String)}.
	 * 
	 * @param name The name of the basil variable
	 * @param value The value to be assigned to this basil variable
	 * @return a reference to this object.
	 * @see #bindIri(String, String)
	 * @see #bindLangedLiteral(String, String, String)
	 * @see #bindPlainLiteral(String, String)
	 * @see #bindNumber(String, String)
	 * @see #bindTypedLiteral(String, String, String)
	 */
	public VariablesBinder bind(String name, String value) {
		if (spec.hasParameter(name)) {
			QueryParameter p = spec.getParameter(name);
			if (p.isIri()) {
				bindIri(name, value);
			} else if (p.isLangedLiteral()) {
				bindLangedLiteral(name, value, p.getLang());
			} else if (p.isTypedLiteral()) {
				bindTypedLiteral(name, value, p.getDatatype());
			} else if (p.isNumber()) {
				bindNumber(name, value);
			} else {
				// Default is PlainLiteral
				bindPlainLiteral(name, value);
			}
		}
		return this;
	}

	/**
	 * Binds a value as typed literal.
	 * 
	 * @param name The name of the basil variable
	 * @param value The value to be assigned to the variable
	 * @param datatype Teh datatype annotation to attach to the typed literal
	 * @return a reference to this object.
	 */
	public VariablesBinder bindTypedLiteral(String name, String value,
			String datatype) {
		pss.setLiteral(spec.getVariableOfParameter(name), value,
				new BaseDatatype(datatype));
		return this;
	}

	/**
	 * Binds a value as typed literal (xsd:double or xsd:integer).
	 *
	 * @param name The name of the basil variable
	 * @param value The value to be assigned to the variable
	 * @return a reference to this object.
	 */
	public VariablesBinder bindNumber(String name, String value) {
		if (value.contains(".")) {
			pss.setLiteral(spec.getVariableOfParameter(name), Double.valueOf(value));	
		}else{
			pss.setLiteral(spec.getVariableOfParameter(name), Integer.valueOf(value));
		}
		return this;
	}

	/**
	 * Binds a value as plain literal.
	 *
	 * @param name The name of the basil variable
	 * @param value The value to be assigned to the variable
	 * @return a reference to this object.
	 */
	public VariablesBinder bindPlainLiteral(String name, String value) {
		pss.setLiteral(spec.getVariableOfParameter(name), value);
		return this;
	}

	/**
	 * Alias of {@link #bindPlainLiteral}
	 *
	 * @param name The name of the basil variable
	 * @param value The value to be assigned to the variable
	 * @see #bindPlainLiteral
	 * @return a reference to this object.
	 */
	public VariablesBinder bindLiteral(String name, String value) {
		return this.bindPlainLiteral(name, value);
	}

	/**
	 * Binds a value as langed literal.
	 *
	 * @param name The name of the basil variable
	 * @param value The value to be assigned to the literal
	 * @param lang The language annotation to be attached to the literal
	 * @return a reference to this object.
	 */
	public VariablesBinder bindLangedLiteral(String name, String value,
			String lang) {
		pss.setLiteral(spec.getVariableOfParameter(name), value, lang);
		return this;
	}

	/**
	 * Binds a value as IRI.
	 *
	 * @param name The name of the basil variable
	 * @param value The value to be assigned to the literal
	 * @return a reference to this object.
	 */
	public VariablesBinder bindIri(String name, String value) {
		pss.setIri(spec.getVariableOfParameter(name), value);
		return this;
	}

	/**
	 * Returns the query with the replaced bindings.
	 * 
	 * @return the query as String.
	 * 
	 */
	public String toString() {
		return toQuery().toString();
	}

	/**
	 * Returns the query with the replaced bindings.
	 * 
	 * @return the query as Query.
	 */
	public Query toQuery() {
		return pss.asQuery();
	}
	
	/**
	 * Returns the update with the replaced bindings.
	 * 
	 * @return the query as UpdateRequest.
	 */
	public UpdateRequest toUpdate() {
		return pss.asUpdate();
	}
}
