package uk.ac.open.kmi.stoner.sparql;

import java.io.IOException;

import org.apache.commons.io.IOUtils;

import uk.ac.open.kmi.stoner.sparql.QueryParameter.Type;

public class TestUtils {

	private static String loadQueryString(String qname) throws IOException {
		return IOUtils.toString(TestUtils.class.getClassLoader()
				.getResourceAsStream("./sparql/" + qname + ".txt"), "UTF-8");
	}

	private static String endpoint(String qname) {
		int pos = qname.indexOf("X-Stoner-Endpoint:");
		int eol = qname.indexOf('\n', pos);
		return qname.substring(pos, eol).trim();
	}

	public static Specification loadQuery(String fileName) throws IOException {
		String sparql = loadQueryString(fileName);
		String endpoint = endpoint(sparql);
		return SpecificationFactory.create(endpoint, sparql);
	}

	public static QueryParameter buildQueryParameter(String name, Type type,
			String lang, String datatype) {
		QueryParameter qp = new QueryParameter();
		qp.setName(name);
		if (type == Type.IRI) {
			qp.setIri();
		} else if (type == Type.TypedLiteral) {
			qp.setDatatype(datatype);
		} else if (type == Type.LangedLiteral) {
			qp.setLang(lang);
		} else if (type == Type.PlainLiteral) {
			qp.setPlainLiteral();
		}
		return qp;
	}
}
