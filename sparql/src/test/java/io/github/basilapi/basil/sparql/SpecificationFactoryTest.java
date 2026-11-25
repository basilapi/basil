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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpecificationFactoryTest {
	private static Logger log = LoggerFactory.getLogger(SpecificationFactoryTest.class);

	@Rule
	public TestName method = new TestName();

	@Before
	public void before() {
		log.info("{}", method.getMethodName());
	}

	@Test
	public void create() throws IOException, UnknownQueryTypeException {
		String q = "# X-Basil-Endpoint: http://data.open.ac.uk/sparql\n" + 
				"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" + 
				"PREFIX mlo: <http://purl.org/net/mlo/> \n" + 
				"PREFIX aiiso: <http://purl.org/vocab/aiiso/schema#> \n" + 
				"\n" + 
				"# Eg value for ?_geoid 2328926\n" + 
				"SELECT ?course FROM <http://data.open.ac.uk/context/course> \n" + 
				"WHERE {\n" + 
				" BIND(iri(CONCAT('http://sws.geonames.org/',?_geoid,'/')) as ?location) .\n" + 
				" ?course mlo:location ?location . ?course a aiiso:Module \n" + 
				"}";
		String endpoint = TestUtils.endpoint(q);
		Specification spec = SpecificationFactory.create(endpoint, q);
		Assert.assertTrue(spec.getEndpoint().equals(endpoint));
		Assert.assertTrue(spec.getQuery().equals(q));
		
	}
	
	@Test
	public void createUpdate() throws IOException, UnknownQueryTypeException {
		String q = "# X-Basil-Endpoint: http://data.open.ac.uk/sparql\n" + 
				"PREFIX dc: <http://purl.org/dc/elements/1.1/>\n" + 
				"INSERT { <http://example/egbook> dc:title  \"This is an example title\" } WHERE {}\n" + 
				"";
		String endpoint = TestUtils.endpoint(q);
		Specification spec = SpecificationFactory.create(endpoint, q);
		Assert.assertTrue(spec.getEndpoint().equals(endpoint));
		Assert.assertTrue(spec.getQuery().equals(q));
	}
	private static final String Q141 = "PREFIX ex: <http://example.org/dt#>\n"
			+ "SELECT ?v WHERE { BIND($_x_ex_money AS ?v) }";

	@Test
	public void issue141_prefixFromQuery() throws Exception {
		Specification spec = SpecificationFactory.create("", Q141, true);
		Assert.assertEquals("http://example.org/dt#money", spec.getParameter("x").getDatatype());
	}

	@Test(expected = ParameterParsingException.class)
	public void issue141_strictUndeclaredPrefix() throws Exception {
		SpecificationFactory.create("", "SELECT ?v WHERE { BIND($_x_ex_money AS ?v) }", true);
	}

	@Test
	public void issue141_lenientUndeclaredPrefix() throws Exception {
		Specification spec = SpecificationFactory.create("", "SELECT ?v WHERE { BIND($_x_ex_money AS ?v) }");
		Assert.assertTrue(spec.getParameter("x").isPlainLiteral());
	}

	@Test
	public void issue148_serviceParameterDisabled() throws Exception {
		System.clearProperty(SpecificationFactory.SERVICE_PARAMETERS_PROPERTY);
		Specification spec = SpecificationFactory.create("",
				"SELECT * WHERE { SERVICE $_endpoint_iri { ?s ?p ?o } }", true);
		Assert.assertFalse(spec.hasParameter("endpoint"));
	}

	@Test
	public void issue148_serviceParameterEnabled() throws Exception {
		System.setProperty(SpecificationFactory.SERVICE_PARAMETERS_PROPERTY, "true");
		try {
			// No suffix: still bound as IRI
			Specification spec = SpecificationFactory.create("",
					"SELECT * WHERE { SERVICE $_endpoint { ?s ?p ?o } }", true);
			Assert.assertTrue(spec.getParameter("endpoint").isIri());
			VariablesBinder b = new VariablesBinder(spec);
			b.bind("endpoint", "http://example.org/sparql");
			Assert.assertTrue(b.toString(), b.toString().contains("SERVICE <http://example.org/sparql>"));
		} finally {
			System.clearProperty(SpecificationFactory.SERVICE_PARAMETERS_PROPERTY);
		}
	}
}
