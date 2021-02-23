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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.basilapi.basil.sparql.Specification;
import io.github.basilapi.basil.sparql.VariablesBinder;
import io.github.basilapi.basil.view.rhino.RhinoMediator;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSet;

public class JavascriptTest {

	private static Logger log = LoggerFactory.getLogger(JavascriptTest.class);

	@Rule
	public TestName method = new TestName();

	@Before
	public void before() {
		log.info("{}", method.getMethodName());
	}

	@Test
	public void test() throws URISyntaxException, IOException {
		String tmpl = TestUtils.loadTemplate("javascript", "script_1");
		Context cx = Context.enter();
		try {
			Scriptable scope = cx.initStandardObjects();
			cx.evaluateString(scope, "var func = " + tmpl, "<cmd>", 1, null);
			Object fObj = scope.get("func", scope);
			if (fObj == Scriptable.NOT_FOUND) {
				log.error("x is not defined.");
			} else {
				log.info("Function: {}", Context.toString(fObj));
			}
			if (!(fObj instanceof Function)) {
				log.error("f is undefined or not a function.");
			} else {
				Function f = (Function) fObj;
				StringWriter writer = new StringWriter();
				RhinoMediator sb = new RhinoMediator();
				sb.init(writer);
				Object result = f.call(cx, scope, sb, new Object[] { sb });
				log.info("returns {}", result);
				Assert.assertTrue(writer.toString()
						.equals("this is a test"));
				Assert.assertTrue(result instanceof RhinoMediator);
			}
			;
		} finally {
			Context.exit();
		}
	}
	
	@Test
	public void script_2() throws Exception {
		String tmpl = TestUtils.loadTemplate("javascript", "script_2");
		Context cx = Context.enter();
		try {
			Scriptable scope = cx.initStandardObjects();
			cx.evaluateString(scope, "var func = " + tmpl, tmpl, 1, null);
			Object fObj = scope.get("func", scope);
//			if (fObj == Scriptable.NOT_FOUND) {
//				log.error("x is not defined.");
//			} else {
//				log.info("Function: {}", Context.toString(fObj));
//			}
//			if (!(fObj instanceof Function)) {
//				log.error("f is undefined or not a function.");
//			} else {
				Function f = (Function) fObj;
				StringWriter writer = new StringWriter();
				RhinoMediator sb = new RhinoMediator();
				sb.init(writer);
				sb.bindItems(buildSome());
				Object result = f.call(cx, scope, sb, new Object[] { sb });
				log.info("returns {}", result);
				String s = writer.toString();
				log.info("Content: {}", s);
				Assert.assertTrue(s
						.contains("this is a test"));
				Assert.assertTrue(result instanceof RhinoMediator);
//			}
			;
		} finally {
			Context.exit();
		}
	}
	
	@Test
	public void select_1() throws IOException, EngineExecutionException {
		Specification spec = TestUtils.loadQuery(method.getMethodName());
		String template = TestUtils.loadTemplate("javascript", method.getMethodName());
		VariablesBinder binder = new VariablesBinder(spec);
		binder.bind("geoid", "2328926");
		Query q = binder.toQuery();
		QueryExecution qe = QueryExecutionFactory.sparqlService(
				spec.getEndpoint(), q);
		final ResultSet rs = qe.execSelect();
		Writer writer = new StringWriter();
		Engine.RHINO.exec(writer, template, Items.create(rs));
		log.info("\n{}", writer);
	}
	
	@Test
	public void testEngine() throws IOException, EngineExecutionException{
		String tmpl = TestUtils.loadTemplate("javascript", "script_2");
		Writer writer = new StringWriter();
		Engine.RHINO.exec(writer, tmpl, buildSome());
		log.info("\n{}", writer);
	}

	private Items buildSome(){
		List<Map<String,String>> ls = new ArrayList<Map<String,String>>();
		HashMap<String,String> i;
		i = new HashMap<String,String>();
		i.put("p1", "abc");
		i.put("p4", "def");
		i.put("p3", "hijkl");
		i.put("p4", "mno");
		i = new HashMap<String,String>();
		i.put("p1", "pqr");
		i.put("p2", "stu");
		i.put("p3", "vwx");
		i.put("p4", "yz");
		i = new HashMap<String,String>();
		i.put("p1", "012");
		i.put("p2", "345");
		i.put("p3", "678");
		i.put("p4", "90");
		
		ls.add(i);
		return Items.create(ls);
	}
}
