package uk.ac.open.kmi.basil.sparql;

import java.util.Set;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementWalker;
import org.apache.jena.update.Update;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;

public class SpecificationFactory {

	public static Specification create(String endpoint, String sparql) {
		VariablesCollector collector = new VariablesCollector();
		Query q = QueryFactory.create(sparql);
		Element element = q.getQueryPattern();
		ElementWalker.walk(element, collector);
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
	
	public static Specification createUpdate(String endpoint, String sparql) {
		VariablesCollector collector = new VariablesCollector();
		UpdateRequest request = UpdateFactory.create(sparql);
		for(Update update : request.getOperations()) {
			update.visit(collector);
		}
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
}
