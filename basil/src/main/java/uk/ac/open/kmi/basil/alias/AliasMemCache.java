package uk.ac.open.kmi.basil.alias;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class AliasMemCache {
	private Map<String, String> cache = new HashMap<String, String>();

	public void set(String id, String alias) {
		cache.put(alias, id);
	}

	public boolean containsAlias(String alias) {
		return cache.containsKey(alias);
	}

	public String getId(String alias) {
		return cache.get(alias);
	}

	public void removeAll(String id) {
		for (Entry<String, String> entry : cache.entrySet()) {
			if (entry.getValue().equals(id)) {
				cache.remove(entry.getKey());
			}
		}
	}
}
