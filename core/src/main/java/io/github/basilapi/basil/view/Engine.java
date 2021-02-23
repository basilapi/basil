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

import java.io.StringReader;
import java.io.Writer;

import io.github.basilapi.basil.view.rhino.RhinoMediator;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

public enum Engine {
	MUSTACHE("template/mustache"), RHINO("application/javascript");
	
	private String contentType;
	
	Engine(String contentType) {
		this.contentType = contentType;
	}

	public String getContentType() {
		return contentType;
	}

	public void exec(Writer writer, String template, Items data)
			throws EngineExecutionException {
		if (this.equals(MUSTACHE)) {
			MustacheFactory mf = new DefaultMustacheFactory();
			Mustache mustache = mf
					.compile(new StringReader(template), template);
			mustache.execute(writer, data);
		} else if (this.equals(RHINO)) {
			final Context cx = Context.enter();
			try {
				final ScriptableObject scope = cx.initStandardObjects();
				//ScriptableObject.defineClass(scope, RhinoMediator.class, false, true);
				cx.evaluateString(scope, "var rhinofunc = " + template,
						template, 1, null);
				Object fObj = scope.get("rhinofunc", scope);
				if (fObj == Scriptable.NOT_FOUND || !(fObj instanceof Function)) {
					throw new EngineExecutionException(
							"Broken input: script should declare a single anonym function. "
							+ "Eg: "
							+ "function(){ "
							+ " var items = this.items;"
							+ "	while(items.hasNext()){ "
							+ "		var i = items.next(); "
							+ "		this.print(i); "
							+ "	} "
							+ "");
				} else {
					Function f = (Function) fObj;
					RhinoMediator sb = new RhinoMediator();
					sb.init(writer);
					sb.bindItems(data);
					f.call(cx, scope, sb, new Object[] { sb });
				}
			} catch(EngineExecutionException e){
				throw e;
			} catch(Exception e){
				throw new EngineExecutionException(e);
			} finally {
				Context.exit();
			}
		}
	}

	public static Engine byContentType(String contentType) {
		if (MUSTACHE.contentType.equals(contentType)) {
			return MUSTACHE;
		} else if (RHINO.contentType.equals(contentType)) {
			return RHINO;
		}
		return null;
	}
}
