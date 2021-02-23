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

package io.github.basilapi.basil.sparql;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueryTypeTest {

	private static Logger log = LoggerFactory.getLogger(QueryTypeTest.class);

	@Rule
	public TestName name = new TestName();

	private String queryType(String q) {

		// Get index of first occurrence of select, insert, ask, construct, and delete
		try {
			return QueryType.guessQueryType(q).toString().toLowerCase();
		} catch (UnknownQueryTypeException e) {
			log.error("", e);
			return "Unknown";
		}
	}

	@Test
	public void QTYPE_SelectIsSelect() throws IOException {
		log.info("#{}", name.getMethodName());
		Assert.assertTrue(queryType(TestUtils.loadQueryString("select_1")).equals("select"));
		Assert.assertTrue(queryType(TestUtils.loadQueryString("select_2")).equals("select"));
		Assert.assertTrue(queryType(TestUtils.loadQueryString("select_3")).equals("select"));
		Assert.assertTrue(queryType(TestUtils.loadQueryString("select_4")).equals("select"));
		Assert.assertTrue(queryType(TestUtils.loadQueryString("select_5")).equals("select"));
		Assert.assertTrue(queryType(TestUtils.loadQueryString("select_6")).equals("select"));
		Assert.assertTrue(queryType(TestUtils.loadQueryString("select_7")).equals("select"));
		Assert.assertTrue(queryType(TestUtils.loadQueryString("select_8")).equals("select"));
	}

	@Test
	public void QTYPE_InsertIsNotSelect() throws IOException {
		log.info("#{}", name.getMethodName());
		// insert is not select
		Assert.assertFalse(queryType(TestUtils.loadQueryString("insert_1")).equals("select"));
		Assert.assertFalse(queryType(TestUtils.loadQueryString("insert_2")).equals("select"));
		Assert.assertFalse(queryType(TestUtils.loadQueryString("insert_3")).equals("select"));
		Assert.assertFalse(queryType(TestUtils.loadQueryString("insert_4")).equals("select"));
	}

	@Test
	public void QTYPE_InsertIsInsert() throws IOException {
		log.info("#{}", name.getMethodName());
		// insert is not select
		Assert.assertTrue(queryType(TestUtils.loadQueryString("insert_1")).equals("insert"));
		Assert.assertTrue(queryType(TestUtils.loadQueryString("insert_2")).equals("insert"));
		Assert.assertTrue(queryType(TestUtils.loadQueryString("insert_3")).equals("insert"));
		Assert.assertTrue(queryType(TestUtils.loadQueryString("insert_4")).equals("insert"));
	}

	@Test
	public void QTYPE_AskIsNotSelect() throws IOException {
		log.info("#{}", name.getMethodName());

		// ask is not select
		Assert.assertFalse(queryType(TestUtils.loadQueryString("ask_1")).equals("select"));
	}

	@Test
	public void QTYPE_AskIsAsk() throws IOException {
		log.info("#{}", name.getMethodName());

		// ask is not select
		Assert.assertTrue(queryType(TestUtils.loadQueryString("ask_1")).equals("ask"));
	}

	@Test
	public void QTYPE_ConstructIsNotSelect() throws IOException {
		log.info("#{}", name.getMethodName());

		// construct is not select
		Assert.assertFalse(queryType(TestUtils.loadQueryString("construct_1")).equals("select"));
	}

	@Test
	public void QTYPE_ConstructIsConstruct() throws IOException {
		log.info("#{}", name.getMethodName());

		// construct is not select
		Assert.assertTrue(queryType(TestUtils.loadQueryString("construct_1")).equals("construct"));
	}

	@Test
	public void QTYPE_DeleteIsNotSelect() throws IOException {
		log.info("#{}", name.getMethodName());

		// delete is not select
		Assert.assertFalse(queryType(TestUtils.loadQueryString("delete_1")).equals("select"));
		Assert.assertFalse(queryType(TestUtils.loadQueryString("delete_2")).equals("select"));
		Assert.assertFalse(queryType(TestUtils.loadQueryString("delete_3")).equals("select"));
	}

	@Test
	public void QTYPE_DeleteIsDelete() throws IOException {
		log.info("#{}", name.getMethodName());

		// delete is not select
		Assert.assertTrue(queryType(TestUtils.loadQueryString("delete_1")).equals("delete"));
		Assert.assertTrue(queryType(TestUtils.loadQueryString("delete_2")).equals("delete"));
		Assert.assertTrue(queryType(TestUtils.loadQueryString("delete_3")).equals("delete"));
	}

	@Test
	public void QTYPE_UpdateIsUpdate() throws UnknownQueryTypeException, IOException {
		log.info("#{}", name.getMethodName());
		
		Assert.assertTrue(QueryType.isUpdate(TestUtils.loadQueryString("insert_1")));
		Assert.assertTrue(QueryType.isUpdate(TestUtils.loadQueryString("insert_2")));
		Assert.assertTrue(QueryType.isUpdate(TestUtils.loadQueryString("insert_3")));
		//
		Assert.assertTrue(QueryType.isUpdate(TestUtils.loadQueryString("delete_1")));
		Assert.assertTrue(QueryType.isUpdate(TestUtils.loadQueryString("delete_2")));
		Assert.assertTrue(QueryType.isUpdate(TestUtils.loadQueryString("delete_3")));
	}
	
	@Test
	public void QTYPE_QueryIsNotUpdate() throws UnknownQueryTypeException, IOException {
		log.info("#{}", name.getMethodName());
		
		Assert.assertFalse(QueryType.isUpdate(TestUtils.loadQueryString("select_1")));
		Assert.assertFalse(QueryType.isUpdate(TestUtils.loadQueryString("select_2")));
		Assert.assertFalse(QueryType.isUpdate(TestUtils.loadQueryString("select_3")));
//		//
		Assert.assertFalse(QueryType.isUpdate(TestUtils.loadQueryString("construct_1")));
		Assert.assertFalse(QueryType.isUpdate(TestUtils.loadQueryString("ask_1")));
	}
	
	@Test
	public void QTYPE_DeleteTokenInComment() throws UnknownQueryTypeException, IOException {
		log.info("#{}", name.getMethodName());
		
		Assert.assertFalse(QueryType.isUpdate(TestUtils.loadQueryString("select_8")));
	}
	

	@Test
	public void QTYPE_InsertTokenAsPrefix() throws UnknownQueryTypeException, IOException {
		log.info("#{}", name.getMethodName());
		Assert.assertFalse(QueryType.isUpdate(TestUtils.loadQueryString("select_9")));
	}
	
	@Test
	public void QTYPE_InsertTokenInNamespace() throws UnknownQueryTypeException, IOException {
		log.info("#{}", name.getMethodName());
		Assert.assertFalse(QueryType.isUpdate(TestUtils.loadQueryString("select_10")));
	}
	
	@Test
	public void QTYPE_InsertTokenAsVariable() throws UnknownQueryTypeException, IOException {
		log.info("#{}", name.getMethodName());
		Assert.assertFalse(QueryType.isUpdate(TestUtils.loadQueryString("select_10")));
	}

	@Test
	public void QTYPE_SelectWithOddSpaces() throws UnknownQueryTypeException, IOException {
		log.info("#{}", name.getMethodName());
		Assert.assertFalse(QueryType.isUpdate(TestUtils.loadQueryString("select_11")));
	}
}
