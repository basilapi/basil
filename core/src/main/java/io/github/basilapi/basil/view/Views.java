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

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class Views implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1537650181054324254L;

	private Map<String, View> views = new HashMap<String, View>();

	public void put(String mimeType, String name, String template, Engine engine) {
		View view = new View();
		view.setExtension(name);
		view.setMimeType(mimeType);
		view.setTemplate(template, engine);
		views.put(name, view);
	}

	public View byName(String name) {
		return views.get(name);
	}

	public boolean exists(String name) {
		return views.containsKey(name);
	}

	public Set<View> byMimeType(String mimeType) {
		Set<View> viewset = new HashSet<View>();
		for (Entry<String, View> e : views.entrySet()) {
			if (e.getValue().getMimeType().equals(mimeType)) {
				viewset.add(e.getValue());
			}
		}
		return Collections.unmodifiableSet(viewset);
	}

	public void remove(View view) {
		views.remove(view.getName());
	}

	public void remove(String name) {
		views.remove(name);
	}

	public boolean supportsMimeType(String mimeType) {
		for (Entry<String, View> e : views.entrySet()) {
			if (e.getValue().getMimeType().equals(mimeType)) {
				return true;
			}
		}
		return false;
	}

	public Set<String> getMimeTypes() {
		Set<String> mimeTypes = new HashSet<String>();
		for (Entry<String, View> e : views.entrySet()) {
			mimeTypes.add(e.getValue().getMimeType());
		}
		return Collections.unmodifiableSet(mimeTypes);
	}

	public Set<String> getNames() {
		return Collections.unmodifiableSet(views.keySet());
	}

	public int numberOf() {
		return views.size();
	}
}
