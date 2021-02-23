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

package io.github.basilapi.basil.rest.core;

import java.util.Collection;
import java.util.Map.Entry;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import io.github.basilapi.basil.core.ApiInfo;
import io.github.basilapi.basil.doc.Doc;
import io.github.basilapi.basil.search.Result;
import io.github.basilapi.basil.search.SimpleQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

@Path("discovery/search")
@Api(value = "/basil", description = "BASIL operations")
public class SearchResource extends AbstractResource {

	private static final Logger log = LoggerFactory.getLogger(SearchResource.class);

	@GET
	@ApiOperation(value = "Search APIs")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 400, message = "Bad request"),
			@ApiResponse(code = 500, message = "Internal error") })
	public Response get(@QueryParam("query") String text) {
		log.trace("Called GET.");
		if (text == null) {
			return Response.status(Status.BAD_REQUEST)
					.entity("Missing mandatory parameter: query")
					.build();
		}
		SimpleQuery q = new SimpleQuery();
		q.setText(text);
		try {
			Collection<Result> results = getSearchProvider().contextSearch(q);
			JsonObject m = new JsonObject();
			m.add("query", new JsonPrimitive(text));
			m.add("count", new JsonPrimitive(results.size()));
			JsonArray r = new JsonArray();
			for (Result i : results) {
				
				String name = getApiManager().getDoc(i.id()).get(Doc.Field.NAME);
				String createdBy = getApiManager().getCreatorOfApi(i.id());
				ApiInfo info = getApiManager().getInfo(i.id());
				JsonObject j = new JsonObject();
				j.add("id", new JsonPrimitive(i.id()));
				j.add("name", new JsonPrimitive(name));
				j.add("modified", new JsonPrimitive(info.modified().getTime()));
				JsonArray a = new JsonArray();
				for(Entry<String,String> e: i.context().entrySet()){
					JsonObject o = new JsonObject();
					o.add(e.getKey(), new JsonPrimitive(e.getValue()));
					a.add(o);
				}
				j.add("createdBy", new JsonPrimitive(createdBy)); // TODO
				//object.add("description", new JsonPrimitive(doc.get(Field.DESCRIPTION)));
				j.add("location", new JsonPrimitive(String.valueOf(requestUri.getBaseUriBuilder().path(i.id()))));
				j.add("context", a);
				r.add(j);
			}
			m.add("results", r);
			ResponseBuilder response = Response.ok().entity(m.toString());
			return response.build();
		} catch (Exception e) {
			log.error("", e);
			return packError(Response.serverError(),e).build();
		}
	}
}
