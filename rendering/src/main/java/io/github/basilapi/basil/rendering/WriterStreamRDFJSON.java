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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonString;
import org.apache.jena.riot.writer.WriterStreamRDFBlocks;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.LiteralLabel;

class WriterStreamRDFJSON extends WriterStreamRDFBlocks {
	private boolean preamble = true;

	public WriterStreamRDFJSON(OutputStream os) {
		super(os);
	}

	private void openDescription(String subject) {
		if (preamble) {
			openRdf();
			preamble = false;
		}
		// Subject
		out.print("{");
		out.print('"');
		JSON.write(out, new JsonString(subject));
		out.print('"');
		out.print(':');
	}

	private void closeDescription() {
		out.print("}");
	}

	@Override
	protected void finalizeRun() {
		closeRdf();
		super.finalizeRun();
	}

	private void openRdf() {

	}

	private void closeRdf() {

	}

	@Override
	protected void printBatchTriples(Node s, List<Triple> triples) {
		openDescription(s.getURI());
		Map<String, List<Node>> subtree = new HashMap<String, List<Node>>();

		for (Triple triple : triples) {
			// XXX
			if (!s.equals(triple.getSubject())) {
				throw new RuntimeException("Assumption violated: subject differs from node!");
			}
			String p = triple.getPredicate().getURI();
			if (!subtree.containsKey(p)) {
				subtree.put(p, new ArrayList<Node>());
			}
			subtree.get(p).add(triple.getObject());
		}

		out.print("{");
		boolean first = true;
		for (Entry<String, List<Node>> entry : subtree.entrySet()) {
			if (first) {
				first = false;
			} else {
				out.print(',');
			}
			// Property
			JSON.write(out, new JsonString(entry.getKey()));
			out.print(": [");
			// Values
			boolean firstv = true;
			for (Node v : entry.getValue()) {
				if (firstv) {
					firstv = false;
				} else {
					out.print(',');
				}
				
				out.print('{');
				// Object
				if (v.isURI()) {
					JSON.write(out, new JsonString("type"));
					out.print(':');
					JSON.write(out, new JsonString("uri"));
					out.print(',');
					JSON.write(out, new JsonString("value"));
					out.print(':');
					JSON.write(out, new JsonString(v.getURI()));
				} else if (v.isBlank()) {
					JSON.write(out, new JsonString("type"));
					out.print(':');
					JSON.write(out, new JsonString("bnode"));
					out.print(',');
					JSON.write(out, new JsonString("value"));
					out.print(':');
					out.print(v.getBlankNodeLabel());
				} else if (v.isLiteral()) {
					LiteralLabel l = v.getLiteral();

					JSON.write(out, new JsonString("type"));
					out.print(':');
					JSON.write(out, new JsonString("literal"));
					out.print(',');
					JSON.write(out, new JsonString("value"));
					out.print(':');
					out.print(l.getLexicalForm());
					if (!l.language().equals("")) {
						out.print(',');
						out.print("lang:");
						JSON.write(out, new JsonString(l.language()));
					} else if (l.getDatatypeURI() != null) {
						out.print(',');
						out.print("datatype:");
						JSON.write(out, new JsonString(l.getDatatypeURI()));
					} 
				}
				out.print('}');
			}
		}
		closeDescription();
	}

	// @Override
	// protected void reset() {
	//
	// };

}
