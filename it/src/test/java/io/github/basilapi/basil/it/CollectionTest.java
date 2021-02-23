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

import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CollectionTest extends BasilTestBase {
	private static final Logger log = LoggerFactory.getLogger(CollectionTest.class);

	@Rule
	public TestName name = new TestName();

	@Test
	public void askAnyGetJson() throws ParseException, ClientProtocolException, IOException {
		log.info("#{}", name.getMethodName());
		executor.execute(builder.buildGetRequest("/basil").withHeader("Accept", "*/*"));
		log.debug(" ... returned content: {}", executor.getContent());
		executor.assertStatus(200).assertContentType("application/json").assertContentRegexp("\\[.*\\]");
	}

	@Test
	public void askJsonGetJson() throws ParseException, ClientProtocolException, IOException {
		log.info("#{}", name.getMethodName());
		executor.execute(builder.buildGetRequest("/basil").withHeader("Accept", "application/json"));
		log.debug(" ... returned content: {}", executor.getContent());
		executor.assertStatus(200).assertContentType("application/json").assertContentRegexp("\\[.*\\]");
	}

	@Test
	public void askHtmlGet406() throws ParseException, ClientProtocolException, IOException {
		log.info("#{}", name.getMethodName());
		executor.execute(builder.buildGetRequest("/basil").withHeader("Accept", "application/any"));
		log.debug(" ... returned content: {}", executor.getContent());
		executor.assertStatus(406);
	}
}
