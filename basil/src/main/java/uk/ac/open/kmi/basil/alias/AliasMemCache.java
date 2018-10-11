package uk.ac.open.kmi.basil.alias;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AliasMemCache implements AliasCache {
	private Map<String, String> cache;
	private int limit;
	private static Logger log = LoggerFactory.getLogger(AliasMemCache.class);
	public AliasMemCache() {
		this(1000);
	}

	public AliasMemCache(int size) {
		this.limit = size;
		this.cache = new LinkedHashMap<String, String>(10, (float) 0.75, false) {
			private static final long serialVersionUID = 1L;

			@Override
			protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
				if(log.isDebugEnabled() && size() > limit) {
					log.debug("removing older cache entry");
					return true;
				}
				return size() > limit;
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.open.kmi.basil.alias.AliasCache#set(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public void set(String id, String alias) {
		cache.put(alias, id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.open.kmi.basil.alias.AliasCache#containsAlias(java.lang.String)
	 */
	@Override
	public boolean containsAlias(String alias) {
		return cache.containsKey(alias);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.open.kmi.basil.alias.AliasCache#getId(java.lang.String)
	 */
	@Override
	public String getId(String alias) {
		return cache.get(alias);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.open.kmi.basil.alias.AliasCache#removeAll(java.lang.String)
	 */
	@Override
	public void removeAll(String id) {
		for (Entry<String, String> entry : cache.entrySet()) {
			if (entry.getValue().equals(id)) {
				cache.remove(entry.getKey());
			}
		}
	}
}
