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

package io.github.basilapi.basil.invoke;

import io.github.basilapi.basil.core.InvocationResult;
import io.github.basilapi.basil.core.exceptions.ApiInvocationException;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.sparql.exec.http.QueryExecutionHTTPBuilder;
import org.apache.jena.sparql.exec.http.UpdateExecutionHTTP;
import org.apache.jena.sparql.exec.http.UpdateExecutionHTTPBuilder;
import org.apache.jena.sparql.exec.http.UpdateSendMode;
import org.apache.jena.update.UpdateRequest;
import java.net.http.HttpClient;

/**
 * 
 * @author enridaga
 *
 */
public class DirectExecutor implements QueryExecutor {

	@Override
	public InvocationResult execute(Query q, String endpoint, HttpClient authenticator) throws ApiInvocationException {
		QueryExecution qe = QueryExecutionHTTPBuilder.create().service(endpoint).query(q).httpClient(authenticator).build();
		if (q.isSelectType()) {
			return new InvocationResult(qe.execSelect(), q);
		} else if (q.isConstructType()) {
			return new InvocationResult(qe.execConstruct(), q);
		} else if (q.isAskType()) {
			return new InvocationResult(qe.execAsk(), q);
		} else if (q.isDescribeType()) {
			return new InvocationResult(qe.execDescribe(), q);
		} else {
			throw new ApiInvocationException("Unsupported query type: " + q.toString());
		}
	}

	@Override
	public InvocationResult execute(UpdateRequest update, String endpoint, HttpClient authenticator) throws ApiInvocationException {
		UpdateHandler handler = new UpdateHandler();
		UpdateExecutionHTTP exec = UpdateExecutionHTTPBuilder.create().
				update(update).endpoint(endpoint).
				sendMode(UpdateSendMode.asPost).
				httpClient(authenticator).build();
		exec.execute();
		// TODO How to handle the response to check if it went OK?
		return new InvocationResult(handler.getResponse(), update);
	}
}
