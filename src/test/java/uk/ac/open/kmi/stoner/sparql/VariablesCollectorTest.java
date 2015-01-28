package uk.ac.open.kmi.stoner.sparql;

import java.io.IOException;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.junit.After;
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

public class VariablesCollectorTest {

	private static Logger log = LoggerFactory
			.getLogger(VariablesCollectorTest.class);

	private VariablesCollector collector = new VariablesCollector();
	
	@Rule
	public TestName testName = new TestName();

	@Before
	public void before(){
		log.info("{}", testName.getMethodName());
	}

	@After
	public void after(){
		collector.reset();
	}
	
	private String loadQuery(String qname) throws IOException{
		return IOUtils.toString(getClass().getClassLoader().getResourceAsStream("./sparql/" + qname + ".txt"), "UTF-8");
	}
	
	private Set<String> extractVars(String query){
		log.debug(" {}", query);
		Query q = QueryFactory.create(query);
		Element element = q.getQueryPattern();		
		ElementWalker.walk(element, collector);
		Set<String> vars = collector.getVariables();
		log.debug(" > {}", vars);
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
}
