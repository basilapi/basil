package uk.ac.open.kmi.stoner.sparql;

import java.io.IOException;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementWalker;

public class SparqlVariablesCollectorTest {

	private static Logger log = LoggerFactory
			.getLogger(SparqlVariablesCollectorTest.class);

	@Rule
	public TestName testName = new TestName();

	@Before
	public void before(){
		log.info("{}", testName.getMethodName());
	}
	
	@Test
	public void test() throws IOException {
		String query = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("./sparql/select-1.txt"), "UTF-8");
		log.info(" {}", query);
		Query q = QueryFactory.create(query);
		Element element = q.getQueryPattern();
		SparqlVariablesCollector collector = new SparqlVariablesCollector();
		ElementWalker.walk(element, collector);
		Set<String> vars = collector.getVariables();
		log.info(" > {}", vars);
        Assert.assertTrue(vars.contains("?course"));
        Assert.assertTrue(vars.contains("?location"));
        Assert.assertTrue(vars.contains("?_geoid"));
	}
}
