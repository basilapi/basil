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

import org.apache.http.client.methods.HttpPut;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ForbiddenTest extends BasilTestBase{
	private static final Logger log = LoggerFactory.getLogger(ForbiddenTest.class);

	@Rule
	public TestName name = new TestName();

	@Test
	public void Spec1_Create_Forbidden() throws Exception{
		log.info("#{}", name.getMethodName());
		HttpPut put = buildPutSpec("select_1");
		String content = executor.execute(builder.buildOtherRequest(put)).assertStatus(403).getContent();
		// Content must be a json message
		assertIsJson(content);
	}
}
