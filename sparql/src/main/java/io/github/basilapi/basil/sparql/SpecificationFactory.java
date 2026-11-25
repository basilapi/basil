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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.jena.query.QueryException;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.update.UpdateFactory;

public class SpecificationFactory {

	private static final Pattern PREFIX_DECL = Pattern
			.compile("(?i)PREFIX\\s+([A-Za-z][\\w.-]*)?:\\s*<([^>]*)>");

	/**
	 * @param strict if true, a parameter in the $_name_prefix_type form whose
	 *               prefix is not declared in the query raises a
	 *               {@link ParameterParsingException}
	 */
	public static Specification create(String endpoint, String sparql) throws UnknownQueryTypeException {
		return build(endpoint, sparql, new ArrayList<String>());
	}

	/**
	 * @param strict if true, a parameter in the $_name_prefix_type form with an
	 *               undeclared prefix or an unknown XSD datatype raises a
	 *               {@link ParameterParsingException}
	 */
	public static Specification create(String endpoint, String sparql, boolean strict)
			throws UnknownQueryTypeException, ParameterParsingException {
		List<String> errors = new ArrayList<String>();
		Specification spec = build(endpoint, sparql, errors);
		if (strict && !errors.isEmpty()) {
			throw new ParameterParsingException(String.join("; ", errors));
		}
		return spec;
	}

	private static Specification build(String endpoint, String sparql, List<String> datatypeErrors)
			throws UnknownQueryTypeException {
		boolean isUpdate = QueryType.isUpdate(sparql);
		Map<String, String> prefixes = prefixes(sparql, isUpdate);
		VariablesCollector collector = new VariablesCollector();
		collector.collect(sparql);
		Specification spec = new Specification();
		spec.setEndpoint(endpoint);
		if (isUpdate) {
			spec.setUpdate(sparql);
		} else {
			spec.setQuery(sparql);
		}
		for (String var : collector.getVariables()) {
			VariableParser parser = new VariableParser(var, prefixes);
			if (parser.isParameter()) {
				spec.map(var, parser.getParameter());
				if (parser.isDatatypeError()) {
					datatypeErrors.add(parser.getException().getMessage());
				}
			}
		}
		return spec;
	}

	private static Map<String, String> prefixes(String sparql, boolean isUpdate) {
		try {
			if (isUpdate) {
				return UpdateFactory.create(sparql).getPrefixMapping().getNsPrefixMap();
			}
			return QueryFactory.create(sparql).getPrefixMapping().getNsPrefixMap();
		} catch (QueryException e) {
			// Not parseable: read PREFIX declarations directly
			Map<String, String> map = new HashMap<String, String>();
			Matcher m = PREFIX_DECL.matcher(sparql);
			while (m.find()) {
				map.put(m.group(1) == null ? "" : m.group(1), m.group(2));
			}
			return map;
		}
	}
}