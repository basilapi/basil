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

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.github.basilapi.basil.sparql.QueryParameter;
import io.github.basilapi.basil.sparql.Specification;
import io.github.basilapi.basil.sparql.VariablesBinder;
import org.apache.jena.query.Query;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Path("{id}/explain")
@Api(value = "/basil", description = "BASIL operations")
public class ExplainResource extends AbstractResource {

	@GET
	@Produces("application/json")
	@ApiOperation(value = "Explain API invocation")
	@ApiResponses(value = {
    		@ApiResponse(code = 200, message = "OK"),
    		@ApiResponse(code = 404, message = "Not Found"),
    		@ApiResponse(code = 500, message = "Internal error") 
    })
	public Response get(@PathParam("id") String id) {
		try {
			try {
				// supports alias
				id = getApiId(id); 
			}catch(IOException e) {
				return packError(Response.status(404), "Not Found").build();
			}

			Specification specification = getApiManager().getSpecification(id);
			VariablesBinder binder = new VariablesBinder(specification);

			List<String> missing = new ArrayList<String>();
			for (QueryParameter qp : specification.getParameters()) {
				if (requestUri.getQueryParameters().containsKey(qp.getName())) {
					List<String> values = requestUri.getQueryParameters().get(qp.getName());
					binder.bind(qp.getName(), values.get(0));
				} else if (!qp.isOptional()) {
					missing.add(qp.getName());
				}
			}

			Query q = binder.toQuery();
			JsonObject m = new JsonObject();
			m.add("query", new JsonPrimitive(q.toString()));

			ResponseBuilder builder = Response.ok(m.toString());
			addHeaders(builder, id);
			builder.header(Headers.Endpoint, specification.getEndpoint());
			return builder.build();
		} catch (IOException e) {
			return packError(Response.serverError(), e).build();
		}
	}
}
