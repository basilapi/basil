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

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import junit.framework.TestCase;

@RunWith(Suite.class)
@Suite.SuiteClasses({ CollectionTest.class, LoginLogoutTest.class, CRUDTest.class, ForbiddenTest.class, ExecutionTest.class })
public class BasilTestSuite extends TestCase {

	@BeforeClass
	public static void oneTimeSetup() throws Exception {
		BasilTestServer.start();
		FusekiTestServer.start();
	}

	@AfterClass
	public static void oneTimeTearDown() throws Exception {
		BasilTestServer.destroy();
	}
}
