package uk.ac.open.kmi.basil.view;

import java.io.StringReader;
import java.io.Writer;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import uk.ac.open.kmi.basil.view.rhino.RhinoMediator;

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
