/*
 * Copyright (c) 2021. Enrico Daga and Luca Panziera
 *
 * MLicensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.github.basilapi.basil.core;

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
