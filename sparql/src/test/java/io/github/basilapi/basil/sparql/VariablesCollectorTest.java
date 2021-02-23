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

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementWalker;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VariablesCollectorTest {

	private static Logger log = LoggerFactory.getLogger(VariablesCollectorTest.class);

	private VariablesCollector collector = new VariablesCollector();

	@Rule
	public TestName testName = new TestName();

	@Before
	public void before() {
		log.info("{}", testName.getMethodName());
	}

	@After
	public void after() {
		collector.reset();
	}

	private String loadQuery(String qname) throws IOException {
		return IOUtils.toString(getClass().getClassLoader().getResourceAsStream("./sparql/" + qname + ".txt"), "UTF-8");
	}

	private Set<String> extractVars(String query) {
		log.debug(" {}", query);
		Query q = QueryFactory.create(query);
		Element element = q.getQueryPattern();
		ElementWalker.walk(element, collector);
		Set<String> vars = collector.getVariables();
		log.debug(" > {}", vars);
		return vars;
	}

	private Set<String> extractVarsUpdate(String query) {
		log.info(" {}", query);
		Set<String> vars = new HashSet<String>();
		VariablesCollector collector = new VariablesCollector();
		collector.collect(query);
		vars = collector.getVariables();
		log.info(" > {}", vars);
		return vars;
	}

	@Test
	public void select_1() throws IOException {
		Set<String> vars = extractVars(loadQuery(testName.getMethodName()));
		Assert.assertTrue(vars.contains("?course"));
		Assert.assertTrue(vars.contains("?location"));
		Assert.assertTrue(vars.contains("?_geoid"));
	}

	@Test
	public void select_2() throws IOException {
		Set<String> vars = extractVars(loadQuery(testName.getMethodName()));
		Assert.assertTrue(vars.contains("?description"));
		Assert.assertTrue(vars.contains("?thing"));
		Assert.assertTrue(vars.contains("?_term"));
	}

	@Test
	public void select_3() throws IOException {
		Set<String> vars = extractVars(loadQuery(testName.getMethodName()));
		Assert.assertTrue(vars.contains("?x"));
		Assert.assertTrue(vars.contains("?_code_literal"));
		Assert.assertTrue(vars.contains("?title"));
		Assert.assertTrue(vars.contains("?url"));
		Assert.assertTrue(vars.contains("?apply"));
		Assert.assertTrue(vars.contains("?numCredits"));
		Assert.assertTrue(vars.contains("?description"));
	}

	@Test
	public void select_4() throws IOException {
		Set<String> vars = extractVars(loadQuery(testName.getMethodName()));
		Assert.assertTrue(vars.contains("?_x_iri"));
	}

	@Test
	public void select_8() throws IOException {
		Set<String> vars = extractVarsUpdate(loadQuery(testName.getMethodName()));
		Assert.assertTrue(vars.contains("?_limit_number"));
	}

	@Test
	public void insert_1() throws IOException {
		Set<String> vars = extractVarsUpdate(loadQuery(testName.getMethodName()));
		//
		Assert.assertTrue(vars.size() == 1);
		Assert.assertTrue(vars.contains("?_title"));
	}

	@Test
	public void insert_2() throws IOException {
		Set<String> vars = extractVarsUpdate(loadQuery(testName.getMethodName()));
		Assert.assertTrue(vars.size() == 2);
		Assert.assertTrue(vars.contains("?_title_string"));
		Assert.assertTrue(vars.contains("?_creator"));
	}

	@Test
	public void insert_3() throws IOException {
		Set<String> vars = extractVarsUpdate(loadQuery(testName.getMethodName()));
		Assert.assertTrue(vars.size() == 1);
		Assert.assertTrue(vars.contains("?_price_number"));
	}

	@Test
	public void delete_1() throws IOException {
		Set<String> vars = extractVarsUpdate(loadQuery(testName.getMethodName()));
		Assert.assertTrue(vars.size() == 2);
		Assert.assertTrue(vars.contains("?_title"));
		Assert.assertTrue(vars.contains("?_creator"));
	}

	@Test
	public void delete_2() throws IOException {
		Set<String> vars = extractVarsUpdate(loadQuery(testName.getMethodName()));
		Assert.assertTrue(vars.size() == 5);
		Assert.assertTrue(vars.contains("?_date_dateTime"));
	}

	@Test
	public void delete_3() throws IOException {
		Set<String> vars = extractVarsUpdate(loadQuery(testName.getMethodName()));
		Assert.assertTrue(vars.size() == 4);
		Assert.assertTrue(vars.contains("?_givenName"));
	}	

	@Test
	public void delete_insert_1() throws IOException {
		Set<String> vars = extractVarsUpdate(loadQuery(testName.getMethodName()));
		Assert.assertTrue(vars.size() == 2);
		Assert.assertTrue(vars.contains("?_delete"));
		Assert.assertTrue(vars.contains("?_insert"));
	}

	@Test
	public void delete_insert_2() throws IOException {
		Set<String> vars = extractVarsUpdate(loadQuery(testName.getMethodName()));
		Assert.assertTrue(vars.contains("?_delete"));
		Assert.assertTrue(vars.contains("?_insert"));
	}

	
}
