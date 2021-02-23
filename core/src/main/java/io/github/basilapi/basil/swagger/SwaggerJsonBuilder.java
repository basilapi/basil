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

package io.github.basilapi.basil.swagger;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.github.basilapi.basil.doc.Doc;
import io.github.basilapi.basil.rendering.MoreMediaType;
import io.github.basilapi.basil.sparql.QueryParameter;
import io.github.basilapi.basil.sparql.Specification;

import javax.ws.rs.core.MediaType;

public class SwaggerJsonBuilder {

	public static JsonObject build(String id, Specification spec, Doc doc,
								   String basilRoot) {
		JsonObject root = new JsonObject();
		root.add("swaggerVersion", new JsonPrimitive("1.2"));
		root.add("basePath", new JsonPrimitive(basilRoot.substring(0, basilRoot.length() - 1)));
		root.add("resourcePath", new JsonPrimitive(id));
		JsonArray apis = new JsonArray();

		// For each API
		JsonObject api = new JsonObject();
		api.add("path", new JsonPrimitive("/" + id + "/api"));
		api.add("resourcePath", new JsonPrimitive("/" + id + "/api"));
		JsonArray operations = new JsonArray();
		JsonObject op = new JsonObject();
		op.add("method", new JsonPrimitive("GET"));
		op.add("nickname", new JsonPrimitive("API"));
		op.add("summary", new JsonPrimitive(doc.get(Doc.Field.DESCRIPTION)));
		op.add("type", new JsonPrimitive("void"));
		JsonArray produces = new JsonArray();
		for (MediaType kt : MoreMediaType.extensions.values()) {
			produces.add(new JsonPrimitive(kt.toString()));
		}
		op.add("produces", produces);
		JsonArray params = new JsonArray();
		JsonArray params2 = new JsonArray();
		JsonObject par;
//		par= new JsonObject();
//		par.add("name", "id");
//		par.add("description", "Id of the BASIL API");
//		par.add("required", true);
//		par.add("type", Types.String.toString());
//		par.add("paramType", "path");
//		params.add(par);
		for (QueryParameter p : spec.getParameters()) {
			par = new JsonObject();
			par.add("name", new JsonPrimitive(p.getName()));
			par.add("description", new JsonPrimitive("")); // TODO See issue #16
			par.add("required", new JsonPrimitive(!p.isOptional()));
			par.add("type", new JsonPrimitive(Types.String.toString()));
			par.add("paramType", new JsonPrimitive("query"));
			params.add(par);
			params2.add(par);
		}
		op.add("parameters", params);
		operations.add(op);

		JsonObject api2 = new JsonObject();
		api2.add("path", new JsonPrimitive("/" + id + "/api{ext}"));
		api2.add("resourcePath", new JsonPrimitive("/" + id + "/api"));
		JsonArray operations2 = new JsonArray();
		JsonObject op2 = new JsonObject();
		op2.add("method", new JsonPrimitive("GET"));
		op2.add("nickname", new JsonPrimitive("APIext"));
		op2.add("summary", new JsonPrimitive(doc.get(Doc.Field.DESCRIPTION)));
		op2.add("type", new JsonPrimitive("void"));
		op2.add("produces", produces);

		par = new JsonObject();
		par.add("name", new JsonPrimitive("ext"));
		par.add("description", new JsonPrimitive("Extension of the output data format (e.g., .json, .xml)"));
		par.add("required", new JsonPrimitive("false"));
		par.add("type", new JsonPrimitive(Types.String.toString()));
		par.add("paramType", new JsonPrimitive("path"));
		par.add("allowMultiple", new JsonPrimitive("false"));

		params2.add(par);
		op2.add("parameters", params2);
		operations2.add(op2);

		api.add("operations", operations);
		api2.add("operations", operations2);
		apis.add(api);
		apis.add(api2);
		root.add("apis", apis);

		// create model section
		// TODO create proper model according to the data model from the SPARQL endpoint

		return root;
	}

	public enum Types {
		List("List"), String("string");
		private String strval;

		Types(String strval) {
			this.strval = strval;
		}

		public String toString() {
			return this.strval;
		}
	}
}
