package uk.ac.open.kmi.basil;

import uk.ac.open.kmi.basil.store.Store;

import javax.servlet.ServletContext;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.regex.Pattern;

public class AbstractResource {

	@Context
	protected HttpHeaders requestHeaders;

	@Context
	protected UriInfo requestUri;

	@Context
	protected ServletContext context;

	protected final ResponseBuilder addHeaders(ResponseBuilder builder,
			String id) {
		URI api = requestUri.getBaseUriBuilder().path(id).path("api").build();
		URI spec = requestUri.getBaseUriBuilder().path(id).path("spec").build();
//		URI store = requestUri.getBaseUriBuilder().path(id).path("store")
//				.build();
		URI views = requestUri.getBaseUriBuilder().path(id).path("view")
				.build();
		URI swagger = requestUri.getBaseUriBuilder().path(id).path("api-docs")
				.build();
		URI docs = requestUri.getBaseUriBuilder().path(id).path("docs")
				.build();
		builder.header(Headers.Api, api);
		builder.header(Headers.Spec, spec);
		// builder.header(Headers.Store, store); XXX Not implemented Yet
		builder.header(Headers.View, views);
		builder.header(Headers.Docs, docs);
		builder.header(Headers.Swagger, swagger);
		return builder;
	}

	protected final String getParameterOrHeader(String parameter,
			boolean mandatory) {
		String value = requestUri.getQueryParameters().getFirst(parameter);
		if (value == null) {
			if (requestHeaders.getHeaderString(Headers.getHeader(parameter)) == null) {
				throw new WebApplicationException(Response
						.status(HttpURLConnection.HTTP_BAD_REQUEST)
						.header(Headers.Error,
								Headers.getHeader(parameter)
										+ " (or query parameter '" + parameter
										+ "') missing.").build());
			} else {
				value = requestHeaders.getHeaderString(Headers
						.getHeader(parameter));
			}
		}
		return value;
	}

	protected Store getDataStore() {
		return (Store) context.getAttribute(BasilApplication.Registry.Store);
	}

	protected boolean isValidId(String id) {
		return Pattern.matches("([^/]+)", id);
	}

	protected boolean isValidName(String name) {
		return Pattern.matches("([^/]+)", name);
	}

	protected boolean isValidExtension(String extentsion) {
		return Pattern.matches("(\\.[\\-a-zA-Z0-9]+)?", extentsion);
	}
}
