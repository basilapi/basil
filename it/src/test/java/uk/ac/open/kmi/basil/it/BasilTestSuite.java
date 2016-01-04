package uk.ac.open.kmi.basil.it;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import junit.framework.TestCase;

@RunWith(Suite.class)
@Suite.SuiteClasses({ CollectionTest.class, CRUDTest.class, ForbiddenTest.class })
public class BasilTestSuite extends TestCase {

	@BeforeClass
	public static void oneTimeSetup() throws Exception {
		BasilTestServer.start();
	}

	@AfterClass
	public static void oneTimeTearDown() throws Exception {
		BasilTestServer.destroy();
	}
}