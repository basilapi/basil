package uk.ac.open.kmi.basil.swagger;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import uk.ac.open.kmi.basil.MoreMediaType;
import uk.ac.open.kmi.basil.doc.Doc;
import uk.ac.open.kmi.basil.doc.Doc.Field;
import uk.ac.open.kmi.basil.sparql.QueryParameter;
import uk.ac.open.kmi.basil.sparql.Specification;

import javax.ws.rs.core.MediaType;

public class SwaggerJsonBuilder {
	@SuppressWarnings("unchecked")
	public static JSONObject build(String id, Specification spec, Doc doc,
								   String basilRoot) {
		JSONObject root = new JSONObject();
		root.put("swaggerVersion", "1.2");
		root.put("basePath", basilRoot.substring(0, basilRoot.length() - 1));
		root.put("resourcePath", id);
		JSONArray apis = new JSONArray();

		// For each API
		JSONObject api = new JSONObject();
		api.put("path", "/" + id + "/api");
		api.put("resourcePath", "/" + id + "/api");
		JSONArray operations = new JSONArray();
		JSONObject op = new JSONObject();
		op.put("method", "GET");
		op.put("nickname", "API");
		op.put("summary", doc.get(Field.DESCRIPTION));
		op.put("type", Types.List.toString());
		JSONArray produces = new JSONArray();
		for (MediaType kt : MoreMediaType.extensions.values()) {
			produces.add(kt.toString());
		}
		op.put("produces", produces);
		JSONArray params = new JSONArray();
		JSONArray params2 = new JSONArray();
		JSONObject par;
//		par= new JSONObject();
//		par.put("name", "id");
//		par.put("description", "Id of the BASIL API");
//		par.put("required", true);
//		par.put("type", Types.String.toString());
//		par.put("paramType", "path");
//		params.add(par);
		for (QueryParameter p : spec.getParameters()) {
			par = new JSONObject();
			par.put("name", p.getName());
			par.put("description", ""); // TODO See issue #16
			par.put("required", !p.isOptional());
			par.put("type", Types.String.toString());
			par.put("paramType", "query");
			params.add(par);
			params2.add(par);
		}
		op.put("parameters", params);
		operations.add(op);

		JSONObject api2 = new JSONObject();
		api2.put("path", "/" + id + "/api{ext}");
		api2.put("resourcePath", "/" + id + "/api");
		JSONArray operations2 = new JSONArray();
		JSONObject op2 = new JSONObject();
		op2.put("method", "GET");
		op2.put("nickname", "APIext");
		op2.put("summary", doc.get(Field.DESCRIPTION));
		op2.put("type", Types.List.toString());
		op2.put("produces", produces);

		par = new JSONObject();
		par.put("name", "ext");
		par.put("description", "Extension of the output data format (e.g., .json, .xml)");
		par.put("required", "false");
		par.put("type", Types.String.toString());
		par.put("paramType", "path");
		par.put("allowMultiple", "false");

		params2.add(par);
		op2.put("parameters", params2);
		operations2.add(op2);

		api.put("operations", operations);
		api2.put("operations", operations2);
		apis.add(api);
		apis.add(api2);
		root.put("apis", apis);
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
