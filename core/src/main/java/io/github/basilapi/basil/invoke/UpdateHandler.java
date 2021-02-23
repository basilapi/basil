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

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.jena.riot.web.HttpResponseHandler;

public class UpdateHandler implements HttpResponseHandler {
	HttpResponse response = null;
	String baseIRI = null;

	@Override
	public void handle(String baseIRI, HttpResponse response) throws IOException {
		this.response = response;
		this.baseIRI = baseIRI;
	}

	public HttpResponse getResponse() {
		return response;
	}

	public String getBaseIRI() {
		return baseIRI;
	}
}
