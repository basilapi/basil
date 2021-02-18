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
