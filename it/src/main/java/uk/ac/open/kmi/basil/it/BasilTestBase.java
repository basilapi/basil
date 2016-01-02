package uk.ac.open.kmi.basil.it;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.stanbol.commons.testing.http.RequestBuilder;
import org.apache.stanbol.commons.testing.http.RequestExecutor;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * 
 * Most of the initial code has been taken from Apache Stanbol's
 * integration-tests module.
 * 
 * https://svn.apache.org/repos/asf/stanbol/trunk/commons/testing/stanbol/src/
 * main/java/org/apache/stanbol/commons/testing/stanbol/StanbolTestBase.java
 * 
 * @author enridaga
 *
 */
public class BasilTestBase {

	private static final Logger log = LoggerFactory.getLogger(BasilTestBase.class);

	protected boolean serverReady = false;
	protected RequestBuilder builder;
	protected CloseableHttpClient httpClient = null;
	protected RequestExecutor executor;

	/**
	 * Override to add features
	 * 
	 * @return
	 */
	protected RequestConfig getRequestConfig() {
		return RequestConfig.DEFAULT;
	}

	/**
	 * Override to customize
	 * 
	 * @return
	 */
	protected HttpClientContext getClientContext() {
		return HttpClientContext.create();
	}

	@Before
	public void prepare() throws Exception {
		// initialize instance request builder and HTTP client
		builder = new RequestBuilder(BasilTestServer.getServerBaseUrl());
		httpClient = HttpClients.custom().setDefaultRequestConfig(getRequestConfig()).build();
		executor = new RequestExecutor(httpClient);
		//
		waitForServerReady();
	}

	public void waitForServerReady() throws Exception {
		log.debug("> before {}#waitForServerReady()", getClass().getSimpleName());

		if (serverReady) {
			log.debug(" ... server already marked as ready!");
			return;
		}

		// Timeout for readiness test
		final String sec = BasilTestServer.getServerReadyTimeout();
		final int timeoutSec = sec == null ? 60 : Integer.valueOf(sec);
		log.info("Will wait up to " + timeoutSec + " seconds for server to become ready");
		final long endTime = System.currentTimeMillis() + timeoutSec * 1000L;

		// Get the list of paths to test and expected content regexps
		final List<String> testPaths = new ArrayList<String>();
		final TreeSet<Object> propertyNames = new TreeSet<Object>();
		propertyNames.addAll(System.getProperties().keySet());
		for (Object o : propertyNames) {
			final String key = (String) o;
			if (key.startsWith(BasilTestServer.SERVER_READY_PROP_PREFIX)) {
				testPaths.add(System.getProperty(key));
			}
		}

		// Consider the server ready if it responds to a GET on each of
		// our configured request paths with a 200 result and content
		// that matches the regexp supplied with the path
		long sleepTime = 100;
		readyLoop: while (!serverReady && System.currentTimeMillis() < endTime) {
			// Wait a bit between checks, to let the server come up
			Thread.sleep(sleepTime);
			sleepTime = Math.min(5000L, sleepTime * 2);

			// A test path is in the form path:substring or just path, in which
			// case
			// we don't check that the content contains the substring
			log.debug(" - check serverReady Paths");
			for (String p : testPaths) {
				final String[] s = p.split(":");
				final String path = s[0];
				final String substring = (s.length > 1 ? s[1] : null);
				final String url = BasilTestServer.getServerBaseUrl() + path;
				log.debug("  > url: {}", url);
				log.debug("  > content: {}", substring);
				final HttpGet get = new HttpGet(url);
				// XXX let it use the default credentials, if any
				// get.setHeader("Authorization", "Basic YWRtaW46YWRtaW4=");
				for (int i = 2; i + 1 < s.length; i = i + 2) {
					log.debug("  > header: {}:{}", s[i], s[i + 1]);
					if (s[i] != null && !s[i].isEmpty() && s[i + 1] != null && !s[i + 1].isEmpty()) {
						get.setHeader(s[i], s[i + 1]);
					}
				}
				CloseableHttpResponse response = null;
				HttpEntity entity = null;
				try {
					log.debug("  > execute: {}", get);
					response = httpClient.execute(get);
					log.debug("  > response: {}", response);
					entity = response.getEntity();
					final int status = response.getStatusLine().getStatusCode();
					if (status != 200) {
						log.info("Got {} at {} - will retry", status, url);
						continue readyLoop;
					} else {
						log.debug("Got {} at {} - will retry", status, url);
					}

					if (substring != null) {
						if (entity == null) {
							log.info("No entity returned for {} - will retry", url);
							continue readyLoop;
						}
						final String content = EntityUtils.toString(entity);
						if (!content.contains(substring)) {
							log.info("Returned content for {}  does not contain " + "{} - will retry", url, substring);
							continue readyLoop;
						}
					}
				} catch (HttpHostConnectException e) {
					log.info("Got HttpHostConnectException at " + url + " - will retry");
					continue readyLoop;
				} finally {
					EntityUtils.consumeQuietly(entity);
					if (response != null) {
						response.close();
					}
				}
			}
			serverReady = true;
			log.info("Got expected content for all configured requests, server is ready");
		}

		if (!serverReady) {
			throw new Exception("Server not ready after " + timeoutSec + " seconds");
		}
	}

	@After
	public void closeExecutor() {
		executor.close();
	}

	protected static String loadQueryString(String qname) throws IOException {
		return IOUtils.toString(BasilTestBase.class.getClassLoader().getResourceAsStream("./sparql/" + qname + ".txt"),
				"UTF-8");
	}

	protected static String extractEndpoint(String qname) {
		int pos = qname.indexOf("X-Basil-Endpoint:");
		int len = ("X-Basil-Endpoint:").length();
		int eol = qname.indexOf('\n', pos);
		return qname.substring(pos + len, eol).trim();
	}

	protected HttpPut buildPutSpec(String name) throws Exception {
		String q = loadQueryString(name);
		HttpPut put = new HttpPut(BasilTestServer.getServerBaseUrl() + "/basil");
		BasicHttpEntity entity = new BasicHttpEntity();
		entity.setContentEncoding("UTF-8");
		entity.setContent(IOUtils.toInputStream(q, "UTF-8"));
		put.setHeader("X-Basil-Endpoint", extractEndpoint(q));
		put.setEntity(entity);
		return put;
	}

	protected void assertIsJson(String str) {
		JsonParser p = new JsonParser();
		try {
			p.parse(str);
		} catch (JsonSyntaxException e) {
			log.error("Response body is not valid Json!");
			Assert.assertTrue(false);
		}
	}
}
