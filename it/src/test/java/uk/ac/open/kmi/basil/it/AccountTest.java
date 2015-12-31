package uk.ac.open.kmi.basil.it;

import org.junit.Rule;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccountTest {
	
	private static final Logger log = LoggerFactory.getLogger(AccountTest.class);

	@Rule
	public TestName name = new TestName();

	public void createAccount(){
		
	}
	
	public void deleteAccount(){
		// Not implemented yet!
	}
}
