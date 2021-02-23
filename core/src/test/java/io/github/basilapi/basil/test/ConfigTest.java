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

package io.github.basilapi.basil.test;

import io.github.basilapi.basil.TestUtils;
import org.apache.shiro.config.Ini;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigTest {
	private static Logger log = LoggerFactory.getLogger(ConfigTest.class);

	@Rule
	public TestName method = new TestName();

	@Before
	public void before() {

	}

	@Test
	public void test() {
		String is = TestUtils.class.getClassLoader().getResource(
				"./config/shiro.ini").getPath();
		Ini ini =  Ini.fromResourcePath(is);
		log.info("server: {}", ini.get("").get("ds.serverName"));
		log.info("port: {}", ini.get("").get("ds.port"));
		Assert.assertTrue(ini.get("").get("ds.serverName").equals("localhost"));
		Assert.assertTrue(ini.get("").get("ds.port").equals("8889"));
		
	}
}
