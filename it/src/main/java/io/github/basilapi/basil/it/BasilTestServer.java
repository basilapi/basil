/*
 * Copyright (c) 2021. Enrico Daga and Luca Panziera
 *
 * MLicensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.github.basilapi.basil.it;

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
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.shiro.authc.credential.DefaultPasswordService;
import org.apache.shiro.config.Ini;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

public class BasilTestServer {
	// System Properties
	public static final String TEST_SERVER_URL_PROP = "test.server.url";
	public static final String TEST_SERVER_USER_PROP = "test.server.user";
	public static final String TEST_SERVER_PWD_PROP = "test.server.pwd";
	public static final String SERVER_READY_TIMEOUT_PROP = "server.ready.timeout.seconds";
	public static final String KEEP_JAR_RUNNING_PROP = "keepJarRunning";
	public static final String BASIL_CONFIGURATION_FILE_PROP = "basil.configurationFile";
	public static final String TEST_DB_INIT_PROP = "test.db.init";
	public static final String TEST_DB_INIT_SCRIPT_PROP = "test.db.init.script";
	//
	public static final String SERVER_READY_PROP_PREFIX = "server.ready.path";

	// MySQL
	private static String dbInit = System.getProperty(TEST_DB_INIT_PROP);
	private static String jdbcConnectionUrl = null;
	private static String databaseName = null;
	private static String user = null;
	private static String password = null;

	// Basil Server
	private static String serverBaseUrl = null;
	private static String serverConfiguration = System.getProperty(BASIL_CONFIGURATION_FILE_PROP);
	private static String basilUser = System.getProperty(TEST_SERVER_USER_PROP);
	private static String basilPassword = System.getProperty(TEST_SERVER_PWD_PROP);
	private static String serverReadyTimeout = System.getProperty(SERVER_READY_TIMEOUT_PROP);
	//
	public static String serverReadyPropPrefix = System.getProperty(SERVER_READY_PROP_PREFIX);

	private static final Logger log = LoggerFactory.getLogger(BasilTestServer.class);

	private static JarExecutor j = null;
	public static void start() throws Exception {
		log.debug("Configuration: {}", serverConfiguration);
		log.debug("Init a test db: {}", dbInit);
		if ("true".equals(dbInit)) {
			serverConfiguration = createTestDb();
		}
		startServer();
	}

	public static void waitForServerReady(CloseableHttpClient httpClient) throws Exception{
		log.debug("> before {}#waitForServerReady()", BasilTestServer.class.getSimpleName());

		// Timeout for readiness test
		final String sec = BasilTestServer.getServerReadyTimeout();
		final int timeoutSec = sec == null ? 60 : Integer.valueOf(sec);
		log.debug("Will wait up to " + timeoutSec + " seconds for server to become ready");
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
		boolean serverReady = false;
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
							log.debug("No entity returned for {} - will retry", url);
							continue readyLoop;
						}
						final String content = EntityUtils.toString(entity);
						if (!content.contains(substring)) {
							log.debug("Returned content for {}  does not contain " + "{} - will retry", url, substring);
							continue readyLoop;
						}
					}
				} catch (HttpHostConnectException e) {
					log.trace("Got HttpHostConnectException at " + url + " - will retry");
					continue readyLoop;
				} finally {
					EntityUtils.consumeQuietly(entity);
					if (response != null) {
						response.close();
					}
				}
			}
			serverReady = true;
			log.debug("Got expected content for all configured requests, server is ready");
			log.info("Server ready");
		}

		if (!serverReady) {
			throw new Exception("Server not ready after " + timeoutSec + " seconds");
		}
	}

	public static void destroy() throws Exception {

		if (!dbInit.equals("true")) {
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

	public static String getBasilUser() {
		return basilUser;
	}

	public static String getBasilPassword() {
		return basilPassword;
	}

	public static String getServerReadyTimeout() {
		return serverReadyTimeout;
	}

	public static String getServerBaseUrl() {
		return serverBaseUrl;
	}

//	public static CloseableHttpResponse authenticate(String username, String password, HttpClientContext httpClientContext) throws Exception {
//		log.trace("Authenticating {}", username);
//		HttpPost post = new HttpPost(BasilTestServer.getServerBaseUrl() + "/basil/auth/login");
//		post.addHeader("Content-type", "application/json");
//		BasicHttpEntity e = new BasicHttpEntity();
//		e.setContent(IOUtils.toInputStream("{username: \"" + username + "\", password: \""
//				+ password + "\"}"));
//		post.setEntity(e);
//		return HttpClients.createDefault().execute(post, httpClientContext);
//	}

	private static String createTestDb() throws Exception {
		log.info("Creating test db (once only)");
		final String inputConfigurationFile = System.getProperty(BASIL_CONFIGURATION_FILE_PROP);
		String str = "abcdefghijklmnopqrstuvwxyz1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		final String dbPostfix = '_' + RandomStringUtils.random(8, str.toCharArray());
		File f = new File(inputConfigurationFile);
		if (!f.exists() || !f.canRead()) {
			throw new Exception("Cannot use basil configuration file: " + f);
		}
		Ini ini = Ini.fromResourcePath(f.getAbsolutePath());
		StringBuilder sb = new StringBuilder().append("jdbc:mysql://").append(ini.get("").get("ds.serverName"))
				.append(":").append(ini.get("").get("ds.port")).append("/");

		sb.append("?");

		if(ini.get("").get("ds.verifyServerCertificate") != null){
			sb.append("&verifyServerCertificate=");
			sb.append(ini.get("").get("ds.verifyServerCertificate"));
		}
		if(ini.get("").get("ds.useSSL") != null){
			sb.append("&useSSL=");
			sb.append(ini.get("").get("ds.useSSL"));
		}
		jdbcConnectionUrl = sb.toString();

		databaseName = ini.get("").get("ds.databaseName") + dbPostfix;
		user = ini.get("").get("ds.user");
		password = ini.get("").get("ds.password");

		log.info("XXX Attemp to init a test db {} - {}", databaseName, jdbcConnectionUrl);

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
			Resource r = new FileSystemResource(System.getProperty(TEST_DB_INIT_SCRIPT_PROP));
			log.info("Running init script: {}", r);
			ScriptUtils.executeSqlScript(conn, r);
			// Create a user
			log.info("Create a test user: {}", basilUser);
			try (Statement usr = conn.createStatement()) {
				usr.executeUpdate(
						"INSERT INTO users (username, email, password) values ('" + basilUser + "','" + basilUser
								+ "@email.com','" + new DefaultPasswordService().encryptPassword(basilPassword) + "')");
				usr.executeUpdate(
						"insert into users_roles (username, role_name) values ('" + basilUser + "', 'default')");
			}
		}
		String newConfigurationFile = inputConfigurationFile + dbPostfix;
		log.info("Write configuration file for test server instance: {}", newConfigurationFile);
		Path from = Paths.get(inputConfigurationFile);
		Path to = Paths.get(newConfigurationFile);
		Charset charset = StandardCharsets.UTF_8;
		String content = new String(Files.readAllBytes(from), charset);
		content = content.replaceAll("ds.databaseName = " + ini.get("").get("ds.databaseName"),"ds.databaseName = " + databaseName);
		Files.write(to, content.getBytes(charset));

		// Avoid to create again!
		dbInit = "false";

		// Configuration file
		return newConfigurationFile;
	}

	public static synchronized void startServer() throws Exception {
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

			Properties properties = new Properties(System.getProperties());
			// Add jvm option for basil configuration file
			String opts = properties.getProperty("jar.executor.vm.options");
			opts += " -Dbasil.configurationFile=" + serverConfiguration;
			properties.setProperty("jar.executor.vm.options", opts);
			// Set command line args
			String port = properties.getProperty("jar.executor.server.port");
			properties.setProperty("jar.executor.jar.args", "-p " + port);
			j = new JarExecutor(properties);
			j.start();
			serverBaseUrl = "http://localhost:" + port;
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
}
