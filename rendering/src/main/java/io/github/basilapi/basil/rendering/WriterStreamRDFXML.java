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

package io.github.basilapi.basil.rendering;

import java.io.OutputStream;
import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.riot.writer.WriterStreamRDFBlocks;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.LiteralLabel;

class WriterStreamRDFXML extends WriterStreamRDFBlocks {
	private boolean preamble = true;
	public WriterStreamRDFXML(OutputStream os) {
		super(os);
	}

	private Pair<String, String> ns(String uri) {
		char c = '#';
		if (uri.lastIndexOf(c) == -1) {
			c = '/';
		}
		return new ImmutablePair<String, String>(uri.substring(0, uri.lastIndexOf(c) + 1), uri.substring(uri.lastIndexOf(c) + 1));
	}
	
	private void openDescription(String subject){
		if(preamble){
			openRdf();
			preamble = false;
		}
		// Subject
		out.print("\n<rdf:Description rdf:about=\"");
		out.print(subject);
		out.print("\">");
	}
	
	private void closeDescription(){
		out.print("\n</rdf:Description>");
	}
	@Override
	protected void finalizeRun() {
		closeRdf();
		super.finalizeRun();
	}
	private void openRdf(){
		out.print("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">");
	}
	
	private void closeRdf(){
		out.print("\n</rdf:RDF>");
	}
	
	@Override
	protected void printBatchTriples(Node s, List<Triple> triples) {
		openDescription(s.getURI());
		for(Triple triple : triples){
			// Property
			// XMLNS
			out.print("\n\t<p:");
			Pair<String, String> pp = ns(triple.getPredicate().getURI());
			out.print(pp.getRight());
			out.print(" xmlns:p=\"");
			out.print(pp.getLeft());
			out.print("\"");
			// Object
			if (triple.getObject().isURI()) {
				out.print(' ');
				out.print("rdf:resource=\"");
				out.print(triple.getObject().getURI());
				out.print("\"/>");
			} else if (triple.getObject().isBlank()) {
				out.print(' ');
				out.print("rdf:nodeID=\"");
				out.print(triple.getObject().getBlankNodeLabel());
				out.print("\"/>");
			} else if (triple.getObject().isLiteral()) {
				LiteralLabel l = triple.getObject().getLiteral();
				if (!l.language().equals("")) {
					// Lang
					out.print(' ');
					out.print("xml:lang=\"");
					out.print(l.language());
					out.print("\">");
					out.print(l.getLexicalForm());
					out.print("</");
					out.print(pp.getRight());
					out.print(">");
				} else if (l.getDatatypeURI() != null) {
					// Lang
					out.print(' ');
					out.print("rdf:datatype=\"");
					out.print(l.getDatatypeURI());
					out.print("\">");
					out.print(l.getLexicalForm());
					out.print("</p:");
					out.print(pp.getRight());
					out.print(">");
				}else{
					out.print(">");
					out.print(l.getLexicalForm());
					out.print("</");
					out.print(pp.getRight());
					out.print(">");
				}
			}
		}
		closeDescription();
	}

//	@Override
//	protected void reset() {
//
//	};

}
