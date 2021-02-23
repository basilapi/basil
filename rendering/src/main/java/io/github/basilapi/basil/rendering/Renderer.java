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

import java.io.InputStream;
import java.util.Map;

import javax.ws.rs.core.MediaType;

public abstract class Renderer<T> {
	private T input;

	public Renderer(T input) {
		this.input = input;
	}
	
	protected T getInput(){
		return this.input;
	}

	public abstract InputStream stream(MediaType type, String g, Map<String, String> pref) throws CannotRenderException;

	public abstract String render(MediaType type, String g, Map<String, String> pref) throws CannotRenderException;
}
