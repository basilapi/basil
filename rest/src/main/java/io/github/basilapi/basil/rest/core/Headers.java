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

package io.github.basilapi.basil.rest.core;

public final class Headers {
	public static final String PREFIX = "X-Basil-";
	public static final String Error = PREFIX + "Error";
	public static final String Endpoint = PREFIX + "Endpoint";
	public static final String Api = PREFIX + "Api";
	public static final String Direct = PREFIX + "Direct";
	public static final String Spec = PREFIX + "Spec";
	public static final String View = PREFIX + "View";
	public static final String Store = PREFIX + "Store";
	public static final String Type = PREFIX + "Type";
	public static final String Docs = PREFIX + "Docs";
	public static final String Name = PREFIX + "Name";
	public static final String Swagger = PREFIX + "Swagger";
	public static final String Creator = PREFIX + "Creator";
	public static final String Alias = PREFIX + "Alias";;

	private Headers() {
	}

	public static String getHeader(String parameter) {
		return PREFIX + parameter.substring(0, 1).toUpperCase()
				+ parameter.substring(1).toLowerCase();
	}

	public static String asParameter(String Header) {
		return Header.substring(PREFIX.length()).toLowerCase();
	}
}
