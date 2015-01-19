package uk.ac.open.kmi.stoner.sparql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SparqlVariableParserTest {

	private static Logger log = LoggerFactory
			.getLogger(SparqlVariableParserTest.class);

	@Rule
	public TestName method = new TestName();

	@Test
	public void isApiParameter() {
		log.info("Test {}", method.getMethodName());

		// A parameter
		Assert.assertTrue(new SparqlVariableParser("$_type").isParameter());
		Assert.assertTrue(new SparqlVariableParser("?_type").isParameter());
		Assert.assertTrue(new SparqlVariableParser("?_name").isParameter());
		Assert.assertTrue(new SparqlVariableParser("?_type_").isParameter());
		// Not a parameter
		Assert.assertFalse(new SparqlVariableParser("?type_").isParameter());
		Assert.assertFalse(new SparqlVariableParser("$type").isParameter());
		Assert.assertFalse(new SparqlVariableParser("$-type").isParameter());
		Assert.assertFalse(new SparqlVariableParser("type").isParameter());
	}

	@Test
	public void getParameterNameOk() {
		log.info("Test {}", method.getMethodName());

		Map<String, String> positives = new HashMap<String, String>();
		positives.put("$_name", "name");
		positives.put("$_name_", "name");
		positives.put("?_name_literal", "name");
		positives.put("$_name_xsd_int", "name");
		positives.put("?_name_ex_dtype", "name");
		positives.put("$_name_en", "name");
		positives.put("?_name_it", "name");
		positives.put("$_name_gr", "name");
		positives.put("?_type_iri", "type");
		for (Entry<String, String> entry : positives.entrySet()) {
			String expected = entry.getValue();
			String actual = "";
			try {
				actual = new SparqlVariableParser(entry.getKey())
						.getParameterName();
			} catch (NotAParameterException e) {
				Assert.assertTrue(false);
				log.error("", e);
			}
			Assert.assertEquals(expected, actual);
		}
	}

	@Test
	public void getParameterNameFailures() {
		log.info("Test {}", method.getMethodName());

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
				new SparqlVariableParser(var).getParameterName();
			} catch (NotAParameterException e) {
				continue;
			}
			log.error("NotAParameterException was expected: {}", var);
			Assert.assertTrue(false);
		}
	}

	@Test
	public void isForcedIri() {
		log.info("Test {}", method.getMethodName());
		Map<String, Boolean> tests = new HashMap<String, Boolean>();
		tests.put("?_name_iri", true);
		tests.put("?_name_iri_", true);
		tests.put("?_name_iri______", true);
		tests.put("?_name_literal", false);
		tests.put("?_name", false);
		tests.put("?_name_xsd_string", false);
		for (Entry<String, Boolean> var : tests.entrySet()) {
			boolean expected = var.getValue();
			boolean actual;
			try {
				actual = new SparqlVariableParser(var.getKey()).isForcedIri();
				Assert.assertEquals(expected, actual);
			} catch (NotAParameterException e) {
				Assert.assertTrue(false);
				log.error("", e);
			}
		}
	}
}
