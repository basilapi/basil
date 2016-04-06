package uk.ac.open.kmi.basil.core;

import org.apache.jena.query.Query;

/**
 * Created by Luca Panziera on 19/06/15.
 */
public class InvocationResult {
    private Object result;
    private Query query;

    public InvocationResult(Object result, Query query) {
        this.result = result;
        this.query = query;
    }

    public Object getResult() {
        return result;
    }

    public Query getQuery() {
        return query;
    }
}
