package uk.ac.open.kmi.basil.it;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.TreeSet;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.shiro.config.Ini;
import org.apache.stanbol.commons.testing.http.RequestBuilder;
import org.apache.stanbol.commons.testing.http.RequestExecutor;
import org.apache.stanbol.commons.testing.jarexec.JarExecutor;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

/**
 * 
 * Most of the initial code has been learnt from Apache Stanbol's
 * integration-tests module.
 * 
 * https://svn.apache.org/repos/asf/stanbol/trunk/commons/testing/stanbol/src/
 * main/java/org/apache/stanbol/commons/testing/stanbol/StanbolTestBase.java
 * 
 * @author enridaga
 *
 */
public class BasilTestBase {
	public static final String TEST_SERVER_URL_PROP = "test.server.url";
	public static final String SERVER_READY_TIMEOUT_PROP = "server.ready.timeout.seconds";
	public static final String SERVER_READY_PROP_PREFIX = "server.ready.path";
	public static final String KEEP_JAR_RUNNING_PROP = "keepJarRunning";
	public static final String BASIL_CONFIGURATION_FILE = "basil.configurationFile";
	public static final String TEST_DB_INIT = "test.db.init";
	public static final String TEST_DB_INIT_SCRIPT = "test.db.init.script";

	protected static String serverBaseUrl;
	protected static String dbInit = "false";
	protected static String jdbcConnectionUrl = null;
	protected static String databaseName = null;
	protected static String user = null;
	protected static String password = null;

	private static final Logger log = LoggerFactory.getLogger(BasilTestBase.class);

	protected boolean serverReady = false;
	protected RequestBuilder builder;
	protected CloseableHttpClient httpClient = null;
	protected RequestExecutor executor;

	/**
	 * Overwrite to enable authentication for requests
	 * 
	 * @return the <code>username:password</code> or <code>null</code> to
	 *         deactivate authentication (default)
	 */
	protected String getCredentials() {
		return null;
	}

	@BeforeClass
	public static void initialize() throws Exception {
		dbInit = System.getProperty(TEST_DB_INIT);
		String conf = System.getProperty(BASIL_CONFIGURATION_FILE);
		log.info("Configuration: {}", conf);
		log.info("Init a test db: {}", dbInit);
		if ("true".equals(dbInit)) {
			log.debug("{} is true", TEST_DB_INIT);
			conf = createTestDb();
		}
		startServer(conf);
	}

	@AfterClass
	public static void cleanup() {
		if(!dbInit.equals("true")){
			return;
		}
		log.info("Cleaning up: dropping db {}", databaseName);
		try {
			// Delete the test Database
			Class.forName("com.mysql.jdbc.Driver");
			try (Connection conn = DriverManager.getConnection(jdbcConnectionUrl, user, password)) {
				try (Statement create = conn.createStatement()) {
					create.executeUpdate("DROP DATABASE `" + databaseName + "`");
					log.info("Database {} dropped", databaseName);
				}
			}
		} catch (Exception e) {
			log.error("IGNORED (cannot cleanup test db!)", e);
		}
	}

	/**
	 * Returns the location of the new configuration file
	 * 
	 * @return
	 * @throws Exception
	 */
	private static String createTestDb() throws Exception {
		log.info("Creating test db");
		final String inputConfigurationFile = System.getProperty(BASIL_CONFIGURATION_FILE);
		String str = "abcdefghijklmnopqrstuvwxyz1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		final String dbPostfix = '_' + RandomStringUtils.random(8, str.toCharArray());
		File f = new File(inputConfigurationFile);
		if (!f.exists() || !f.canRead()) {
			throw new Exception("Cannot use basil configuration file: " + f);
		}
		Ini ini = Ini.fromResourcePath(f.getAbsolutePath());
		jdbcConnectionUrl = new StringBuilder().append("jdbc:mysql://").append(ini.get("").get("ds.serverName"))
				.append(":").append(ini.get("").get("ds.port")).append("/").toString();
		databaseName = ini.get("").get("ds.databaseName") + dbPostfix;
		user = ini.get("").get("ds.user");
		password = ini.get("").get("ds.password");

		log.info("Attemp to init a test db {}", databaseName);

		Class.forName("com.mysql.jdbc.Driver");
		try (Connection conn = DriverManager.getConnection(jdbcConnectionUrl, user, password)) {
			try (Statement create = conn.createStatement()) {
				create.executeUpdate("CREATE DATABASE `" + databaseName + "`");
				log.info("Database {} created", databaseName);
			}
			try (Statement create = conn.createStatement()) {
				create.executeQuery("USE `" + databaseName + "`");
			}
			// Now init db
			Resource r = new FileSystemResource(System.getProperty(TEST_DB_INIT_SCRIPT));
			log.info("Running init script: {}", r);
			ScriptUtils.executeSqlScript(conn, r);
		}
		String newConfigurationFile = inputConfigurationFile + dbPostfix;
		log.info("Write configuration file for test server instance: {}", newConfigurationFile);
		Path from = Paths.get(inputConfigurationFile);
		Path to = Paths.get(newConfigurationFile);
		Charset charset = StandardCharsets.UTF_8;
		String content = new String(Files.readAllBytes(from), charset);
		content = content.replaceAll(ini.get("").get("ds.databaseName"), databaseName);
		Files.write(to, content.getBytes(charset));
		// Configuration file
		return newConfigurationFile;
	}

