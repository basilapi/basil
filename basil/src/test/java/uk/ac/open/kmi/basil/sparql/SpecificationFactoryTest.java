package uk.ac.open.kmi.basil.sparql;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpecificationFactoryTest {
	private static Logger log = LoggerFactory.getLogger(SpecificationFactoryTest.class);

	@Rule
	public TestName method = new TestName();

	@Before
	public void before() {
		log.info("{}", method.getMethodName());
	}

	@Test
	public void create() throws IOException, UnknownQueryTypeException {
		String q = "# X-Basil-Endpoint: http://data.open.ac.uk/sparql\n" + 
				"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" + 
				"PREFIX mlo: <http://purl.org/net/mlo/> \n" + 
				"PREFIX aiiso: <http://purl.org/vocab/aiiso/schema#> \n" + 
				"\n" + 
				"# Eg value for ?_geoid 2328926\n" + 
				"SELECT ?course FROM <http://data.open.ac.uk/context/course> \n" + 
				"WHERE {\n" + 
				" BIND(iri(CONCAT('http://sws.geonames.org/',?_geoid,'/')) as ?location) .\n" + 
				" ?course mlo:location ?location . ?course a aiiso:Module \n" + 
				"}";
		String endpoint = TestUtils.endpoint(q);
		Specification spec = SpecificationFactory.create(endpoint, q);
		Assert.assertTrue(spec.getEndpoint().equals(endpoint));
		Assert.assertTrue(spec.getQuery().equals(q));
		
	}
	
	@Test
	public void createUpdate() throws IOException, UnknownQueryTypeException {
		String q = "# X-Basil-Endpoint: http://data.open.ac.uk/sparql\n" + 
				"PREFIX dc: <http://purl.org/dc/elements/1.1/>\n" + 
				"INSERT { <http://example/egbook> dc:title  \"This is an example title\" } WHERE {}\n" + 
				"";
		String endpoint = TestUtils.endpoint(q);
		Specification spec = SpecificationFactory.create(endpoint, q);
		Assert.assertTrue(spec.getEndpoint().equals(endpoint));
		Assert.assertTrue(spec.getQuery().equals(q));
	}
	
}
