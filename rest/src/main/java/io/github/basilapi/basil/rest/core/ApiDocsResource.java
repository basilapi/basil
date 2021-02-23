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
import com.wordnik.swagger.annotations.*;
import io.github.basilapi.basil.doc.Doc;
import io.github.basilapi.basil.sparql.Specification;
import io.github.basilapi.basil.swagger.SwaggerJsonBuilder;
import io.github.basilapi.basil.swagger.SwaggerUIBuilder;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import java.io.IOException;


@Path("{id}/api-docs")
@Api(value = "/basil", description = "BASIL operations")
public class ApiDocsResource extends AbstractResource {
	
	@GET
	@Produces({"application/json", "text/html", "*/*"})
	@ApiOperation(value = "Get API Swagger Description")
	@ApiResponses(value = {
    		@ApiResponse(code = 200, message = "OK"),
    		@ApiResponse(code = 404, message = "Not Found"),
    		@ApiResponse(code = 500, message = "Internal error") 
    })
	public Response get(@PathParam("id") String id,
						@ApiParam(value = "Accepted Media Type", allowableValues = "application/json,text/html")
						@HeaderParam("Accept") String accept) {
		try {
			try {
				// supports alias
				id = getApiId(id); 
			}catch(IOException e) {
				return packError(Response.status(404), "Not Found").build();
			}

			 if (accept.contains("text/html")) {
				String msg = SwaggerUIBuilder.build(requestUri.getRequestUri());
				ResponseBuilder builder = Response.ok(msg);
				addHeaders(builder, id);
				return builder.build();
			}else if (accept.contains("application/json") || accept.contains("*/*")) {
				 Specification specification = getApiManager().getSpecification(id);
				 Doc docs = getApiManager().getDoc(id);
				 JsonObject o = SwaggerJsonBuilder.build(id, specification, docs, requestUri.getBaseUri().toString());
				 ResponseBuilder builder = Response.ok(o.toString());
				addHeaders(builder, id);
				return builder.build();
			} 
			return Response.status(406).build();
		} catch (IOException e) {
			throw new WebApplicationException(e);
		}
	}
}
