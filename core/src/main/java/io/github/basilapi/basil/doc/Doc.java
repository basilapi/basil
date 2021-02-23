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

package io.github.basilapi.basil.doc;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author enridaga
 *
 */
public class Doc implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8091475762370792961L;

	public enum Field {
		NAME, DESCRIPTION
	}

	private Map<Field, String> doc = new HashMap<Field, String>();

	public void set(Field field, String value) {
		doc.put(field, value);
	}

	/**
	 * Returns an empty string if null
	 * 
	 * @param f the field
	 * @return the value of the field
	 */
	public String get(Field f) {
		String v = doc.get(f);
		return (v == null) ? "" : v;
	}

	public boolean isEmpty() {
		return (get(Field.NAME) == null || "".equals(get(Field.NAME)))
				&& (get(Field.DESCRIPTION) == null || "".equals(get(Field.DESCRIPTION)));
	}
}
