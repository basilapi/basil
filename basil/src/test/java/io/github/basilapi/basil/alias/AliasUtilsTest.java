package uk.ac.open.kmi.basil.alias;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AliasUtilsTest {
	private static Logger log = LoggerFactory.getLogger(AliasUtilsTest.class);

	@Rule
	public TestName testName = new TestName();

	private Set<String> makeSet(String... alias) {
		return new HashSet<String>(Arrays.asList(alias));
	}

	@Test
	public void aliasTooShort1() {
		log.info("{}", testName.getMethodName());
		try {
			AliasUtils.test(makeSet("a"));
			Assert.assertTrue(testName.getMethodName(), false);
		} catch (IOException io) {
			Assert.assertTrue(testName.getMethodName(), true);
		}
	}

	@Test
	public void aliasTooShort2() {
		log.info("{}", testName.getMethodName());
		try {
			AliasUtils.test(makeSet("ab"));
			Assert.assertTrue(testName.getMethodName(), false);
		} catch (IOException io) {
			Assert.assertTrue(testName.getMethodName(), true);
		}
	}

	@Test
	public void aliasTooShort4() {
		log.info("{}", testName.getMethodName());
		try {
			AliasUtils.test(makeSet("abcd"));
			Assert.assertTrue(testName.getMethodName(), false);
		} catch (IOException io) {
			Assert.assertTrue(testName.getMethodName(), true);
		}
	}

	@Test
	public void aliasTooLong() {
		log.info("{}", testName.getMethodName());
		try {
			AliasUtils.test(makeSet("aaaaaaaaaaaaaaaaX"));
			Assert.assertTrue(testName.getMethodName(), false);
		} catch (IOException io) {
			Assert.assertTrue(testName.getMethodName(), true);
		}
	}

	@Test
	public void aliasInvalidChar() {
		log.info("{}", testName.getMethodName());
		try {
			AliasUtils.test(makeSet("aaaaaaa$aaaaaaaa"));
			Assert.assertTrue(testName.getMethodName(), false);
		} catch (IOException io) {
			Assert.assertTrue(testName.getMethodName(), true);
		}
	}

	@Test
	public void aliasInvalidChar1() {
		log.info("{}", testName.getMethodName());
		try {
			AliasUtils.test(makeSet("aaaaaaa/aaaaaaaa"));
			Assert.assertTrue(testName.getMethodName(), false);
		} catch (IOException io) {
			Assert.assertTrue(testName.getMethodName(), true);
		}
	}

	@Test
	public void aliasInvalidChar2() {
		log.info("{}", testName.getMethodName());
		try {
			AliasUtils.test(makeSet("aaaaaaa=aaaaaaaa"));
			Assert.assertTrue(testName.getMethodName(), false);
		} catch (IOException io) {
			Assert.assertTrue(testName.getMethodName(), true);
		}
	}

	@Test
	public void aliasInvalidChar3() {
		log.info("{}", testName.getMethodName());
		try {
			AliasUtils.test(makeSet("aaaaaaa#aaaaaaaa"));
			Assert.assertTrue(testName.getMethodName(), false);
		} catch (IOException io) {
			Assert.assertTrue(testName.getMethodName(), true);
		}
	}

	@Test
	public void aliasInvalidChar4() {
		log.info("{}", testName.getMethodName());
		try {
			AliasUtils.test(makeSet("aaaaaaa&aaaaaaaa"));
			Assert.assertTrue(testName.getMethodName(), false);
		} catch (IOException io) {
			Assert.assertTrue(testName.getMethodName(), true);
		}
	}

	@Test
	public void aliasInvalidChars() {
		log.info("{}", testName.getMethodName());
		for (String s : new String[] { " ", "_", "\"", "'", "?", "<", ">" }) {
			try {
				AliasUtils.test(makeSet("aaaaaaa" + s));
				Assert.assertTrue(testName.getMethodName(), false);
			} catch (IOException io) {
				Assert.assertTrue(testName.getMethodName(), true);
			}
		}
	}

	@Test
	public void aliasOK() {
		log.info("{}", testName.getMethodName());
		try {
			AliasUtils.test(makeSet("aaaaaaa-aaaaaaaa"));
			Assert.assertTrue(testName.getMethodName(), true);
		} catch (IOException io) {
			log.error(testName.getMethodName(), io);
			Assert.assertTrue(testName.getMethodName(), false);
		}
	}

	@Test
	public void aliasOK1() {
		log.info("{}", testName.getMethodName());
		try {
			AliasUtils.test(makeSet("my-api-1977"));
			Assert.assertTrue(testName.getMethodName(), true);
		} catch (IOException io) {
			log.error(testName.getMethodName(), io);
			Assert.assertTrue(testName.getMethodName(), false);
		}
	}

	@Test
	public void aliasOK2() {
		log.info("{}", testName.getMethodName());
		try {
			AliasUtils.test(makeSet("1977-my-api-"));
			Assert.assertTrue(testName.getMethodName(), true);
		} catch (IOException io) {
			log.error(testName.getMethodName(), io);
			Assert.assertTrue(testName.getMethodName(), false);
		}
	}

	@Test
	public void aliasOKs() {
		log.info("{}", testName.getMethodName());
		for (String s : new String[] { "my-api-here", "myapi", "-api-", "againApi", "this-123", "667890", "123123-12312" , "123123-12312"}) {
			try {
				AliasUtils.test(makeSet(s));
				Assert.assertTrue(testName.getMethodName(), true);
			} catch (IOException io) {
				log.error(testName.getMethodName(), io);
				Assert.assertTrue(testName.getMethodName(), false);
			}
		}
	}
}
