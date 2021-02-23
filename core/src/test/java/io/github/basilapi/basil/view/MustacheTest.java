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

package io.github.basilapi.basil.view;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.basilapi.basil.TestUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.basilapi.basil.sparql.Specification;
import io.github.basilapi.basil.sparql.VariablesBinder;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSet;

public class MustacheTest {

	private static Logger log = LoggerFactory.getLogger(MustacheTest.class);

	@Rule
	public TestName method = new TestName();

	@Before
	public void before() {
		log.info("{}", method.getMethodName());
	}

	@Test
	public void test() throws URISyntaxException, IOException,
			EngineExecutionException {
		String tmpl = TestUtils.loadTemplate("mustache", "mustache1");
		List<Map<String, String>> items = new ArrayList<Map<String, String>>();
		Map<String, String> row = new HashMap<String, String>();
		row.put("name", "Enrico");
		items.add(row);
		row = new HashMap<String, String>();
		row.put("name", "Luca");
		items.add(row);
		Writer writer = new StringWriter();
		Engine.MUSTACHE.exec(writer, tmpl, Items.create(items));
		writer.flush();
		log.debug("\n{}", writer);
	}

	@Test
	public void select_1() throws IOException, EngineExecutionException {
		Specification spec = TestUtils.loadQuery(method.getMethodName());
		String template = TestUtils.loadTemplate("mustache",
				method.getMethodName());
		VariablesBinder binder = new VariablesBinder(spec);
		binder.bind("geoid", "2328926");

		Query q = binder.toQuery();
		QueryExecution qe = QueryExecutionFactory.sparqlService(
				spec.getEndpoint(), q);
		final ResultSet rs = qe.execSelect();
		Writer writer = new StringWriter();
		Engine.MUSTACHE.exec(writer, template, Items.create(rs));
		writer.flush();
		log.debug("\n{}", writer);
	}
}
