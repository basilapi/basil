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

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.vocabulary.XSD;

public class VariableParser {

	// group 1: _ or __ (optional); group 2: name; group 3: type / lang / prefix;
	// group 4: datatype local name, which may contain underscores (e.g. amount_due)
	private static final Pattern PARAMETER = Pattern.compile(
			"[\\$\\?]([_]{1,2})([^_]+)_?([a-zA-Z0-9]+)?_?([a-zA-Z0-9]+(?:_[a-zA-Z0-9]+)*)?.*$");

	private String variable;
	private boolean isParameter = false;
	private final Map<String, String> prefixes;
	private boolean isError = false;
	private boolean isDatatypeError = false;
	private Exception exception = null;
	private QueryParameter p = null;

	public VariableParser(String variable) {
		this(variable, null);
	}

	/**
	 * @param prefixes the query's prefix mapping; xsd is added if not declared
	 */
	public VariableParser(String variable, Map<String, String> prefixes) {
		this.variable = variable;
		this.prefixes = new HashMap<String, String>();
		if (prefixes != null) {
			this.prefixes.putAll(prefixes);
		}
		this.prefixes.putIfAbsent("xsd", XSD.getURI());
		parse();
	}

	private static boolean isKnownDatatype(String uri) {
		return TypeMapper.getInstance().getTypeByName(uri) != null;
	}

	private void error(String message, boolean datatype) {
		this.isError = true;
		this.isDatatypeError = datatype;
		this.exception = new ParameterException(message);
	}

	private void parse() {
		Matcher m = PARAMETER.matcher(this.variable);
		if (!m.matches()) {
			return; // not a parameter
		}
		this.isParameter = true;
		this.p = new QueryParameter();
		p.setOptional(m.group(1).length() == 2);
		p.setName(m.group(2));
		String g3 = m.group(3);
		String g4 = m.group(4);
		if (g3 == null) {
			p.setPlainLiteral();
		} else if (g3.equalsIgnoreCase("iri")) {
			p.setIri();
		} else if (g3.equalsIgnoreCase("literal")) {
			p.setPlainLiteral();
		} else if (g3.equalsIgnoreCase("number")) {
			p.setNumber();
		} else if (g3.length() == 2 && g4 == null) {
			// language tag
			p.setLang(g3.toLowerCase());
		} else if (g4 != null) {
			// $_name_prefix_localName: the prefix must be declared in the query (xsd is default)
			String namespace = prefixes.get(g3);
			if (namespace == null) {
				error("Unknown prefix '" + g3 + "' in parameter " + variable, true);
				p.setPlainLiteral();
			} else {
				// Any local name: the datatype IRI is built as is
				p.setDatatype(namespace + g4);
			}
		} else if (isKnownDatatype(XSD.getURI() + g3)) {
			// $_name_xsdLocalName: any XSD datatype known to Jena
			p.setDatatype(XSD.getURI() + g3);
		} else {
			// Unknown single suffix: plain literal, as before (not a datatype error)
			error("Cannot recognize parameter properties: " + variable, false);
			p.setPlainLiteral();
		}
	}

	public boolean isParameter() {
		return isParameter;
	}

	public QueryParameter getParameter() {
		return p;
	}

	public String getVariable() {
		return variable;
	}

	public boolean isError() {
		return this.isError;
	}

	/**
	 * True when the $_name_prefix_type form uses a prefix not declared in the query.
	 */
	public boolean isDatatypeError() {
		return this.isDatatypeError;
	}

	public Exception getException() {
		return this.exception;
	}
}
