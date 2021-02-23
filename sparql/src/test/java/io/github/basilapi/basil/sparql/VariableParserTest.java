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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.vocabulary.XSD;

public class VariableParserTest {

	private static Logger log = LoggerFactory
			.getLogger(VariableParserTest.class);

	@Rule
	public TestName method = new TestName();

	@Before
	public void before() {
		log.info("{}", method.getMethodName());
	}

	@Test
	public void isApiParameter() {

		// A parameter
		Assert.assertTrue(new VariableParser("$_type").isParameter());
		Assert.assertTrue(new VariableParser("?_type").isParameter());
		Assert.assertTrue(new VariableParser("?_name").isParameter());
		Assert.assertTrue(new VariableParser("?_type_").isParameter());
		// Not a parameter
		Assert.assertFalse(new VariableParser("?type_").isParameter());
		Assert.assertFalse(new VariableParser("$type").isParameter());
		Assert.assertFalse(new VariableParser("$-type").isParameter());
		Assert.assertFalse(new VariableParser("type").isParameter());
	}

	@Test
	public void getParameterNameOk() throws ParameterException {

		Assert.assertEquals("name", new VariableParser("$_name").getParameter()
				.getName());
		Assert.assertEquals("name", new VariableParser("$_name_")
				.getParameter().getName());
		Assert.assertEquals("name", new VariableParser("?_name_literal")
				.getParameter().getName());
		Assert.assertEquals("name", new VariableParser("$_name_xsd_int")
				.getParameter().getName());
		Assert.assertEquals("name", new VariableParser("?_name_ex_dtype")
				.getParameter().getName());
		Assert.assertEquals("name", new VariableParser("$_name_en")
				.getParameter().getName());
		Assert.assertEquals("name", new VariableParser("?_name_it")
				.getParameter().getName());
		Assert.assertEquals("name", new VariableParser("$_name_gr")
				.getParameter().getName());
		Assert.assertEquals("type", new VariableParser("?_type_iri")
				.getParameter().getName());
		Assert.assertEquals("name", new VariableParser("?__name_it")
				.getParameter().getName());
	}

	@Test
	public void getParameterNameFailures() {

		List<String> negatives = new ArrayList<String>();
		negatives.add("$x");
		negatives.add("?name_literal");
		negatives.add("$name_xsd_int");
		negatives.add("?name_ex_dtype");
		negatives.add("$name_en");
		negatives.add("?name_it");
		negatives.add("$name_gr");
		negatives.add("?type_iri");
		for (String var : negatives) {
			try {
				new VariableParser(var).getParameter().getName();
			} catch (NullPointerException e) {
				continue;
			}
			log.error("NotAParameterException was expected: {}", var);
			Assert.assertTrue(false);
		}
	}

	@Test
	public void isIri() throws ParameterException {
		// True
		Assert.assertTrue(new VariableParser("?_name_iri").getParameter()
				.isIri());
		Assert.assertTrue(new VariableParser("?_name_iri_").getParameter()
				.isIri());
		Assert.assertTrue(new VariableParser("?_name_iri_____").getParameter()
				.isIri());
		// False
		Assert.assertFalse(new VariableParser("?_name_literal").getParameter()
				.isIri());
		Assert.assertFalse(new VariableParser("?_name").getParameter().isIri());
		Assert.assertFalse(new VariableParser("?_name_string").getParameter()
				.isIri());
		Assert.assertFalse(new VariableParser("?_name_xsd_iri").getParameter()
				.isIri());
	}

	@Test
	public void isLiteral() throws ParameterException {
		Assert.assertTrue(new VariableParser("?_name_literal").getParameter()
				.isPlainLiteral());
		Assert.assertTrue(new VariableParser("$_type_literal").getParameter()
				.isPlainLiteral());
		Assert.assertTrue(new VariableParser("?_literal_literal")
				.getParameter().isPlainLiteral());
		Assert.assertTrue(new VariableParser("?_literal").getParameter()
				.isPlainLiteral());
		// False
		Assert.assertFalse(new VariableParser("$_literal_iri").getParameter()
				.isPlainLiteral());

	}

	@Test
	public void isTypedLiteral() throws ParameterException {

		Assert.assertTrue(new VariableParser("?_literal_xsd_string")
				.getParameter().isTypedLiteral());

		Assert.assertTrue(new VariableParser("?_param_string").getParameter()
				.isTypedLiteral());
		Assert.assertTrue(new VariableParser("?_param_boolean").getParameter()
				.isTypedLiteral());
		Assert.assertTrue(new VariableParser("?_param_int").getParameter()
				.isTypedLiteral());
		Assert.assertTrue(new VariableParser("?_param_integer").getParameter()
				.isTypedLiteral());

		Assert.assertFalse(new VariableParser("$_literal_xsd").getParameter()
				.isTypedLiteral());
		Assert.assertFalse(new VariableParser("?_literal").getParameter()
				.isTypedLiteral());

		// Undefined prefixes
		Assert.assertTrue(new VariableParser("?_name_rdf_HTML").isError());
		Assert.assertTrue(new VariableParser("$_type_ex_bob").isError());

	}

	@Test
	public void isLangedLiteral() throws ParameterException {

		// True:
		Assert.assertTrue(new VariableParser("?_name_en").getParameter()
				.isLangedLiteral());
		Assert.assertTrue(new VariableParser("?_name_it").getParameter()
				.isLangedLiteral());
		Assert.assertTrue(new VariableParser("?_name_es").getParameter()
				.isLangedLiteral());
		Assert.assertTrue(new VariableParser("?_name_ab").getParameter()
				.isLangedLiteral());
		// False:
		Assert.assertFalse(new VariableParser("?_name_abc").getParameter()
				.isLangedLiteral());
		Assert.assertFalse(new VariableParser("?_name_a").getParameter()
				.isLangedLiteral());
		Assert.assertFalse(new VariableParser("?_name_literal").getParameter()
				.isLangedLiteral());
		Assert.assertFalse(new VariableParser("$_type_literal").getParameter()
				.isLangedLiteral());
		Assert.assertFalse(new VariableParser("?_literal_literal")
				.getParameter().isLangedLiteral());

		Assert.assertFalse(new VariableParser("$_literal_iri").getParameter()
				.isLangedLiteral());
		Assert.assertFalse(new VariableParser("?_literal").getParameter()
				.isLangedLiteral());
	}

	@Test
	public void getDatatype() throws ParameterException {
		Assert.assertTrue(new VariableParser("$_paramname_xsd_string")
				.getParameter().getDatatype()
				.equals(XSDDatatype.XSDstring.getURI()));
		Assert.assertTrue(new VariableParser("$_paramname_xsd_boolean")
				.getParameter().getDatatype()
				.equals(XSDDatatype.XSDboolean.getURI()));

		Map<String, String> prefixes = new HashMap<String, String>();
		prefixes.put("en", XSD.getURI());
		Assert.assertTrue(new VariableParser("$_paramname_en_string", prefixes)
				.getParameter().getDatatype()
				.equals(XSDDatatype.XSDstring.getURI()));
	}
}
