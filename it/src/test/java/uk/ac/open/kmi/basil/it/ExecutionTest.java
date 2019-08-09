package uk.ac.open.kmi.basil.it;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.stanbol.commons.testing.http.Request;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ExecutionTest extends AuthenticatedTestBase {

	private static final String FUSEKI_AUTH_ENTITY = "admin\npwdxxxxx";

	private static final Logger log = LoggerFactory.getLogger(CollectionTest.class);

	private static String insertId = null;

	private static String selectId = null;

	@Rule
	public TestName name = new TestName();

	public void waitForServerReady() throws Exception {
		log.debug("> before {}#waitForServerReady()", getClass().getSimpleName());

		if (serverReady) {
			log.debug(" ... server already marked as ready!");
			return;
		} else {
			BasilTestServer.waitForServerReady(httpClient);
		}

		FusekiTestServer.waitForServerReady(httpClient);
	}

	private String getFusekiUpdateURL() {
		return FusekiTestServer.getServerBaseUrl() + "/fuseki/update";
	}

	private String getFusekiQueryURL() {
		return FusekiTestServer.getServerBaseUrl() + "/fuseki/sparql";
	}

	@Test
	public void EXEC01_CreateInsertAPI() throws Exception {
		log.info("#{}", name.getMethodName());
		String body = loadQueryString("insert_1");
		HttpPut put = new HttpPut(BasilTestServer.getServerBaseUrl() + "/basil");
		put.addHeader("X-Basil-Endpoint", getFusekiUpdateURL());
		BasicHttpEntity entity = new BasicHttpEntity();
		entity.setContent(IOUtils.toInputStream(body));
		put.setEntity(entity);
		HttpResponse response = executor.execute(builder.buildOtherRequest(put)).assertStatus(201).getResponse();
		log.debug(" > Response headers:");
		for (Header h : response.getAllHeaders()) {
			log.debug(" >> {}: {}", h.getName(), h.getValue());
		}
		String l = response.getFirstHeader("Location").getValue();
		insertId = l.substring(l.lastIndexOf('/') + 1);
		log.info(" > Api {} created", insertId);
	}

	@Test
	public void EXEC02_PutAuth() throws Exception {
		log.info("#{}", name.getMethodName());
		HttpPut put = new HttpPut(BasilTestServer.getServerBaseUrl() + "/basil/" + insertId + "/auth");
		BasicHttpEntity entity = new BasicHttpEntity();
		entity.setContent(IOUtils.toInputStream(FUSEKI_AUTH_ENTITY));
		put.setEntity(entity);
		executor.execute(builder.buildOtherRequest(put).withRedirects(false)).assertStatus(201);
	}

	@Test
	public void EXEC03_RunInsertAPI() throws Exception {
		log.info("#{}", name.getMethodName());
		log.trace("Running insert API {}", insertId);
		log.info(" > {}",
				executor.execute(builder
						.buildGetRequest(new URIBuilder("/basil/" + insertId + "/api")
								.addParameter("title", "Moby Dick").addParameter("author", "H. Melville").toString())
						.withRedirects(true)).assertStatus(200).assertContentType("text/plain"));
	}

	private HashMap<String, String> books() {
		String line = "";
		String cvsSplitBy = "\t";
		HashMap<String, String> list = new HashMap<>();
		try (BufferedReader br = new BufferedReader(
				new InputStreamReader(getClass().getClassLoader().getResourceAsStream("books.tsv")))) {

			while ((line = br.readLine()) != null) {
				log.info("{}", line);
				// use comma as separator
				String[] data = line.split(cvsSplitBy);
				String title = data[0];
				String author = "";
				if (data.length > 1) {
					author = data[1];
				}
				// System.out.println(country[0] +" " + country[1]);
				list.put(title, author);
			}

		} catch (IOException e) {
			log.error("", e);
		}
		return list;
	}

	@Test
	public void EXEC04_RunInsertManyAPI() throws Exception {
		log.info("#{}", name.getMethodName());
		log.trace("Running insert API {}", insertId);
		Map<String, String> m = books();
		for (Entry<String, String> en : m.entrySet()) {
			Request req = builder.buildGetRequest(new URIBuilder("/basil/" + insertId + "/api")
					.addParameter("title", en.getKey()).addParameter("author", en.getValue()).toString())
					.withRedirects(true);
			executor.execute(req);
			int s = executor.getResponse().getStatusLine().getStatusCode();
			log.info(">> {} [{}]", req.getRequest().getURI(), s);

			executor.assertStatus(200).assertContentType("text/plain");
		}
	}
	
	@Test
	public void EXEC05_CreateSelectAPI() throws Exception {
		log.info("#{}", name.getMethodName());
		String body = loadQueryString("select_2");
		HttpPut put = new HttpPut(BasilTestServer.getServerBaseUrl() + "/basil");
		put.addHeader("X-Basil-Endpoint", getFusekiQueryURL());
		BasicHttpEntity entity = new BasicHttpEntity();
		entity.setContent(IOUtils.toInputStream(body));
		put.setEntity(entity);
		HttpResponse response = executor.execute(builder.buildOtherRequest(put)).assertStatus(201).getResponse();
		log.debug(" > Response headers:");
		for (Header h : response.getAllHeaders()) {
			log.debug(" >> {}: {}", h.getName(), h.getValue());
		}
		String l = response.getFirstHeader("Location").getValue();
		selectId = l.substring(l.lastIndexOf('/') + 1);
		log.info(" > Api {} created", selectId);
	}
	
	@Test
	public void EXEC06_PutAuth() throws Exception {
		log.info("#{}", name.getMethodName());
		HttpPut put = new HttpPut(BasilTestServer.getServerBaseUrl() + "/basil/" + selectId + "/auth");
		BasicHttpEntity entity = new BasicHttpEntity();
		entity.setContent(IOUtils.toInputStream(FUSEKI_AUTH_ENTITY));
		put.setEntity(entity);
		executor.execute(builder.buildOtherRequest(put).withRedirects(false)).assertStatus(201);
	}
	
	@Test
	public void EXEC07_RunSelectAPI() throws Exception {
		log.info("#{}", name.getMethodName());
		log.trace("Running insert API {}", insertId);
		log.info(" > {}",
				executor.execute(builder
						.buildGetRequest(new URIBuilder("/basil/" + selectId + "/api").toString())
						.withRedirects(true)).assertStatus(200).assertContentType("text/plain"));
	}
	
	@Test
	public void EXEC08_RunSelectAPIJSON() throws Exception {
		log.info("#{}", name.getMethodName());
		log.trace("Running insert API {}", insertId);
		log.info(" > {}",
				executor.execute(builder
						.buildGetRequest(new URIBuilder("/basil/" + selectId + "/api.json").toString())
						.withRedirects(true)).assertStatus(200).assertContentType("application/json").getContent());
	}
}
