package uk.ac.open.kmi.basil.it;

import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ExecutionTest extends AuthenticatedTestBase {
	private static final Logger log = LoggerFactory.getLogger(CollectionTest.class);
	
	@Rule
	public TestName name = new TestName();
	public void waitForServerReady() throws Exception {
		log.debug("> before {}#waitForServerReady()", getClass().getSimpleName());

		if (serverReady) {
			log.debug(" ... server already marked as ready!");
			return;
		}else{
			BasilTestServer.waitForServerReady(httpClient);
		}

		FusekiTestServer.waitForServerReady(httpClient);
	}
	
	@Test
	public void EXEC01_CreateWriteAPI() throws Exception {
		log.info("#{}", name.getMethodName());
		log.info("Not implemented yet");
	}
}
