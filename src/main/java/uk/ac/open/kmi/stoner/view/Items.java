package uk.ac.open.kmi.stoner.view;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

public class Items implements Callable<Iterator<Map<String, Object>>> {
	private Iterator<Map<String, Object>> items;

	public static Items create(List<Map<String, Object>> items) {
		Items o = new Items();
		o.items = items.iterator();
		return o;
	}

	private Items() {
	}

	public static Items create(final ResultSet rs) {
		Items o = new Items();
		o.items = new Iterator<Map<String, Object>>() {
			public boolean hasNext() {
				return rs.hasNext();
			}

			public Map<String, Object> next() {
				QuerySolution qs = rs.next();
				Iterator<String> vars = qs.varNames();
				Map<String, Object> item = new HashMap<String, Object>();
				while (vars.hasNext()) {
					String var = vars.next();
					item.put(var, qs.get(var));
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

		o.items = new Iterator<Map<String, Object>>() {
			boolean hasNext = true;

			public boolean hasNext() {
				return hasNext;
			}

			public Map<String, Object> next() {
				hasNext = false;
				Map<String, Object> item = new HashMap<String, Object>();
				item.put("boolean", rs);
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
		o.items = new Iterator<Map<String, Object>>() {
			public boolean hasNext() {
				return triples.hasNext();
			}

			public Map<String, Object> next() {
				Triple qs = triples.next();
				Map<String, Object> item = new HashMap<String, Object>();
				item.put("s", qs.getSubject());
				item.put("p", qs.getPredicate());
				item.put("o", qs.getObject());
				item.put("subject", qs.getSubject());
				item.put("predicate", qs.getPredicate());
				item.put("object", qs.getObject());
				return item;
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
		return o;
	}

	public Iterator<Map<String, Object>> call() throws Exception {
		return items;
	}

	public Items items() {
		return this;
	}
}
