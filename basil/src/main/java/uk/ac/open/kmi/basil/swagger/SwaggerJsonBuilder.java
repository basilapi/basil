package uk.ac.open.kmi.basil.swagger;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import uk.ac.open.kmi.basil.doc.Doc;
import uk.ac.open.kmi.basil.doc.Doc.Field;
import uk.ac.open.kmi.basil.rest.core.MoreMediaType;
import uk.ac.open.kmi.basil.sparql.QueryParameter;
import uk.ac.open.kmi.basil.sparql.Specification;

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
		op.add("summary", new JsonPrimitive(doc.get(Field.DESCRIPTION)));
		op.add("type", new JsonPrimitive(Types.List.toString()));
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
		op2.add("summary", new JsonPrimitive(doc.get(Field.DESCRIPTION)));
		op2.add("type", new JsonPrimitive(Types.List.toString()));
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
