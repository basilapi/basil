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

package io.github.basilapi.basil.search;

import java.util.Arrays;
import java.util.List;

public class SimpleQuery implements Query {

	private String text = "";
	private String endpoint = null;
	private List<String> nss = null;
	private List<String> rss = null;

	@Override
	public void setText(String text) {
		this.text = text;
	}

	@Override
	public String getText() {
		return this.text;
	}

	@Override
	public String getEndpoint() {
		return endpoint;
	}

	@Override
	public String[] getNamespaces() {
		if (nss != null) {
			return nss.toArray(new String[nss.size()]);
		}
		return new String[] {};
	}

	@Override
	public String[] getResources() {
		if (rss != null) {
			return rss.toArray(new String[rss.size()]);
		}
		return new String[] {};
	}

	@Override
	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	@Override
	public void setNamespaces(String... nss) {
		this.nss = Arrays.asList(nss);
	}

	@Override
	public void setResources(String... rss) {
		this.rss = Arrays.asList(rss);
	}
}
