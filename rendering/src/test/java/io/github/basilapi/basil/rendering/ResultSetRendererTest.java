package uk.ac.open.kmi.basil.rendering;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

public class ResultSetRendererTest {
	final static Logger log = LoggerFactory.getLogger(ResultSetRendererTest.class);
	final static Map<String, String> pm = new HashMap<String, String>();
	private static ResultSetRenderer r = null;
	private static ResultSet rs;

	@Rule
	public TestName test = new TestName();

	@BeforeClass
	public static void beforeClass() {
		Model m = ModelFactory.createDefaultModel();
		m.read(ResultSetRendererTest.class.getClassLoader().getResourceAsStream("foaf.rdf"), "");
		rs = ResultSetFactory.makeResults(m);
		r = new ResultSetRenderer(rs);
	}

	@Test
	public void getInput() {
		Assert.assertEquals(rs, r.getInput());
	}

	@Test
	public void render() {
		List<Exception> failures = new ArrayList<Exception>();
		for (MediaType m : MoreMediaType.MediaTypes) {
			log.debug("Testing support {}", m);
			try {
				r.stream(m, "", pm);
			} catch (CannotRenderException e) {
				failures.add(e);
			}
		}
		for (Exception e : failures)
			log.error("Failed: {}", e.getMessage());
		Assert.assertEquals(failures.size(), 0);

	}

}
