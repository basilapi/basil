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

import java.io.IOException;

import javax.ws.rs.core.MediaType;

/**
 * No available rendering for kind of object and mime type
 * 
 * @author enridaga
 *
 */
public class CannotRenderException extends Exception {

	public CannotRenderException() {
		super("Cannot render.");
	}

	public CannotRenderException(IOException e) {
		super(e);
	}

	public CannotRenderException(MediaType type) {
		super("Cannot render " + type.toString());
	}
	
	public CannotRenderException(Object o, MediaType type) {
		super("Cannot render " + o.getClass() + " with type " + type.toString());
	}

	public CannotRenderException(String string) {
		super(string);
	}

	public CannotRenderException(String string, Throwable e) {
		super(string, e);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
