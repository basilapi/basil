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

import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthenticatedTestBase extends BasilTestBase {
	private static final Logger log = LoggerFactory.getLogger(AuthenticatedTestBase.class);
	protected static HttpClientContext context = HttpClientContext.create(); // Set
																				// a
																				// default
	protected static RequestConfig config = RequestConfig.DEFAULT;
	
	@BeforeClass
	public static void beforeClass() throws Exception {
		log.trace("Initializing authenticated test. Overriding HTTP context");
		// Authenticate and remembered session cookie (in context)
		context = HttpClientContext.create();
		config = RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build();
		CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(config).build();
		BasilTestServer.waitForServerReady(httpClient);
		authenticator.authenticate(BasilTestServer.getServerBaseUrl(), BasilTestServer.getBasilUser(),
				BasilTestServer.getBasilPassword(), context);
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
