package uk.ac.open.kmi.stoner.sparql;

import java.util.HashSet;
import java.util.Set;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementWalker;

public class SpecificationFactory {

	public static Specification create(String endpoint, String sparql) {
		VariablesCollector collector = new VariablesCollector();
		Query q = QueryFactory.create(sparql);
		Element element = q.getQueryPattern();
		ElementWalker.walk(element, collector);
		Set<String> vars = collector.getVariables();
		VariableParser parser;
		Set<QueryParameter> parameters = new HashSet<QueryParameter>();
		for (String var : vars) {
			parser = new VariableParser(var);
			if (parser.isParameter()) {
				parameters.add(parser.getParameter());
			}
		}
		Specification spec = new Specification();
		spec.setEndpoint(endpoint);
		spec.setQuery(sparql);
		spec.setParameters(parameters);
		return spec;
	}
}
