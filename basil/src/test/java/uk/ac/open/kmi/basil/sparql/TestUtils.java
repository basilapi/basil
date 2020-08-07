package uk.ac.open.kmi.basil.sparql;

import org.apache.commons.io.IOUtils;
import uk.ac.open.kmi.basil.rest.core.Headers;
import uk.ac.open.kmi.basil.sparql.QueryParameter.Type;

import java.io.IOException;

public class TestUtils {

	public static String loadQueryString(String qname) throws IOException {
		return IOUtils.toString(TestUtils.class.getClassLoader().getResourceAsStream("./sparql/" + qname + ".txt"),
				"UTF-8");
	}

	public static String endpoint(String qname) {
		int pos = qname.indexOf(Headers.Endpoint + ":");
		int len = (Headers.Endpoint + ":").length();
		int eol = qname.indexOf('\n', pos);
		return qname.substring(pos + len, eol).trim();
	}

	public static Specification loadQuery(String fileName) throws IOException {
		String sparql = loadQueryString(fileName);
		String endpoint = endpoint(sparql);
		// System.out.println(endpoint);
		try {
			return SpecificationFactory.create(endpoint, sparql);
		} catch (UnknownQueryTypeException e) {
			throw new IOException(e);
		}
	}

	public static QueryParameter buildQueryParameter(String name, Type type, String lang, String datatype) {
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

	public static String loadTemplate(String type, String qname) throws IOException {
		return IOUtils.toString(
				TestUtils.class.getClassLoader().getResourceAsStream("./" + type + "/" + qname + ".tmpl"), "UTF-8");
	}
}
