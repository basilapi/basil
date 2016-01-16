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
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.shiro.authc.credential.DefaultPasswordService;
import org.apache.shiro.config.Ini;
import org.apache.stanbol.commons.testing.jarexec.JarExecutor;
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

	private static final Logger log = LoggerFactory.getLogger(BasilTestBase.class);

	public static void start() throws Exception {
		log.debug("Configuration: {}", serverConfiguration);
		log.debug("Init a test db: {}", dbInit);
		if ("true".equals(dbInit)) {
			serverConfiguration = createTestDb();
		}
		startServer();
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
	
	public static CloseableHttpResponse authenticate(String username, String password, HttpClientContext httpClientContext) throws Exception {
		log.trace("Authenticating {}", username);
		HttpPost post = new HttpPost(BasilTestServer.getServerBaseUrl() + "/basil/auth/login");
		post.addHeader("Content-type", "application/json");
		BasicHttpEntity e = new BasicHttpEntity();
		e.setContent(IOUtils.toInputStream("{username: \"" + username + "\", password: \""
				+ password + "\"}"));
		post.setEntity(e);
		return HttpClients.createDefault().execute(post, httpClientContext);
	}

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
			Resource r = new FileSystemResource(System.getProperty(TEST_DB_INIT_SCRIPT_PROP));
			log.info("Running init script: {}", r);
			ScriptUtils.executeSqlScript(conn, r);
			// Create a user
			log.info("Create a test user: {}", basilUser);
			try (Statement usr = conn.createStatement()) {
				usr.executeUpdate(
						"INSERT INTO USERS (username, email, password) values ('" + basilUser + "','" + basilUser
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
		content = content.replaceAll(ini.get("").get("ds.databaseName"), databaseName);
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

			Properties properties = System.getProperties();
			// Add jvm option for basil configuration file
			String opts = properties.getProperty("jar.executor.vm.options");
			opts += " -Dbasil.configurationFile=" + serverConfiguration;
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
}
