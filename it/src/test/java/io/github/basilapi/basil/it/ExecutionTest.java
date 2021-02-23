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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.stanbol.commons.testing.http.Request;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonParser;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ExecutionTest extends AuthenticatedTestBase {

	private static final String FUSEKI_AUTH_ENTITY = "admin\npwdxxxxx";

	private static final Logger log = LoggerFactory.getLogger(CollectionTest.class);

	private static String insert_1_Id = null;
	private static String insert_2_Id = null;
	private static String insert_3_Id = null;

	private static String delete_1_Id = null;
	private static String delete_2_Id = null;

	private static String select_2_Id = null;
	private static String select_3_Id = null;
	private static String select_4_Id = null;
	private static String select_5_Id = null;
	private static String construct_1_Id = null;

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

	private String _putApi(String queryFile, String endpoint) throws IOException {
		String body = loadQueryString(queryFile);
		HttpPut put = new HttpPut(BasilTestServer.getServerBaseUrl() + "/basil");
		put.addHeader("X-Basil-Endpoint", endpoint);
		BasicHttpEntity entity = new BasicHttpEntity();
		entity.setContent(IOUtils.toInputStream(body));
		put.setEntity(entity);
		HttpResponse response = executor.execute(builder.buildOtherRequest(put)).assertStatus(201).getResponse();
		log.debug(" > Response headers:");
		for (Header h : response.getAllHeaders()) {
			log.debug(" >> {}: {}", h.getName(), h.getValue());
		}
		String l = response.getFirstHeader("Location").getValue();
		String id = l.substring(l.lastIndexOf('/') + 1);
		log.info(" > Api {} created", id);
		return id;
	}

	private void _putAuth(String apiId) throws ClientProtocolException, IOException {
		HttpPut put = new HttpPut(BasilTestServer.getServerBaseUrl() + "/basil/" + apiId + "/auth");
		BasicHttpEntity entity = new BasicHttpEntity();
		entity.setContent(IOUtils.toInputStream(FUSEKI_AUTH_ENTITY));
		put.setEntity(entity);
		executor.execute(builder.buildOtherRequest(put).withRedirects(false)).assertStatus(201);
	}

	@Test
	public void EXEC01_CreateInsertAPI() throws Exception {
		log.info("#{}", name.getMethodName());
		insert_1_Id = _putApi("insert_1", getFusekiUpdateURL());
		log.info(" > Api {} created", insert_1_Id);
	}

	@Test
	public void EXEC02_PutAuth() throws Exception {
		log.info("#{}", name.getMethodName());
		_putAuth(insert_1_Id);
	}

	@Test
	public void EXEC03_RunInsertAPI() throws Exception {
		log.info("#{}", name.getMethodName());
		log.trace("Running insert API {}", insert_1_Id);
		log.info(" > {}",
				executor.execute(builder
						.buildGetRequest(new URIBuilder("/basil/" + insert_1_Id + "/api")
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
		log.trace("Running insert API {}", insert_1_Id);
		Map<String, String> m = books();
		for (Entry<String, String> en : m.entrySet()) {
			Request req = builder.buildGetRequest(new URIBuilder("/basil/" + insert_1_Id + "/api")
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
		select_2_Id = _putApi("select_2", getFusekiQueryURL());
		log.info(" > Api {} created", select_2_Id);
	}

	@Test
	public void EXEC06_PutAuth() throws Exception {
		log.info("#{}", name.getMethodName());
		_putAuth(select_2_Id);
	}

	@Test
	public void EXEC07_RunSelectAPI() throws Exception {
		log.info("#{}", name.getMethodName());
		log.trace("Running API {}", select_2_Id);
		log.info(" > {}",
				executor.execute(builder.buildGetRequest(new URIBuilder("/basil/" + select_2_Id + "/api").toString())
						.withRedirects(true)).assertStatus(200).assertContentType("text/plain"));
	}

	@Test
	public void EXEC08_RunSelectAPIJSON() throws Exception {
		log.info("#{}", name.getMethodName());
		log.trace("Running API {}", select_2_Id);
		log.info(" > {}", executor.execute(builder
				.buildGetRequest(new URIBuilder("/basil/" + select_2_Id + "/api.json").toString()).withRedirects(true))
				.assertStatus(200).assertContentType("application/json").getContent());
	}

	@Test
	public void EXEC09_CreateDeleteAll() throws Exception {
		log.info("#{}", name.getMethodName());
		delete_1_Id = _putApi("delete_1", getFusekiUpdateURL());
		_putAuth(delete_1_Id);
	}

	private void _performDeleteAll() throws ClientProtocolException, IOException, URISyntaxException {
		log.info(" > (delete) {}",
				executor.execute(builder.buildGetRequest(new URIBuilder("/basil/" + delete_1_Id + "/api").toString())
						.withRedirects(true)).assertStatus(200).assertContentType("text/plain"));

		// If this worked, the returning json in the following should be have an empty
		// "items" array
		String json = executor.execute(builder
				.buildGetRequest(new URIBuilder("/basil/" + select_2_Id + "/api.json").toString()).withRedirects(true))
				.assertStatus(200).assertContentType("application/json").getContent();
		log.info(" > (read: expected empty) {}", json);
		JsonParser p = new JsonParser();
		Assert.assertTrue(p.parse(json).getAsJsonObject().get("items").getAsJsonArray().size() == 0);
	}

	@Test
	public void EXEC10_ExecDeleteAll() throws Exception {
		log.info("#{}", name.getMethodName());
		_performDeleteAll();
	}

	@Test
	public void EXEC11_CreateInsertAPI() throws Exception {
		log.info("#{}", name.getMethodName());
		insert_2_Id = _putApi("insert_2", getFusekiUpdateURL());
		_putAuth(insert_2_Id);
		log.info(" > Api {} created", insert_2_Id);
	}

	@Test
	public void EXEC12_ExecInsertManyAPI() throws Exception {
		log.info("#{}", name.getMethodName());
		log.trace("Running insert API {}", insert_2_Id);

		Map<String, String> m = books();
		for (Entry<String, String> en : m.entrySet()) {
			Request req = builder.buildGetRequest(new URIBuilder("/basil/" + insert_2_Id + "/api")
					.addParameter("title", en.getKey()).addParameter("author", en.getValue()).toString())
					.withRedirects(true);
			executor.execute(req);
			int s = executor.getResponse().getStatusLine().getStatusCode();
			log.info(">> {} [{}]", req.getRequest().getURI(), s);

			executor.assertStatus(200).assertContentType("text/plain");
		}

		// after this, the dataset should contain 100 items
		log.trace("Running select API {}", select_2_Id);
		String json = executor.execute(builder
				.buildGetRequest(new URIBuilder("/basil/" + select_2_Id + "/api.json").toString()).withRedirects(true))
				.assertStatus(200).assertContentType("application/json").getContent();
		log.info(" > (read: expected 97 items) {}", json);

		JsonParser p = new JsonParser();
		int size = p.parse(json).getAsJsonObject().get("items").getAsJsonArray().size();
		log.info(" > (read: got {} objects", size);
		Assert.assertTrue(size == 97);
	}

	@Test
	public void EXEC13_CreateAPIs_withQuads() throws Exception {

		log.info("#{}", name.getMethodName());
		insert_3_Id = _putApi("insert_3", getFusekiUpdateURL());
		_putAuth(insert_3_Id);
		log.info(" > Api {} created", insert_3_Id);
		select_3_Id = _putApi("select_3", getFusekiQueryURL());
		_putAuth(select_3_Id);
		log.info(" > Api {} created", select_3_Id);
		construct_1_Id = _putApi("construct_1", getFusekiQueryURL());
		_putAuth(construct_1_Id);
		log.info(" > Api {} created", construct_1_Id);
	}

	@Test
	public void EXEC14_ExecInsertMany_withQuads() throws Exception {
		log.info("#{}", name.getMethodName());
		log.trace("Running insert API {}", insert_3_Id);

		Map<String, String> m = books();
		int c = 0;
		for (Entry<String, String> en : m.entrySet()) {
			c++;
			Request req = builder.buildGetRequest(new URIBuilder("/basil/" + insert_3_Id + "/api")
					// this time the id is a int and it is passed
					.addParameter("id", Integer.toString(c)).addParameter("title", en.getKey())
					.addParameter("author", en.getValue()).toString()).withRedirects(true);
			executor.execute(req);
			int s = executor.getResponse().getStatusLine().getStatusCode();
			log.info(">> {} [{}]", req.getRequest().getURI(), s);

			executor.assertStatus(200).assertContentType("text/plain");
		}

		// after this, the graph should contain 97 items
		log.trace("Running select API {}", select_3_Id);
		String json = executor.execute(builder
				.buildGetRequest(new URIBuilder("/basil/" + select_3_Id + "/api.json").toString()).withRedirects(true))
				.assertStatus(200).assertContentType("application/json").getContent();
		log.info(" > (read: expected 97 items) {}", json);

		JsonParser p = new JsonParser();
		int size = p.parse(json).getAsJsonObject().get("items").getAsJsonArray().size();
		log.info(" > (read: got {} items)", size);
		Assert.assertTrue(size == 97);
	}

	@Test
	public void EXEC15_ExecConstructAPI() throws Exception {
		log.info("#{}", name.getMethodName());
		log.trace("Running construct API {}", construct_1_Id);

		Map<String, String> m = books();
		int c = 0;
		for (Entry<String, String> en : m.entrySet()) {
			c++;
			Request req = builder.buildGetRequest(new URIBuilder("/basil/" + construct_1_Id + "/api")
					// this time the id is a int and it is passed
					.addParameter("id", Integer.toString(c)).toString()).withRedirects(true);
			executor.execute(req);
			int s = executor.getResponse().getStatusLine().getStatusCode();
			log.info(">> {} [{}]", req.getRequest().getURI(), s);

			String content = executor.assertStatus(200).assertContentType("text/plain").getContent();
			log.trace(">> {}", content);
		}
	}

	@Test
	public void EXEC16_CreateDeleteOne() throws Exception {
		log.info("#{}", name.getMethodName());
		delete_2_Id = _putApi("delete_2", getFusekiUpdateURL());
		_putAuth(delete_2_Id);
		log.info(" > Api {} created", delete_2_Id);

	}

	@Test
	public void EXEC17_ExecDeleteOneByOne() throws Exception {
		log.info("#{}", name.getMethodName());
		Map<String, String> m = books();
		int c = 0;
		for (Entry<String, String> en : m.entrySet()) {
			c++;
			Request req = builder.buildGetRequest(new URIBuilder("/basil/" + delete_2_Id + "/api")
					// this time the id is a int and it is passed
					.addParameter("id", Integer.toString(c)).toString()).withRedirects(true);
			executor.execute(req);
			int s = executor.getResponse().getStatusLine().getStatusCode();
			log.info(">> {} [{}]", req.getRequest().getURI(), s);

			String content = executor.assertStatus(200).assertContentType("text/plain").getContent();
			log.trace(">> {}", content);
		}

		// after this, the graph should contain 0 items
		log.trace("Running select API {}", select_3_Id);
		String json = executor.execute(builder
				.buildGetRequest(new URIBuilder("/basil/" + select_3_Id + "/api.json").toString()).withRedirects(true))
				.assertStatus(200).assertContentType("application/json").getContent();
		log.info(" > (read: expected 0 items) {}", json);

		JsonParser p = new JsonParser();
		int size = p.parse(json).getAsJsonObject().get("items").getAsJsonArray().size();
		log.info(" > (read: got {} items)", size);
		Assert.assertTrue(size == 0);
	}

	@Test
	public void EXEC18_ExecDeleteAll() throws Exception {
		log.info("#{}", name.getMethodName());
		// Before closing, we delete everything
		EXEC10_ExecDeleteAll();
	}

	@Test
	public void EXEC19_CreateSelectLimit() throws Exception {
		log.info("#{}", name.getMethodName());
		select_4_Id = _putApi("select_4", getFusekiQueryURL());
		_putAuth(select_4_Id);
		log.info(" > Api {} created", select_4_Id);
	}

	@Test
	public void EXEC20_ExecPrepareSelectLimit() throws Exception {
		log.info("#{}", name.getMethodName());
		// Populate the data again
		log.trace("Running insert API {}", insert_3_Id);

		Map<String, String> m = books();
		int c = 0;
		for (Entry<String, String> en : m.entrySet()) {
			c++;
			Request req = builder.buildGetRequest(new URIBuilder("/basil/" + insert_3_Id + "/api")
					// this time the id is a int and it is passed
					.addParameter("id", Integer.toString(c)).addParameter("title", en.getKey())
					.addParameter("author", en.getValue()).toString()).withRedirects(true);
			executor.execute(req);
			int s = executor.getResponse().getStatusLine().getStatusCode();
			log.info(">> {} [{}]", req.getRequest().getURI(), s);

			executor.assertStatus(200).assertContentType("text/plain");
		}
	}

	@Test
	public void EXEC21_ExecSelectLimit() throws Exception {
		log.info("#{}", name.getMethodName());

		// Test select with limit
		// after this, the query should return 10 items
		log.trace("Running select_4 API {}", select_4_Id);
		String json = executor.execute(builder
				.buildGetRequest(
						new URIBuilder("/basil/" + select_4_Id + "/api.json").addParameter("limit", "10").toString())
				.withRedirects(true)).assertStatus(200).assertContentType("application/json").getContent();
		log.info(" > (read: expected 10 items) {}", json);

		JsonParser p = new JsonParser();
		int size = p.parse(json).getAsJsonObject().get("items").getAsJsonArray().size();
		log.info(" > (read: got {} items)", size);
		Assert.assertTrue(size == 10);
	}
	
	@Test
	public void EXEC22_CreateSelectOffsetLimit() throws Exception {
		log.info("#{}", name.getMethodName());
		select_5_Id = _putApi("select_5", getFusekiQueryURL());
		_putAuth(select_5_Id);
		log.info(" > Api {} created", select_5_Id);
	}
	
	@Test
	public void EXEC23_ExecSelectLimitOffset() throws Exception {
		log.info("#{}", name.getMethodName());

		// Test select with limit
		// after this, the query should return 10 items
		log.trace("Running select_5 API {}", select_5_Id);
		String json = executor.execute(builder
				.buildGetRequest(
						new URIBuilder("/basil/" + select_5_Id + "/api.json").addParameter("limit", "10").addParameter("offset", "11").toString())
				.withRedirects(true)).assertStatus(200).assertContentType("application/json").getContent();
		log.info(" > (read: expected 10 items) {}", json);

		JsonParser p = new JsonParser();
		int size = p.parse(json).getAsJsonObject().get("items").getAsJsonArray().size();
		log.info(" > (read: got {} items)", size);
		Assert.assertTrue(size == 10);
	}

	@Test
	public void EXEC24_ExecDeleteAll() throws Exception {
		log.info("#{}", name.getMethodName());
		// Before closing, we delete everything
		_performDeleteAll();
	}

}
