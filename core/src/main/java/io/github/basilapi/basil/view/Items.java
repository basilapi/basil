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

package io.github.basilapi.basil.view;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.jena.graph.Triple;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

public class Items implements Callable<Iterator<Map<String, String>>> {
	private Iterator<Map<String, String>> items;

	public static Items create(List<Map<String, String>> items) {
		Items o = new Items();
		o.items = items.iterator();
		return o;
	}

	private Items() {
	}

	public static Items create(final ResultSet rs) {
		Items o = new Items();
		o.items = new Iterator<Map<String, String>>() {
			public boolean hasNext() {
				return rs.hasNext();
			}

			public Map<String, String> next() {
				QuerySolution qs = rs.next();
				Iterator<String> vars = qs.varNames();
				Map<String, String> item = new HashMap<String, String>();
				while (vars.hasNext()) {
					String var = vars.next();
					item.put(var, qs.get(var).toString());
				}
				return item;
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
		return o;
	}

	public static Items create(final Boolean rs) {
		Items o = new Items();

		o.items = new Iterator<Map<String, String>>() {
			boolean hasNext = true;

			public boolean hasNext() {
				return hasNext;
			}

			public Map<String, String> next() {
				hasNext = false;
				Map<String, String> item = new HashMap<String, String>();
				item.put("boolean", Boolean.toString(rs));
				return item;
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
		return o;
	}

	public static Items create(final Iterator<Triple> triples) {
		Items o = new Items();
		o.items = new Iterator<Map<String, String>>() {
			public boolean hasNext() {
				return triples.hasNext();
			}

			public Map<String, String> next() {
				Triple qs = triples.next();
				Map<String, String> item = new HashMap<String, String>();
				item.put("s", qs.getSubject().toString());
				item.put("p", qs.getPredicate().toString());
				item.put("o", qs.getObject().toString());
				item.put("subject", qs.getSubject().toString());
				item.put("predicate", qs.getPredicate().toString());
				item.put("object", qs.getObject().toString());
				return item;
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
		return o;
	}

	public Iterator<Map<String, String>> call() throws Exception {
		return items;
	}

	public Items items() {
		return this;
	}
}
