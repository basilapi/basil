package uk.ac.open.kmi.basil.it;

import java.io.IOException;

import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CollectionTest extends BasilTestBase {
	private static final Logger log = LoggerFactory.getLogger(CollectionTest.class);

	@Rule
	public TestName name = new TestName();

	@Test
	public void askAnyGetJson() throws ParseException, ClientProtocolException, IOException {
		log.info("#{}", name.getMethodName());
		executor.execute(builder.buildGetRequest("/basil").withHeader("Accept", "*/*"));
		log.debug(" ... returned content: {}", executor.getContent());
		executor.assertStatus(200).assertContentType("application/json").assertContentRegexp("\\{.*\\}");
	}

	@Test
	public void askJsonGetJson() throws ParseException, ClientProtocolException, IOException {
		log.info("#{}", name.getMethodName());
		executor.execute(builder.buildGetRequest("/basil").withHeader("Accept", "application/json"));
		log.debug(" ... returned content: {}", executor.getContent());
		executor.assertStatus(200).assertContentType("application/json").assertContentRegexp("\\{.*\\}");
	}

	@Test
	public void askHtmlGet406() throws ParseException, ClientProtocolException, IOException {
		log.info("#{}", name.getMethodName());
		executor.execute(builder.buildGetRequest("/basil").withHeader("Accept", "application/any"));
		log.debug(" ... returned content: {}", executor.getContent());
		executor.assertStatus(406);
	}
}
