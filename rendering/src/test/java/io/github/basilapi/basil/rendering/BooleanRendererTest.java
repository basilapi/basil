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

package io.github.basilapi.basil.rendering;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BooleanRendererTest {
	final static Logger log = LoggerFactory.getLogger(BooleanRendererTest.class);
	final static BooleanRenderer r = new BooleanRenderer(true);
	final static Map<String,String> pm = new HashMap<String,String>();
	@Rule
	public TestName test = new TestName();

	@Test
	public void getInput() {
		Assert.assertEquals(new Boolean(true), r.getInput());
	}
	
	@Test
	public void render() {
		List<Exception> failures = new ArrayList<Exception>();
		for(MediaType m: MoreMediaType.MediaTypes){
			log.debug("Testing support {}", m);
			try {
				r.stream(m, "", pm);
			} catch (CannotRenderException e) {
				failures.add(e);
			}
		}
		for(Exception e : failures)log.error("Failed: {}", e.getMessage());
		Assert.assertEquals(failures.size(), 0);

	}
	
}
