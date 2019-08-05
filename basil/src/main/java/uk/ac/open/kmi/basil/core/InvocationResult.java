package uk.ac.open.kmi.basil.core;

import org.apache.jena.query.Query;
import org.apache.jena.update.UpdateRequest;

/**
 * Created by Luca Panziera on 19/06/15.
 * 
 * Supports update by enridaga
 * 
 */
public class InvocationResult {
	private Object result;
	private Query query;
	private UpdateRequest update;

	public InvocationResult(Object result, Query query) {
		this.result = result;
		this.query = query;
		this.update = null;
	}

	public InvocationResult(Object result, UpdateRequest update) {
		this.result = result;
		this.query = null;
		this.update = update;
	}

	public Object getResult() {
		return result;
	}

	/**
	 * It is null when Update
	 * 
	 * @return
	 */
	public Query getQuery() {
		return query;
	}

	/**
	 * It is null when Query
	 * 
	 * @return
	 */
	public UpdateRequest getUpdate() {
		return update;
	}
	
	public boolean isQuery() {
		return this.query != null;
	}
	
	public boolean isUpdate() {
		return this.update != null;
	}
}
