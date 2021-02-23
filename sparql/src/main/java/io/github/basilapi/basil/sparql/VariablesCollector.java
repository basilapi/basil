/*
 * Copyright (c) 2021. Enrico Daga and Luca Panziera
 *
 * MLicensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.github.basilapi.basil.sparql;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.jena.atlas.lib.Sink;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryException;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.modify.request.QuadAcc;
import org.apache.jena.sparql.modify.request.Target;
import org.apache.jena.sparql.modify.request.UpdateAdd;
import org.apache.jena.sparql.modify.request.UpdateClear;
import org.apache.jena.sparql.modify.request.UpdateCopy;
import org.apache.jena.sparql.modify.request.UpdateCreate;
import org.apache.jena.sparql.modify.request.UpdateDataDelete;
import org.apache.jena.sparql.modify.request.UpdateDataInsert;
import org.apache.jena.sparql.modify.request.UpdateDeleteWhere;
import org.apache.jena.sparql.modify.request.UpdateDrop;
import org.apache.jena.sparql.modify.request.UpdateLoad;
import org.apache.jena.sparql.modify.request.UpdateModify;
import org.apache.jena.sparql.modify.request.UpdateMove;
import org.apache.jena.sparql.modify.request.UpdateVisitor;
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
import org.apache.jena.update.Update;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;

public class VariablesCollector implements ElementVisitor, UpdateVisitor {

	private Set<String> variables = new HashSet<String>();

	public Set<String> getVariables() {
		return Collections.unmodifiableSet(variables);
	}

	/**
	 * XXX General method supporting query/update and also cases in which
	 * variables are not allowed (INSERT DATA)
	 * 
	 * @param query
	 */
	public void collect(String query) {
		reset();
		//System.err.println(query);
		try {
			Query q = QueryFactory.create(query);
			Element element = q.getQueryPattern();
			ElementWalker.walk(element, this);
		} catch (QueryException qe) {
			//System.err.println(qe.getMessage());
			try {
				UpdateRequest request = UpdateFactory.create(query);
				for (Update update_ : request.getOperations()) {
					update_.visit(this);
				}
			} catch (QueryException qe2) {
				// XXX Some SPARQL update queries do not allow for variables.
				// Implementing a "weak" method for getting variables out of the query string
				// From https://stackoverflow.com/questions/36292591/splitting-a-nested-string-keeping-quotation-marks
				//String str = "This is \"a string\" and this is \"a \\\"nested\\\" string\""; 
				Pattern ptrn = Pattern.compile("\"[^\"\\\\]*(?:\\\\.[^\"\\\\]*)*\"|\\S+");
				Matcher matcher = ptrn.matcher(query);
				while (matcher.find()) {
				    String l = matcher.group(0);
				    l = l.trim();
					if(l.startsWith("?") || l.startsWith("$")) {
						variables.add(l);
					}
				}
			}
		}
	}

	public void reset() {
		variables = new HashSet<String>();
	}

	private void collectFromTriple(Triple triple) {
		collectFromNode(triple.getPredicate());
		collectFromNode(triple.getSubject());
		collectFromNode(triple.getObject());
	}

	private void collectFromExpr(Expr expr) {
		for (Var var : expr.getVarsMentioned()) {
			variables.add(var.toString());
		}
	}

	private void collectFromQuads(List<Quad> quads) {
		for (Quad quad : quads) {
			collectFromNode(quad.getGraph());
			collectFromTriple(quad.asTriple());
		}
	}

	private void collectFromNodes(List<Node> nodes) {
		for (Node node : nodes) {
			collectFromNode(node);
		}
	}

	private void collectFromNode(Node node) {
		if (node != null && node.isVariable()) {
			variables.add(node.toString());

		}
	}

	private void collectFromTarget(Target target) {
		collectFromNode(target.getGraph());
	}

	public void visit(ElementTriplesBlock el) {
		for (Triple triple : el.getPattern().getList()) {
			collectFromTriple(triple);
		}
	}

	public void visit(ElementPathBlock el) {
		for (TriplePath triple : el.getPattern().getList()) {
			if (triple.isTriple()) {
				collectFromTriple(triple.asTriple());
			} else {
				// is a path, no variables in path expressions
				collectFromNode(triple.getSubject());
				collectFromNode(triple.getObject());
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

	// UPDATE VISITOR

	@Override
	public void visit(UpdateModify update) {
		// DELETE / INSERT / WHERE
		QuadAcc acc = update.getDeleteAcc();
		collectFromNode(acc.getGraph());
		collectFromQuads(acc.getQuads());
		//
		collectFromQuads(update.getDeleteQuads());
		QuadAcc insAcc = update.getInsertAcc();
		collectFromNode(insAcc.getGraph());
		collectFromQuads(insAcc.getQuads());
		//
		collectFromQuads(update.getInsertQuads());
		collectFromNodes(update.getUsing());
		collectFromNodes(update.getUsingNamed());
		//
		Element where = update.getWherePattern();
		ElementWalker.walk(where, this);
		collectFromNode(update.getWithIRI());
	}

	@Override
	public void visit(UpdateDeleteWhere update) {
		collectFromQuads(update.getQuads());
	}

	@Override
	public void visit(UpdateDataDelete update) {
		collectFromQuads(update.getQuads());
	}

	@Override
	public void visit(UpdateDataInsert update) {
		collectFromQuads(update.getQuads());
	}

	@Override
	public void visit(UpdateMove update) {
		collectFromTarget(update.getSrc());
		collectFromTarget(update.getDest());
	}

	@Override
	public void visit(UpdateCopy update) {
		collectFromTarget(update.getSrc());
		collectFromTarget(update.getDest());
	}

	@Override
	public void visit(UpdateAdd update) {
		collectFromTarget(update.getSrc());
		collectFromTarget(update.getDest());
	}

	@Override
	public void visit(UpdateLoad update) {
		String source = update.getSource();
		VariableParser p = new VariableParser(source);
		if (p.isParameter()) {
			variables.add(source);
		}
		collectFromNode(update.getDest());
	}

	@Override
	public void visit(UpdateCreate update) {
		if (update.getGraph().isVariable()) {
			variables.add(update.getGraph().toString());
		}
	}

	@Override
	public void visit(UpdateClear update) {
		collectFromTarget(update.getTarget());
		if (update.getGraph().isVariable()) {
			variables.add(update.getGraph().toString());
		}
	}

	@Override
	public void visit(UpdateDrop update) {
		collectFromTarget(update.getTarget());
		if (update.getGraph().isVariable()) {
			variables.add(update.getGraph().toString());
		}
	}

	@Override
	public Sink<Quad> createInsertDataSink() {
		throw new RuntimeException("Method not implemented");
	}

	@Override
	public Sink<Quad> createDeleteDataSink() {
		throw new RuntimeException("Method not implemented");
	}
}
