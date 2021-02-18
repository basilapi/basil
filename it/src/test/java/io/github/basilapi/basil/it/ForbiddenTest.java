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
