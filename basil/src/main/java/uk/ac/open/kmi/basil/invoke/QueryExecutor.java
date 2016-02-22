package uk.ac.open.kmi.basil.invoke;

import com.hp.hpl.jena.query.Query;

import uk.ac.open.kmi.basil.core.InvocationResult;
import uk.ac.open.kmi.basil.core.exceptions.ApiInvocationException;

public interface QueryExecutor {

	public InvocationResult execute(Query query, String endpoint) throws ApiInvocationException;
}
