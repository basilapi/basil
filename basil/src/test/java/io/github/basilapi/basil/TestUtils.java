package io.github.basilapi.basil;

import io.github.basilapi.basil.sparql.QueryParameter;
import io.github.basilapi.basil.sparql.Specification;
import io.github.basilapi.basil.sparql.SpecificationFactory;
import io.github.basilapi.basil.sparql.UnknownQueryTypeException;
import org.apache.commons.io.IOUtils;

import java.io.IOException;

public class TestUtils {
//
	public static String loadQueryString(String qname) throws IOException {
		return IOUtils.toString(TestUtils.class.getClassLoader().getResourceAsStream("./sparql/" + qname + ".txt"),
				"UTF-8");
	}
//
	public static String endpoint(String qname) {
		int pos = qname.indexOf("X-Basil-Endpoint:");
		int len = ("X-Basil-Endpoint:").length();
		int eol = qname.indexOf('\n', pos);
		return qname.substring(pos + len, eol).trim();
	}
//
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


	public static String loadTemplate(String type, String qname) throws IOException {
		return IOUtils.toString(
				TestUtils.class.getClassLoader().getResourceAsStream("./" + type + "/" + qname + ".tmpl"), "UTF-8");
	}
}
