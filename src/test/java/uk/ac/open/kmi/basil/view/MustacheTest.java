package uk.ac.open.kmi.basil.view;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.open.kmi.basil.sparql.Specification;
import uk.ac.open.kmi.basil.sparql.TestUtils;
import uk.ac.open.kmi.basil.sparql.VariablesBinder;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.ResultSet;

public class MustacheTest {

	private static Logger log = LoggerFactory.getLogger(MustacheTest.class);

	@Rule
	public TestName method = new TestName();

	@Before
	public void before() {
		log.info("{}", method.getMethodName());
	}

	@Test
	public void test() throws URISyntaxException, IOException {
		String tmpl = TestUtils.loadTemplate("mustache1");
		List<Map<String, Object>> items = new ArrayList<Map<String, Object>>();
		Map<String, Object> row = new HashMap<String, Object>();
		row.put("name", "Enrico");
		items.add(row);
		row = new HashMap<String, Object>();
		row.put("name", "Luca");
		items.add(row);
		Writer writer = new StringWriter();
		Engine.MUSTACHE.exec(writer, tmpl, Items.create(items));
		writer.flush();
		log.debug("\n{}", writer);
	}

	@Test
	public void select_1() throws IOException {
		Specification spec = TestUtils.loadQuery(method.getMethodName());
		String template = TestUtils.loadTemplate(method.getMethodName());
		VariablesBinder binder = new VariablesBinder(spec);
		binder.bind("geoid", "2328926");

		Query q = binder.toQuery();
		QueryExecution qe = QueryExecutionFactory.sparqlService(
				spec.getEndpoint(), q);
		final ResultSet rs = qe.execSelect();
		Writer writer = new StringWriter();
		Engine.MUSTACHE.exec(writer, template, Items.create(rs));
		writer.flush();
		log.debug("\n{}", writer);
	}
}
