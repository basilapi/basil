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

import org.apache.http.HttpResponse;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;

public class RendererFactory {

	public static final Renderer<? extends Object> getRenderer(Object o) throws CannotRenderException {
		if (o instanceof Boolean) {
			return new BooleanRenderer((Boolean) o);
		} else if (o instanceof Model) {
			return new ModelRenderer((Model) o);
		} else if (o instanceof ResultSet) {
			return new ResultSetRenderer((ResultSet) o);
		} else if (o instanceof HttpResponse) {
			return new HttpResponseRenderer((HttpResponse) o);
		}
		throw new CannotRenderException();
	}
}
