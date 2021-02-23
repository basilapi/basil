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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;

import javax.servlet.ServletContext;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;

import io.github.basilapi.basil.alias.AliasCache;
import io.github.basilapi.basil.core.ApiManager;
import io.github.basilapi.basil.core.ApiManagerImpl;
import io.github.basilapi.basil.core.auth.UserManager;
import io.github.basilapi.basil.invoke.QueryExecutor;
import io.github.basilapi.basil.search.SearchProvider;
import io.github.basilapi.basil.store.Store;
import org.apache.shiro.subject.Subject;
import org.secnod.shiro.jaxrs.Auth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.basilapi.basil.server.BasilApplication;
import io.github.basilapi.basil.rest.auth.StatelessBasicAuth;
import io.github.basilapi.basil.rest.msg.ErrorMessage;

public class AbstractResource {

	private static Logger log = LoggerFactory.getLogger(AbstractResource.class);
	@Auth Subject subject;
	
	@Context
	protected HttpHeaders requestHeaders;

	@Context
	protected UriInfo requestUri;

	@Context
	protected ServletContext context;
	private ApiManager apiManager;
	private AliasCache aliasCache;
	
	protected ApiManager getApiManager() {
		if (apiManager == null) {
			apiManager = new ApiManagerImpl(getDataStore(), getUserManager(), getQueryExecutor());
		}
		return apiManager;
	}
	
	protected String getApiId(String idOrAlias) throws IOException {
		// Try alias in cache
		if(getAliasCache().containsAlias(idOrAlias)) {
			log.trace("alias from cache: {}", idOrAlias);
			return getAliasCache().getId(idOrAlias);
		}
		// Try id
	
		if(getApiManager().existsSpec(idOrAlias)) {
			log.trace("id is id: {}", idOrAlias);
			return idOrAlias;
		}else {
			// Try Alias
			String id = getApiManager().byAlias(idOrAlias);
			log.trace("id from alias: {}={}", idOrAlias, id);
			// Save in cache
			getAliasCache().set(id, idOrAlias);
			return id;
		}
	}
	
	protected boolean isAuthenticated(){
		return subject.isAuthenticated() || new StatelessBasicAuth().authenticate(requestHeaders);
	}
	
	protected SearchProvider getSearchProvider() {
		return (SearchProvider) context.getAttribute(BasilApplication.Registry.SearchProvider);
	}

	protected QueryExecutor getQueryExecutor() {
		return (QueryExecutor) context.getAttribute(BasilApplication.Registry.QueryExecutor);
	}

	protected AliasCache getAliasCache() {
		if (aliasCache == null) {
			// XXX That't the only implementation at the moment.
			aliasCache = (AliasCache) context.getAttribute(BasilApplication.Registry.AliasCache);
		}
		return aliasCache;
	}
	
	protected final ResponseBuilder packError(ResponseBuilder builder, String message){
		return builder.header(Headers.Error, message).entity(new ErrorMessage(message).asJSON());
	}
	
	protected final ResponseBuilder packError(ResponseBuilder builder, Exception e){
		return packError(builder, e.getMessage());
	}
	
	protected final ResponseBuilder addHeaders(ResponseBuilder builder,
			String id) {
		URI api = requestUri.getBaseUriBuilder().path(id).path("api").build();
		URI direct = requestUri.getBaseUriBuilder().path(id).path("direct").build();
		URI spec = requestUri.getBaseUriBuilder().path(id).path("spec").build();
		URI views = requestUri.getBaseUriBuilder().path(id).path("view")
				.build();
		URI swagger = requestUri.getBaseUriBuilder().path(id).path("api-docs")
				.build();
		URI docs = requestUri.getBaseUriBuilder().path(id).path("docs")
				.build();
		URI alias = requestUri.getBaseUriBuilder().path(id).path("alias")
				.build();
		builder.header(Headers.Api, api);
		builder.header(Headers.Alias, alias);
		builder.header(Headers.Spec, spec);
		builder.header(Headers.Direct, direct);
		builder.header(Headers.View, views);
		builder.header(Headers.Docs, docs);
		builder.header(Headers.Swagger, swagger);
		try {
			builder.header(Headers.Creator, getApiManager().getCreatorOfApi(id));
		} catch (IOException e) {
			log.error("",e);
		}
		return builder;
	}

	protected final String getParameterOrHeader(String parameter) {
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

	private Store getDataStore() {
		return (Store) context.getAttribute(BasilApplication.Registry.Store);
	}
	
	protected UserManager getUserManager() {
		return (UserManager) context.getAttribute(BasilApplication.Registry.UserManager);
	}
}
