package uk.ac.open.kmi.basil.sparql;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementAssign;
import org.apache.jena.sparql.syntax.ElementBind;
import org.apache.jena.sparql.syntax.ElementData;
import org.apache.jena.sparql.syntax.ElementDataset;
import org.apache.jena.sparql.syntax.ElementExists;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementMinus;
import org.apache.jena.sparql.syntax.ElementNamedGraph;
import org.apache.jena.sparql.syntax.ElementNotExists;
import org.apache.jena.sparql.syntax.ElementOptional;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.sparql.syntax.ElementService;
import org.apache.jena.sparql.syntax.ElementSubQuery;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import org.apache.jena.sparql.syntax.ElementUnion;
import org.apache.jena.sparql.syntax.ElementVisitor;
import org.apache.jena.sparql.syntax.ElementWalker;

public class VariablesCollector implements ElementVisitor {

	private Set<String> variables = new HashSet<String>();

	public Set<String> getVariables() {
		return Collections.unmodifiableSet(variables);
	}

	public void collect(String query){
		reset();
		Query q = QueryFactory.create(query);
		Element element = q.getQueryPattern();		
		ElementWalker.walk(element, this);
	}
	
	public void reset() {
		variables = new HashSet<String>();
	}

	private void collectFromTriple(Triple triple) {
		if (triple.getPredicate().isVariable()) {
			variables.add(triple.getPredicate().toString());
		}
		if (triple.getSubject().isVariable()) {
			variables.add(triple.getSubject().toString());
		}
		if (triple.getObject().isVariable()) {
			variables.add(triple.getObject().toString());
		}
	}

	private void collectFromExpr(Expr expr){
		for(Var var : expr.getVarsMentioned()){
			variables.add(var.toString());
		}
	}
	
	public void visit(ElementTriplesBlock el) {
		for (Triple triple : el.getPattern().getList()) {
			collectFromTriple(triple);
		}
	}

	public void visit(ElementPathBlock el) {
		for (TriplePath triple : el.getPattern().getList()) {
			if(triple.isTriple()){
				collectFromTriple(triple.asTriple());
			}else{
				// is a path
				if (triple.getSubject().isVariable()) {
					variables.add(triple.getSubject().toString());
				}
				if (triple.getObject().isVariable()) {
					variables.add(triple.getObject().toString());
				}
			}
		}
	}

	public void visit(ElementFilter el) {
		collectFromExpr(el.getExpr());
	}

	public void visit(ElementAssign el) {
		collectFromExpr(el.getExpr());
	}

	public void visit(ElementBind el) {
		collectFromExpr(el.getExpr());
	}

	public void visit(ElementData el) {
		//
	}

	public void visit(ElementUnion el) {
		// 
	}

	public void visit(ElementOptional el) {
		// 
	}

	public void visit(ElementGroup el) {
		// 
	}

	public void visit(ElementDataset el) {
		// 
	}

	public void visit(ElementNamedGraph el) {
		// 
	}

	public void visit(ElementExists el) {
		// 
	}

	public void visit(ElementNotExists el) {
		// 
	}

	public void visit(ElementMinus el) {
		// 
	}

	public void visit(ElementService el) {
		// 
	}

	public void visit(ElementSubQuery el) {
		// 
	}

}
