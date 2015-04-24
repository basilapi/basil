package uk.ac.open.kmi.basil.sparql;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.sparql.core.TriplePath;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementAssign;
import com.hp.hpl.jena.sparql.syntax.ElementBind;
import com.hp.hpl.jena.sparql.syntax.ElementData;
import com.hp.hpl.jena.sparql.syntax.ElementDataset;
import com.hp.hpl.jena.sparql.syntax.ElementExists;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementMinus;
import com.hp.hpl.jena.sparql.syntax.ElementNamedGraph;
import com.hp.hpl.jena.sparql.syntax.ElementNotExists;
import com.hp.hpl.jena.sparql.syntax.ElementOptional;
import com.hp.hpl.jena.sparql.syntax.ElementPathBlock;
import com.hp.hpl.jena.sparql.syntax.ElementService;
import com.hp.hpl.jena.sparql.syntax.ElementSubQuery;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;
import com.hp.hpl.jena.sparql.syntax.ElementUnion;
import com.hp.hpl.jena.sparql.syntax.ElementVisitor;
import com.hp.hpl.jena.sparql.syntax.ElementWalker;

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
