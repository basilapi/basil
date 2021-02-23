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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueryParameterTest {

	private static Logger log = LoggerFactory
			.getLogger(QueryParameterTest.class);

	private QueryParameter parameter;

	@Rule
	public TestName method = new TestName();

	@Before
	public void before() {
		log.info("{}", method.getMethodName());
		parameter = new QueryParameter();
	}

	@Test
	public void getName() {
		parameter.setName("myname");
		Assert.assertTrue(parameter.getName().equals("myname"));
	}

	@Test
	public void isIri() {
		parameter.setIri();
		Assert.assertTrue(parameter.isIri()); // True
		Assert.assertFalse(parameter.isLangedLiteral());
		Assert.assertFalse(parameter.isTypedLiteral());
		Assert.assertFalse(parameter.isPlainLiteral());
		Assert.assertNull(parameter.getDatatype());
		Assert.assertNull(parameter.getLang());
	}

	@Test
	public void isPlainLiteral() {
		parameter.setPlainLiteral();
		Assert.assertFalse(parameter.isIri());
		Assert.assertFalse(parameter.isLangedLiteral());
		Assert.assertFalse(parameter.isTypedLiteral());
		Assert.assertTrue(parameter.isPlainLiteral()); // True
		Assert.assertNull(parameter.getDatatype());
		Assert.assertNull(parameter.getLang());
	}

	@Test
	public void isLangedLiteral() {
		parameter.setLang("en");
		Assert.assertFalse(parameter.isIri());
		Assert.assertTrue(parameter.isLangedLiteral()); // True
		Assert.assertFalse(parameter.isTypedLiteral());
		Assert.assertFalse(parameter.isPlainLiteral()); 
		Assert.assertNull(parameter.getDatatype());
		Assert.assertEquals("en",parameter.getLang()); // en
	}

	@Test
	public void isTypedLiteral() {
		parameter.setDatatype("http://www.example.org/datatype");
		Assert.assertFalse(parameter.isIri());
		Assert.assertFalse(parameter.isLangedLiteral());
		Assert.assertTrue(parameter.isTypedLiteral()); // True
		Assert.assertFalse(parameter.isPlainLiteral()); 
		Assert.assertNotNull(parameter.getDatatype());
		Assert.assertEquals("http://www.example.org/datatype", parameter.getDatatype());
		Assert.assertNull(parameter.getLang());
	}

	
	@Test
	public void equals() {
		QueryParameter parameter2 = new QueryParameter();
		parameter.setName("myname");
		parameter2.setName("myname");
		Assert.assertTrue(parameter.equals(parameter2));
		Assert.assertTrue(parameter2.equals(parameter2));
		Assert.assertTrue(parameter2.equals(parameter));
		
		parameter.setIri();
		Assert.assertFalse(parameter.equals(parameter2));
		Assert.assertFalse(parameter2.equals(parameter));
		parameter2.setIri();
		Assert.assertTrue(parameter.equals(parameter2));
		Assert.assertTrue(parameter2.equals(parameter2));
		Assert.assertTrue(parameter.equals(parameter));
		
		parameter.setLang("it");
		parameter2.setLang("en");
		Assert.assertFalse(parameter.equals(parameter2));
		Assert.assertFalse(parameter2.equals(parameter));
		parameter2.setLang("it");
		Assert.assertTrue(parameter.equals(parameter2));
		Assert.assertTrue(parameter2.equals(parameter2));
		Assert.assertTrue(parameter.equals(parameter));
		
		parameter.setDatatype("http://www.example.org/datatype");
		parameter2.setDatatype("http://www.example.org/datatype2");
		Assert.assertFalse(parameter.equals(parameter2));
		Assert.assertFalse(parameter2.equals(parameter));
		parameter2.setLang("en");
		Assert.assertFalse(parameter.equals(parameter2));
		Assert.assertFalse(parameter2.equals(parameter));
		parameter2.setDatatype("http://www.example.org/datatype");
		Assert.assertTrue(parameter.equals(parameter2));
		Assert.assertTrue(parameter2.equals(parameter2));
		Assert.assertTrue(parameter.equals(parameter));
		
	}
}
