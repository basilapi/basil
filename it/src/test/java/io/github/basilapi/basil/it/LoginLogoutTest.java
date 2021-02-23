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

import org.apache.http.HttpResponse;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LoginLogoutTest extends AuthenticatedTestBase {
	private static final Logger log = LoggerFactory.getLogger(CollectionTest.class);

	@Rule
	public TestName name = new TestName();

	@Test
	public void L1_isLogged() throws Exception {
		log.info("#{}", name.getMethodName());
		executor.execute(builder.buildGetRequest("/basil/auth/me")).assertStatus(200);
	}

	@Test
	public void L2_Logout() throws Exception {
		log.info("#{}", name.getMethodName());
		executor.execute(builder.buildGetRequest("/basil/auth/logout")).assertStatus(200);
		executor.execute(builder.buildGetRequest("/basil/auth/me")).assertStatus(403);
	}

	@Test
	public void L3_LoginFailed() throws Exception {
		log.info("#{}", name.getMethodName());
		HttpResponse response = authenticator.authenticate(BasilTestServer.getServerBaseUrl(), "xxxxxxxxx", BasilTestServer.getBasilPassword(), getClientContext());
		Assert.assertTrue(response.getStatusLine().getStatusCode() == 403);
	}

	@Test
	public void L4_LoginFailed() throws Exception {
		log.info("#{}", name.getMethodName());
		HttpResponse response = authenticator.authenticate(BasilTestServer.getServerBaseUrl(), BasilTestServer.getBasilUser(), "yyyyyyyy", getClientContext());
		log.info("authenticated: {}", response.getStatusLine());
		Assert.assertTrue(response.getStatusLine().getStatusCode() == 403);
	}

	@Test
	public void L5_LoginFailed() throws Exception {
		log.info("#{}", name.getMethodName());
		HttpResponse response = authenticator.authenticate(BasilTestServer.getServerBaseUrl(), "xxxxxxxxxxxx", "yyyyyyyy", getClientContext());
		Assert.assertTrue(response.getStatusLine().getStatusCode() == 403);
	}
}
