package uk.ac.open.kmi.basil.it;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.impl.client.HttpClients;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthenticatedTestBase extends BasilTestBase {
	private static final Logger log = LoggerFactory.getLogger(AuthenticatedTestBase.class);
	protected static HttpClientContext context = null;
	protected static RequestConfig config = null;
	@BeforeClass
	public static void beforeClass() throws Exception {
		// Authenticate and remembed session cookie (in context)
		context = HttpClientContext.create();
		config = RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build();
		authenticate();
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

	protected static void authenticate() throws Exception {
		log.info("Authenticating");
		HttpClient httpClient = HttpClients.createDefault();
		HttpPost post = new HttpPost(BasilTestServer.getServerBaseUrl() + "/basil/auth/login");
		post.addHeader("Content-type", "application/json");
		BasicHttpEntity e = new BasicHttpEntity();
		e.setContent(IOUtils.toInputStream("{username: \"" + BasilTestServer.getBasilUser() + "\", password: \""
				+ BasilTestServer.getBasilPassword() + "\"}"));
		post.setEntity(e);
		httpClient.execute(post, context);
	}
}
