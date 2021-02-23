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

package io.github.basilapi.basil.alias;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
				if (log.isDebugEnabled() && size() > limit) {
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
	 * @see AliasCache#set(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public void set(String id, String alias) {
		cache.put(alias, id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see AliasCache#containsAlias(java.lang.String)
	 */
	@Override
	public boolean containsAlias(String alias) {
		return cache.containsKey(alias);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see AliasCache#getId(java.lang.String)
	 */
	@Override
	public String getId(String alias) {
		return cache.get(alias);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see AliasCache#removeAll(java.lang.String)
	 */
	@Override
	public void removeAll(String id) {
		Set<String> remove = new HashSet<String>();
		for (Entry<String, String> entry : cache.entrySet()) {
			if (entry.getValue().equals(id)) {
				remove.add(entry.getKey());
			}
		}
		for (String key : remove)
			cache.remove(key);
	}
}
