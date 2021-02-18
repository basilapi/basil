package io.github.basilapi.basil.invoke;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.jena.riot.web.HttpResponseHandler;

public class UpdateHandler implements HttpResponseHandler {
	HttpResponse response = null;
	String baseIRI = null;

	@Override
	public void handle(String baseIRI, HttpResponse response) throws IOException {
		this.response = response;
		this.baseIRI = baseIRI;
	}

	public HttpResponse getResponse() {
		return response;
	}

	public String getBaseIRI() {
		return baseIRI;
	}
}
