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

package io.github.basilapi.basil.store.file;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.basilapi.basil.sparql.QueryParameter;
import io.github.basilapi.basil.sparql.QueryParameter.Type;
import io.github.basilapi.basil.sparql.Specification;
import io.github.basilapi.basil.sparql.SpecificationFactory;
import io.github.basilapi.basil.sparql.UnknownQueryTypeException;

public class FileStoreTest {

	private static Logger log = LoggerFactory.getLogger(FileStoreTest.class);
	private static File home;
	@Rule
	public TestName testName = new TestName();
	private FileStore store;

	@BeforeClass
	public static void beforeClass() throws URISyntaxException {
		URL u = FileStoreTest.class.getClassLoader().getResource(".");
		home = new File(new File(u.toURI()), "FileStoreTest");
		if (!home.exists()) {
			home.mkdirs();
		}
	}

	@Before
	public void before() throws IOException {
		log.info("{}", testName.getMethodName());
		FileUtils.deleteDirectory(home);
		home.mkdirs();
		store = new FileStore(home);

	}

	@Test
	public void serializeQueryParameter() throws IOException,
			ClassNotFoundException {
		QueryParameter param = QueryParameter.buildQueryParameter("param1",
				Type.LangedLiteral, "en", null);
		store.write(testName.getMethodName(), param, "qp");
		QueryParameter param2 = (QueryParameter) store.read(
				testName.getMethodName(), "qp");
		Assert.assertTrue(param2.getName().equals(param.getName()));
	}

	@Test
	public void serializeSpecification() throws IOException,
			ClassNotFoundException {
		Specification spec;
		try {
			spec = SpecificationFactory.create(
					"http://data.open.ac.uk/sparql",
					"SELECT * WHERE {?X a ?_type_iri}");
		} catch (UnknownQueryTypeException e) {
			throw new IOException(e);
		}
		store.write(testName.getMethodName(), spec, "spec");
		Specification spec2 = (Specification) store.read(
				testName.getMethodName(), "spec");
		Assert.assertTrue(spec.getEndpoint().equals(spec2.getEndpoint()));
		Assert.assertTrue(spec.getQuery().equals(spec2.getQuery()));
		Assert.assertTrue(spec.getParameters().iterator().next()
				.equals(spec2.getParameters().iterator().next()));
	}

	@Test
	public void saveAndLoadSpec() throws IOException {
		Specification spec;
		try {
			spec = SpecificationFactory.create(
					"http://data.open.ac.uk/sparql",
					"SELECT * WHERE {?X a ?_type_iri}");
		} catch (UnknownQueryTypeException e) {
			throw new IOException(e);
		}
		store.saveSpec("myspecid", spec);
		Specification spec2 = store.loadSpec("myspecid");
		Assert.assertTrue(spec.getEndpoint().equals(spec2.getEndpoint()));
		Assert.assertTrue(spec.getQuery().equals(spec2.getQuery()));
		Assert.assertTrue(spec.getParameters().iterator().next()
				.equals(spec2.getParameters().iterator().next()));
	}

	@Test
	public void saveAndListSpec() throws IOException {
		Specification spec;
		try {
			spec = SpecificationFactory.create(
					"http://data.open.ac.uk/sparql",
					"SELECT * WHERE {?X a ?_type_iri}");
		} catch (UnknownQueryTypeException e) {
			throw new IOException(e);
		}
		store.saveSpec("myspecid", spec);
		store.saveSpec("myspecid2", spec);
		store.saveSpec("myspecid3", spec);
		store.saveSpec("myspecid4", spec);
		List<String> stones = store.listSpecs();
		for (String id : stones) {
			log.debug(" - {}", id);
		}
		Assert.assertTrue(stones.size() == 4);
		stones.contains("myspecid");
		stones.contains("myspecid2");
		stones.contains("myspecid3");
		stones.contains("myspecid4");
	}


	@Test
	public void saveAndLoadAlias() throws IOException {
		Specification spec;
		try {
			spec = SpecificationFactory.create(
					"http://data.open.ac.uk/sparql",
					"SELECT * WHERE {?X a ?_type_iri}");
		} catch (UnknownQueryTypeException e) {
			throw new IOException(e);
		}
		store.saveSpec("myspecid", spec);
		store.saveSpec("myspecid1", spec);
		store.saveSpec("myspecid2", spec);
		
		store.saveAlias("myspecid", new HashSet<String>(Arrays.asList(new String[] {"alias", "another", "again"})));
		store.saveAlias("myspecid1", new HashSet<String>(Arrays.asList(new String[] {"alias1", "another1", "again1"})));
		store.saveAlias("myspecid2", new HashSet<String>(Arrays.asList(new String[] {"alias2", "another2", "again2"})));

		Set<String> alias = store.loadAlias("myspecid");
		Set<String> alias0 = store.loadAlias("myspecid");
		Set<String> alias1 = store.loadAlias("myspecid1");
		Set<String> alias2 = store.loadAlias("myspecid2");
		
		Assert.assertTrue(alias.size() == 3);
		Assert.assertTrue(alias1.size() == 3);
		Assert.assertTrue(alias1.size() == 3);
		
		Assert.assertTrue(alias.equals(alias0));
		Assert.assertTrue(!alias1.equals(alias0));
		Assert.assertTrue(!alias2.equals(alias0));
		
		Assert.assertTrue(alias.contains("alias"));
		Assert.assertTrue(alias1.contains("alias1"));
		Assert.assertTrue(alias2.contains("alias2"));

		Assert.assertTrue(!alias.contains("alias1"));
		Assert.assertTrue(!alias1.contains("alias2"));
		Assert.assertTrue(!alias2.contains("alias"));
	}
}
