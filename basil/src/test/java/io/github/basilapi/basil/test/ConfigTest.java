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
