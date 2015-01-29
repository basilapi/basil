package uk.ac.open.kmi.stoner.format;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
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

import uk.ac.open.kmi.stoner.sparql.Specification;
import uk.ac.open.kmi.stoner.sparql.TestUtils;
import uk.ac.open.kmi.stoner.sparql.VariablesBinder;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
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
		File tmpl = new File(getClass().getClassLoader()
				.getResource("./format/mustache1.tmpl").toURI());
		List<Map<String, Object>> items = new ArrayList<Map<String, Object>>();
		Map<String, Object> row = new HashMap<String, Object>();
		row.put("name", "Enrico");
		items.add(row);
		row = new HashMap<String, Object>();
		row.put("name", "Luca");
		items.add(row);
		Writer writer = new StringWriter();
		MustacheFactory mf = new DefaultMustacheFactory();
		Mustache mustache = mf.compile(new FileReader(tmpl), "mustache1");
		mustache.execute(writer, Items.create(items));
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
		MustacheFactory mf = new DefaultMustacheFactory();
		Mustache mustache = mf.compile(new StringReader(template),
				method.getMethodName());

		mustache.execute(writer, Items.create(rs));
		writer.flush();
		log.debug("\n{}", writer);
	}
}
