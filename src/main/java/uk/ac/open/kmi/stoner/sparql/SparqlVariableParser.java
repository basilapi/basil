package uk.ac.open.kmi.stoner.sparql;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SparqlVariableParser {

	private String variable;
	private String name;
	private boolean isParameter = false;
	private boolean isForcedIri = false;
	public SparqlVariableParser(String variable) {
		this.variable = variable;
		Pattern p = Pattern
				.compile("[\\$\\?]_([^_]+)_?([a-zA-Z0-9]+)?_?([a-zA-Z0-9]+)?.*$");
		Matcher m = p.matcher(this.variable);
		if (m.matches()) {
			this.isParameter = true;
			this.name = m.group(1);
			if(m.group(2) != null && m.group(2).toLowerCase().equals("iri")){
				this.isForcedIri = true;
			} else {
				this.isForcedIri = false;
			}
		} 
	}

	public boolean isParameter() {
		return isParameter;
	}

	public String getParameterName() throws NotAParameterException {
		if (name != null) {
			return this.name;
		} else {
			throw new NotAParameterException();
		}
	}

	public boolean isForcedIri() throws NotAParameterException{
		if(!isParameter()){
			throw new NotAParameterException();
		}else{
			return this.isForcedIri;
		}
	}
}
