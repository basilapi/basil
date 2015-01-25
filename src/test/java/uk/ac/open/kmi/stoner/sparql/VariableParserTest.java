package uk.ac.open.kmi.stoner.sparql;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VariableParserTest {

	private static Logger log = LoggerFactory
			.getLogger(VariableParserTest.class);

	@Rule
	public TestName method = new TestName();

	@Before
	public void before(){
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

		Assert.assertEquals("name", new VariableParser("$_name")
						.getParameterName());
		Assert.assertEquals("name", new VariableParser("$_name_")
		.getParameterName());
		Assert.assertEquals("name", new VariableParser("?_name_literal")
		.getParameterName());
		Assert.assertEquals("name", new VariableParser("$_name_xsd_int")
		.getParameterName());
		Assert.assertEquals("name", new VariableParser("?_name_ex_dtype")
		.getParameterName());
		Assert.assertEquals("name", new VariableParser("$_name_en")
		.getParameterName());
		Assert.assertEquals("name", new VariableParser("?_name_it")
		.getParameterName());
		Assert.assertEquals("name", new VariableParser("$_name_gr")
		.getParameterName());
		Assert.assertEquals("type", new VariableParser("?_type_iri")
		.getParameterName());
		Assert.assertEquals("name", new VariableParser("?__name_it")
		.getParameterName());
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
				new VariableParser(var).getParameterName();
			} catch (ParameterException e) {
				continue;
			}
			log.error("NotAParameterException was expected: {}", var);
			Assert.assertTrue(false);
		}
	}

	@Test
	public void isForcedIri() throws ParameterException {
		// True
		Assert.assertTrue(new VariableParser( "?_name_iri"  ).isForcedIri());
		Assert.assertTrue(new VariableParser( "?_name_iri_"  ).isForcedIri());
		Assert.assertTrue(new VariableParser( "?_name_iri_____"  ).isForcedIri());
		// False
		Assert.assertFalse(new VariableParser( "?_name_literal"  ).isForcedIri());
		Assert.assertFalse(new VariableParser( "?_name"  ).isForcedIri());
		Assert.assertFalse(new VariableParser( "?_name_string"  ).isForcedIri());
		Assert.assertFalse(new VariableParser( "?_name_xsd_iri"  ).isForcedIri());
	}

	@Test
	public void isForcedLiteral() throws ParameterException {
		Assert.assertTrue(new VariableParser("?_name_literal")
				.isForcedPlainLiteral());
		Assert.assertTrue(new VariableParser("$_type_literal")
				.isForcedPlainLiteral());
		Assert.assertTrue(new VariableParser("?_literal_literal")
				.isForcedPlainLiteral());

		Assert.assertFalse(new VariableParser("$_literal_iri")
				.isForcedPlainLiteral());
		Assert.assertFalse(new VariableParser("?_literal")
				.isForcedPlainLiteral());
	}

	@Test
	public void isForcedTypedLiteral() throws ParameterException {
		
		Assert.assertTrue(new VariableParser("?_name_rdf_HTML")
				.isForcedTypedLiteral());
		Assert.assertTrue(new VariableParser("$_type_ex_bob")
				.isForcedTypedLiteral());
		Assert.assertTrue(new VariableParser("?_literal_xsd_string")
				.isForcedTypedLiteral());

		Assert.assertTrue(new VariableParser("?_param_string")
				.isForcedTypedLiteral());
		Assert.assertTrue(new VariableParser("?_param_boolean")
				.isForcedTypedLiteral());
		Assert.assertTrue(new VariableParser("?_param_int")
				.isForcedTypedLiteral());
		Assert.assertTrue(new VariableParser("?_param_integer")
				.isForcedTypedLiteral());

		Assert.assertFalse(new VariableParser("$_literal_xsd")
				.isForcedTypedLiteral());
		Assert.assertFalse(new VariableParser("?_literal")
				.isForcedTypedLiteral());
	}

	@Test
	public void isPlain() throws ParameterException {
		
		Assert.assertTrue(new VariableParser("?_name").isPlain());
		Assert.assertTrue(new VariableParser("?_name______").isPlain());
		Assert.assertTrue(new VariableParser("?_name_accipicchia___")
				.isPlain());

		Assert.assertFalse(new VariableParser("?_literal_xsd_string")
				.isPlain());
		Assert.assertFalse(new VariableParser("$_literal_xsd_string")
				.isPlain());
		Assert.assertFalse(new VariableParser("$_iri_iri").isPlain());
		Assert.assertFalse(new VariableParser("?_literal_iri").isPlain());
		Assert.assertFalse(new VariableParser("?_rdf_html_html")
				.isPlain()); // this is var named 'rdf' with datatype html:html
								// ...
	}

	@Test
	public void isForcedLangedLiteral() throws ParameterException {
		
		// True:
		Assert.assertTrue(new VariableParser("?_name_en")
				.isForcedLangedLiteral());
		Assert.assertTrue(new VariableParser("?_name_it")
				.isForcedLangedLiteral());
		Assert.assertTrue(new VariableParser("?_name_es")
				.isForcedLangedLiteral());
		Assert.assertTrue(new VariableParser("?_name_ab")
				.isForcedLangedLiteral());
		// False:
		Assert.assertFalse(new VariableParser("?_name_abc")
				.isForcedLangedLiteral());
		Assert.assertFalse(new VariableParser("?_name_a")
				.isForcedLangedLiteral());
		Assert.assertFalse(new VariableParser("?_name_literal")
				.isForcedLangedLiteral());
		Assert.assertFalse(new VariableParser("$_type_literal")
				.isForcedLangedLiteral());
		Assert.assertFalse(new VariableParser("?_literal_literal")
				.isForcedLangedLiteral());

		Assert.assertFalse(new VariableParser("$_literal_iri")
				.isForcedPlainLiteral());
		Assert.assertFalse(new VariableParser("?_literal")
				.isForcedPlainLiteral());
	}
	
	@Test
	public void getDatatypePrefix() throws ParameterException{
		Assert.assertTrue(new VariableParser("$_paramname_xsd_string").getDatatypePrefix().equals("xsd"));
		Assert.assertTrue(new VariableParser("$_paramname_xsd_boolean").getDatatypePrefix().equals("xsd"));
		Assert.assertTrue(new VariableParser("$_paramname_en_string").getDatatypePrefix().equals("en"));
		Assert.assertTrue(new VariableParser("$_paramname_xsd_string").getDatatypePrefix().equals("xsd"));
	}
}
