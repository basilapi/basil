package uk.ac.open.kmi.stoner.sparql;

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
		binder = new VariablesBinder(spec, "_geoid", "2328926");
		VariablesCollector vars = new VariablesCollector();
		vars.collect(binder.toString());
		Assert.assertFalse(vars.getVariables().contains("_geoid"));
	}
	
	
}
