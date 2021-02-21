package io.github.basilapi.basil.invoke;

import io.github.basilapi.basil.core.InvocationResult;
import io.github.basilapi.basil.core.exceptions.ApiInvocationException;
import org.apache.jena.atlas.web.auth.HttpAuthenticator;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.riot.WebContent;
import org.apache.jena.riot.web.HttpOp;
import org.apache.jena.update.UpdateRequest;

/**
 * 
 * @author enridaga
 *
 */
public class DirectExecutor implements QueryExecutor {

	@Override
	public InvocationResult execute(Query q, String endpoint, HttpAuthenticator authenticator) throws ApiInvocationException {
		QueryExecution qe = QueryExecutionFactory.sparqlService(endpoint, q, authenticator);
		if (q.isSelectType()) {
			return new InvocationResult(qe.execSelect(), q);
		} else if (q.isConstructType()) {
			return new InvocationResult(qe.execConstruct(), q);
		} else if (q.isAskType()) {
			return new InvocationResult(qe.execAsk(), q);
		} else if (q.isDescribeType()) {
			return new InvocationResult(qe.execDescribe(), q);
		} else {
			throw new ApiInvocationException("Unsupported query type: " + q.getQueryType());
		}
	}

	@Override
	public InvocationResult execute(UpdateRequest update, String endpoint, HttpAuthenticator authenticator) throws ApiInvocationException {
		UpdateHandler handler = new UpdateHandler();
		HttpOp.execHttpPost(endpoint, WebContent.contentTypeSPARQLUpdate, update.toString(), "",  handler, null, null, authenticator) ;
		return new InvocationResult(handler.getResponse(), update);
	}
}