package uk.ac.open.kmi.basil.it;

import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.protocol.HttpClientContext;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthenticatedTestBase extends BasilTestBase {
	private static final Logger log = LoggerFactory.getLogger(AuthenticatedTestBase.class);
	protected static HttpClientContext context = null;
	protected static RequestConfig config = null;
	
	@BeforeClass
	public static void beforeClass() throws Exception {
		log.trace("Initializing authenticated test");
		// Authenticate and remembered session cookie (in context)
		context = HttpClientContext.create();
		config = RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build();
		authenticator.authenticate(BasilTestServer.getServerBaseUrl(), BasilTestServer.getBasilUser(), BasilTestServer.getBasilPassword(), context);
	}

	@Override
	protected RequestConfig getRequestConfig() {
		return config;
	}

	@Override
	protected HttpClientContext getClientContext() {
		// Reuse the authenticated context!
		return context;
	}
}
