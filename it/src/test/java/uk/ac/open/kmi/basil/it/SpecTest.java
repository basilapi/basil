package uk.ac.open.kmi.basil.it;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.BasicHttpEntity;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SpecTest extends AuthenticatedTestBase {
	private static final Logger log = LoggerFactory.getLogger(CollectionTest.class);

	@Rule
	public TestName name = new TestName();

	@Test
	public void Spec1_Create() throws Exception {
		log.info("#{}", name.getMethodName());
		String body = loadQueryString("select_1");
		HttpPut put = new HttpPut(BasilTestServer.getServerBaseUrl() + "/basil");
		put.addHeader("X-Basil-Endpoint", extractEndpoint(body));
		BasicHttpEntity entity = new BasicHttpEntity();
		entity.setContent(IOUtils.toInputStream(body));
		put.setEntity(entity);
		httpClient.execute(put, context);
	}
}
