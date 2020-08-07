package uk.ac.open.kmi.basil.sparql;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.open.kmi.basil.sparql.Specification;
import uk.ac.open.kmi.basil.sparql.VariablesBinder;
import uk.ac.open.kmi.basil.sparql.VariablesCollector;

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