	public static synchronized void startServer(String configurationFile) throws Exception {
		log.info("Starting testing server");
		if (serverBaseUrl != null) {
			// concurrent initialization by loading subclasses
			return;
		}
		final String configuredUrl = System.getProperty(TEST_SERVER_URL_PROP);
		if (configuredUrl != null && !configuredUrl.trim().isEmpty()) {
			serverBaseUrl = configuredUrl;
			try {
				new URL(configuredUrl);
			} catch (MalformedURLException e) {
				log.error("Configured " + TEST_SERVER_URL_PROP + " = " + configuredUrl + "is not a valid URL!");
				throw e;
			}
			log.info(TEST_SERVER_URL_PROP + " is set: not starting server jar (" + serverBaseUrl + ")");
		} else {

			Properties properties = System.getProperties();
			// Add jvm option for basil configuration file
			String opts = properties.getProperty("jar.executor.vm.options");
			opts += " -Dbasil.configurationFile=" + configurationFile;
			properties.setProperty("jar.executor.vm.options", opts);
			final JarExecutor j = JarExecutor.getInstance(properties);
			j.start();
			serverBaseUrl = "http://localhost:" + j.getServerPort();
			log.info("Forked subprocess server listening to: " + serverBaseUrl);

			// Optionally block here so that the runnable jar stays up - we can
			// then run tests against it from another VM
			if ("true".equals(System.getProperty(KEEP_JAR_RUNNING_PROP))) {
				log.info(KEEP_JAR_RUNNING_PROP + " set to true - entering infinite loop"
						+ " so that runnable jar stays up. Kill this process to exit.");
				while (true) {
					Thread.sleep(1000L);
				}
			}
		}
	}

	@Before
	public void waitForServerReady() throws Exception {
		log.debug("> before {}#waitForServerReady()", getClass().getSimpleName());
		// initialize instance request builder and HTTP client
		builder = new RequestBuilder(serverBaseUrl);
		// TODO:user name and pwd
		String credentials = getCredentials();
		if (credentials != null && !credentials.isEmpty()) {
			CredentialsProvider credsProvider = new BasicCredentialsProvider();
			credsProvider.setCredentials(new AuthScope(HttpHost.create(serverBaseUrl)),
					new UsernamePasswordCredentials(credentials));
			httpClient = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build();
		} else {
			httpClient = HttpClients.createDefault();
		}
		executor = new RequestExecutor(httpClient);

		if (serverReady) {
			log.debug(" ... server already marked as ready!");
			return;
		}

		// Timeout for readiness test
		final String sec = System.getProperty(SERVER_READY_TIMEOUT_PROP);
		final int timeoutSec = sec == null ? 60 : Integer.valueOf(sec);
		log.info("Will wait up to " + timeoutSec + " seconds for server to become ready");
		final long endTime = System.currentTimeMillis() + timeoutSec * 1000L;

		// Get the list of paths to test and expected content regexps
		final List<String> testPaths = new ArrayList<String>();
		final TreeSet<Object> propertyNames = new TreeSet<Object>();
		propertyNames.addAll(System.getProperties().keySet());
		for (Object o : propertyNames) {
			final String key = (String) o;
			if (key.startsWith(SERVER_READY_PROP_PREFIX)) {
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
				final String url = serverBaseUrl + path;
				log.debug("    > url: {}", url);
				log.debug("    > content: {}", substring);
				final HttpGet get = new HttpGet(url);
				// authenticate as admin with password admin
				// XXX let it use the default credentials, if any
				// get.setHeader("Authorization", "Basic YWRtaW46YWRtaW4=");
				for (int i = 2; i + 1 < s.length; i = i + 2) {
					log.debug("    > header: {}:{}", s[i], s[i + 1]);
					if (s[i] != null && !s[i].isEmpty() && s[i + 1] != null && !s[i + 1].isEmpty()) {
						get.setHeader(s[i], s[i + 1]);
					}
				}
				CloseableHttpResponse response = null;
				HttpEntity entity = null;
				try {
					log.debug("    > execute: {}", get);
					response = httpClient.execute(get);
					log.debug("    > response: {}", response);
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
}
