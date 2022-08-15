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

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
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
	protected static final Authenticator authenticator = new Authenticator();
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
		httpClient = HttpClients.custom().setDefaultRequestConfig(getRequestConfig())
				.setDefaultCookieStore(getClientContext().getCookieStore()).build();
		executor = new RequestExecutor(httpClient);
		//
		waitForServerReady();
	}

	public void waitForServerReady() throws Exception {
		log.debug("> before {}#waitForServerReady()", getClass().getSimpleName());

		if (serverReady) {
			log.debug(" ... server already marked as ready!");
			return;
		}else{
			BasilTestServer.waitForServerReady(httpClient);
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
