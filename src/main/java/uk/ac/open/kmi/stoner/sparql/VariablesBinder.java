package uk.ac.open.kmi.stoner.sparql;

import com.hp.hpl.jena.datatypes.BaseDatatype;
import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.query.Query;

public class VariablesBinder {

	private ParameterizedSparqlString pss;
	private Specification spec;

	public VariablesBinder(Specification spec, String... bindings) {
		this.spec = spec;
		this.pss = new ParameterizedSparqlString(spec.getQuery());
		for (int x = 0; x < bindings.length; x += 2) {
			bind(bindings[x], bindings[x + 1]);
		}

	}

	public VariablesBinder bind(String name, String value) {
		if (spec.hasParameter(name)) {
			QueryParameter p = spec.getParameter(name);
			if (p.isIri()) {
				bindIri(name, value);
			} else if (p.isLangedLiteral()) {
				bindLangedLiteral(name, value, p.getLang());
			} else if (p.isTypedLiteral()) {
				bindTypedLiteral(name, value, p.getDatatype());
			} else {
				// Default is PlainLiteral
				bindPlainLiteral(name, value);
			}
		}
		return this;
	}

	public VariablesBinder bindTypedLiteral(String name, String value,
			String datatype) {
		pss.setLiteral(name, value, new BaseDatatype(datatype));
		return this;
	}

	private VariablesBinder bindPlainLiteral(String name, String value) {
		pss.setLiteral(name, value);
		return this;
	}

	private VariablesBinder bindLangedLiteral(String name, String value,
			String lang) {
		pss.setLiteral(name, value, lang);
		return this;
	}

	private VariablesBinder bindIri(String name, String value) {
		pss.setIri(name, value);
		return this;
	}

	public String toString() {
		return toQuery().toString();
	}

	public Query toQuery() {
		return pss.asQuery();
	}
}
