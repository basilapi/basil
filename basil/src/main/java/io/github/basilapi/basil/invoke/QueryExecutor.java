package io.github.basilapi.basil.invoke;

import io.github.basilapi.basil.core.InvocationResult;
import io.github.basilapi.basil.core.exceptions.ApiInvocationException;
import org.apache.jena.atlas.web.auth.HttpAuthenticator;
import org.apache.jena.query.Query;
import org.apache.jena.update.UpdateRequest;

public interface QueryExecutor {
	public InvocationResult execute(Query query, String endpoint, HttpAuthenticator authenticator) throws ApiInvocationException;
	public InvocationResult execute(UpdateRequest query, String endpoint, HttpAuthenticator authenticator)
			throws ApiInvocationException;

}
