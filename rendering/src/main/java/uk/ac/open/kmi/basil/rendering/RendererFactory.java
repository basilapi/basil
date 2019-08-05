package uk.ac.open.kmi.basil.rendering;

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
