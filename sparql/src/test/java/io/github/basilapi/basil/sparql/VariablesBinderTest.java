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

public class VariablesBinderTest {

	private static Logger log = LoggerFactory
			.getLogger(VariablesBinderTest.class);

	private VariablesBinder binder;
	
	@Rule
	public TestName method = new TestName();

	@Before
	public void before() {
		log.info("{}", method.getMethodName());
	}

	@Test
	public void select_1() throws IOException {
		Specification spec = TestUtils.loadQuery(method.getMethodName());
		log.debug("before: \n{}\n", spec.getQuery());
		Assert.assertTrue(spec.hasVariable("?_geoid"));
		Assert.assertTrue(spec.hasParameter("geoid"));
		binder = new VariablesBinder(spec, "geoid", "2328926");
		log.debug("after: \n{}\n", binder.toString());
		VariablesCollector vars = new VariablesCollector();
		vars.collect(binder.toString());
		Assert.assertFalse(vars.getVariables().contains("?_geoid"));
	}

	@Test
	public void select_2() throws IOException{
		Specification spec = TestUtils.loadQuery(method.getMethodName());
		log.debug("before: \n{}\n", spec.getQuery());
		Assert.assertTrue(spec.hasParameter("term"));
		Assert.assertTrue(spec.hasVariable("?_term"));
		binder = new VariablesBinder(spec);
		binder.bind( "term", "earthquake");
		log.debug("after: \n{}\n", binder.toString());
		VariablesCollector vars = new VariablesCollector();
		vars.collect(binder.toString());
		Assert.assertFalse(vars.getVariables().contains("?_term"));
	}
	
	@Test
	public void select_12() throws IOException{
		Specification spec = TestUtils.loadQuery(method.getMethodName());
		log.info("before: \n{}\n", spec.getQuery());
		Assert.assertTrue(spec.hasParameter("limit"));
		Assert.assertTrue(spec.hasVariable("?_limit_number"));
		binder = new VariablesBinder(spec);
		binder.bind( "limit", "11");
		log.info("after: \n{}\n", binder.toString());
		VariablesCollector vars = new VariablesCollector();
		vars.collect(binder.toString());
		Assert.assertFalse(vars.getVariables().contains("?_limit_number"));
	}

	@Test
	public void select_13() throws IOException{
		Specification spec = TestUtils.loadQuery(method.getMethodName());
		log.info("before: \n{}\n", spec.getQuery());
		Assert.assertTrue(spec.hasParameter("limit"));
		Assert.assertTrue(spec.hasVariable("?_limit_number"));
		Assert.assertTrue(spec.hasVariable("?_offset_number"));
		binder = new VariablesBinder(spec);
		binder.bind( "limit", "10");
		binder.bind( "offset", "10");
		log.info("after: \n{}\n", binder.toString());
		VariablesCollector vars = new VariablesCollector();
		vars.collect(binder.toString());
		Assert.assertFalse(vars.getVariables().contains("?_limit_number"));
		Assert.assertFalse(vars.getVariables().contains("?_offset_number"));
	}
}
