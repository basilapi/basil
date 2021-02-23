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

import org.apache.commons.lang3.builder.HashCodeBuilder;

public class View implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5880624222164431668L;

	private String mimeType;
	private String name;
	private String template;
	private Engine engine;

	public String getMimeType() {
		return mimeType;
	}

	void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public String getName() {
		return name;
	}

	void setExtension(String extension) {
		this.name = extension;
	}

	public String getTemplate() {
		return template;
	}

	void setTemplate(String template, Engine engine) {
		this.template = template;
		this.engine = engine;
	}

	public Engine getEngine() {
		return this.engine;
	}

	public boolean equals(Object o) {
		if (o instanceof View) {
			View f = (View) o;
			return f.engine.equals(this.engine)
					&& f.name.equals(this.name)
					&& f.mimeType.equals(this.mimeType)
					&& f.template.equals(this.template);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(engine).append(mimeType)
				.append(name).append(template).build();
	}
}
