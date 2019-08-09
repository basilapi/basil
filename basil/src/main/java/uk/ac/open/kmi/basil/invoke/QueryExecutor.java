package uk.ac.open.kmi.basil.invoke;

import org.apache.jena.atlas.web.auth.HttpAuthenticator;
import org.apache.jena.query.Query;
import org.apache.jena.update.UpdateRequest;

import uk.ac.open.kmi.basil.core.InvocationResult;
import uk.ac.open.kmi.basil.core.exceptions.ApiInvocationException;

public interface QueryExecutor {
	public InvocationResult execute(Query query, String endpoint, HttpAuthenticator authenticator) throws ApiInvocationException;
	public InvocationResult execute(UpdateRequest query, String endpoint, HttpAuthenticator authenticator)
			throws ApiInvocationException;

}
