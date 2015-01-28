package uk.ac.open.kmi.stoner.store;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.open.kmi.stoner.sparql.QueryParameter;
import uk.ac.open.kmi.stoner.sparql.QueryParameter.Type;
import uk.ac.open.kmi.stoner.sparql.Specification;
import uk.ac.open.kmi.stoner.sparql.SpecificationFactory;
import uk.ac.open.kmi.stoner.sparql.TestUtils;

public class FileStoreTest {

	private static Logger log = LoggerFactory.getLogger(FileStoreTest.class);

	private FileStore store;
	private static File home;

	@BeforeClass
	public static void beforeClass() throws URISyntaxException {
		URL u = FileStoreTest.class.getClassLoader().getResource(".");
		home = new File(new File(u.toURI()), "FileStoreTest");
		if (!home.exists()) {
			home.mkdirs();
		}
	}

	@Before
	public void before() {
		log.info("{}", testName.getMethodName());
		store = new FileStore(home);
	}

	@Rule
	public TestName testName = new TestName();

	@Test
	public void serializeQueryParameter() throws IOException,
			ClassNotFoundException {
		QueryParameter param = TestUtils.buildQueryParameter("param1",
				Type.LangedLiteral, "en", null);
		store.write(testName.getMethodName(), param, "qp");
		QueryParameter param2 = (QueryParameter) store.read(
				testName.getMethodName(), "qp");
		Assert.assertTrue(param2.getName().equals(param.getName()));
	}

	@Test
	public void serializeSpecification() throws IOException,
			ClassNotFoundException {
		Specification spec = SpecificationFactory.create(
				"http://data.open.ac.uk/sparql",
				"SELECT * WHERE {?X a ?_type_iri}");
		store.write(testName.getMethodName(), spec, "spec");
		Specification spec2 = (Specification) store.read(
				testName.getMethodName(), "spec");
		Assert.assertTrue(spec.getEndpoint().equals(spec2.getEndpoint()));
		Assert.assertTrue(spec.getQuery().equals(spec2.getQuery()));
		Assert.assertTrue(spec.getParameters().iterator().next()
				.equals(spec2.getParameters().iterator().next()));
	}

	@Test
	public void saveAndLoadSpec() throws IOException {
		Specification spec = SpecificationFactory.create(
				"http://data.open.ac.uk/sparql",
				"SELECT * WHERE {?X a ?_type_iri}");
		store.saveSpec("myspecid", spec);
		Specification spec2 = store.loadSpec("myspecid");
		Assert.assertTrue(spec.getEndpoint().equals(spec2.getEndpoint()));
		Assert.assertTrue(spec.getQuery().equals(spec2.getQuery()));
		Assert.assertTrue(spec.getParameters().iterator().next()
				.equals(spec2.getParameters().iterator().next()));
	}

}
