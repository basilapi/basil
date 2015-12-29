package uk.ac.open.kmi.basil.it;

import java.io.IOException;

import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CollectionTest extends BasilTestBase {
	private static final Logger log = LoggerFactory.getLogger(CollectionTest.class);

	@Rule
	public TestName name = new TestName();

	@Test
	public void getAskAnyGetJson() throws ParseException, ClientProtocolException, IOException {
		log.info("IT#{}", name.getMethodName());
		executor.execute(builder.buildGetRequest("/basil").withHeader("Accept", "*/*"));
		log.debug(" ... returned content: {}", executor.getContent());
		executor.assertStatus(200)
		.assertContentType("application/json").assertContentRegexp("\\{.*\\}");
	}

	@Test
	public void getAskJsonGetJson() throws ParseException, ClientProtocolException, IOException {
		log.info("IT#{}", name.getMethodName());
		executor.execute(builder.buildGetRequest("/basil").withHeader("Accept", "application/json"));
		log.debug(" ... returned content: {}", executor.getContent());
		executor.assertStatus(200).assertContentType("application/json").assertContentRegexp("\\{.*\\}");
	}

	@Test
	public void getAskHtmlGet406() throws ParseException, ClientProtocolException, IOException {
		log.info("IT#{}", name.getMethodName());
		executor.execute(builder.buildGetRequest("/basil").withHeader("Accept", "application/any"));
		log.debug(" ... returned content: {}", executor.getContent());
		executor.assertStatus(406);
	}
}
