package uk.ac.open.kmi.stoner.sparql;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hp.hpl.jena.vocabulary.XSD;

public class VariableParser {
	private String variable;
	private boolean isParameter = false;
	private Map<String, String> prefixes;
	private boolean isError = false;
	private Exception exception = null;
	private QueryParameter p = null;

	public VariableParser(String variable) {
		this.variable = variable;
		this.prefixes = new HashMap<String, String>();
		this.prefixes.put("xsd", XSD.getURI());
		try {
			parse();
		} catch (ParameterException e) {
			this.exception = e;
			this.isError = true;
		}
	}

	public VariableParser(String variable, Map<String, String> prefixes) {
		this.variable = variable;
		this.prefixes = prefixes;
		try {
			parse();
		} catch (ParameterException e) {
			this.exception = e;
			this.isError = true;
		}
	}

	private void parse() throws ParameterException {
		Pattern pt = Pattern
				.compile("[\\$\\?]([_]{1,2})([^_]+)_?([a-zA-Z0-9]+)?_?([a-zA-Z0-9]+)?.*$");
		Matcher m = pt.matcher(this.variable);
		if (m.matches()) {
			this.isParameter = true;

			this.p = new QueryParameter();
			p.setOptional(m.group(1).length() == 2);
			p.setName(m.group(2));
			if (m.group(3) != null) {
				if (m.group(3).toLowerCase().equals("iri")) {
					p.setIri();
				} else if (m.group(3).toLowerCase().equals("literal")) {
					p.setPlainLiteral();
				} else if (m.group(3).length() == 2 && m.group(4) == null) {
					// specifies lang
					p.setLang(m.group(3).toLowerCase());
				} else if (m.group(4) != null) {
					String datatypePrefix = m.group(3);
					String datatypeLocalName = m.group(4);
					String namespace = prefixes.get(datatypePrefix);
					if (namespace == null) {
						exception = new ParameterException("Unknown prefix: "
								+ datatypePrefix);
						isError = true;
					} else {
						p.setDatatype(namespace + datatypeLocalName);
					}
				} else {
					// Let's check if group(3) is a well known XSD Datatype
					Set<String> xsdDatatypes = new HashSet<String>();
					xsdDatatypes.add("string");
					xsdDatatypes.add("int");
					xsdDatatypes.add("integer");
					xsdDatatypes.add("boolean");
					xsdDatatypes.add("double");
					xsdDatatypes.add("long");
					xsdDatatypes.add("anyURI");
					xsdDatatypes.add("date");
					xsdDatatypes.add("dateTime");
					xsdDatatypes.add("gYear");
					xsdDatatypes.add("gMonth");
					// TODO add others
					// ...
					//
					if (xsdDatatypes.contains(m.group(3))) {
						String datatypeLocalName = m.group(3);
						String datatype = XSD.getURI() + datatypeLocalName;
						p.setDatatype(datatype);
					} else {
						isError = true;
						this.exception = new ParameterException(
								"Cannot recognize parameter properties.");
						p.setPlainLiteral();
					}
				}
			} else {
				p.setPlainLiteral();
			}

		} else {
			// It's not a parameter
		}
	}

	public boolean isParameter() {
		return isParameter;
	}

	public QueryParameter getParameter() {
		return p;
	}

	public String getVariable() {
		return variable;
	}

	public boolean isError() {
		return this.isError;
	}

	public Exception getException() {
		return this.exception;
	}
}
