package io.github.basilapi.basil.sparql;

import java.util.Set;

public class SpecificationFactory {
	
	public static Specification create(String endpoint, String sparql) throws UnknownQueryTypeException {
		if(QueryType.isUpdate(sparql)) {
			return createUpdate(endpoint, sparql);
		} else {
			return createQuery(endpoint, sparql);
		}
	}
	
	private static Specification createQuery(String endpoint, String sparql) {
		VariablesCollector collector = new VariablesCollector();
		collector.collect(sparql);
		Set<String> vars = collector.getVariables();
		VariableParser parser;
		Specification spec = new Specification();
		spec.setEndpoint(endpoint);
		spec.setQuery(sparql);
		
		for (String var : vars) {
			parser = new VariableParser(var);
			if (parser.isParameter()) {
				spec.map(var, parser.getParameter());
			}
		}
		return spec;
	}
	
	private static Specification createUpdate(String endpoint, String sparql) {
		VariablesCollector collector = new VariablesCollector();
		collector.collect(sparql);
		Set<String> vars = collector.getVariables();
		VariableParser parser;
		Specification spec = new Specification();
		spec.setEndpoint(endpoint);
		spec.setUpdate(sparql);
		
		for (String var : vars) {
			parser = new VariableParser(var);
			if (parser.isParameter()) {
				spec.map(var, parser.getParameter());
			}
		}
		return spec;
	}
}
