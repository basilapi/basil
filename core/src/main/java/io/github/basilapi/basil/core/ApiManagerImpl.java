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

package io.github.basilapi.basil.core;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.ws.rs.core.MultivaluedMap;

import io.github.basilapi.basil.core.auth.UserManager;
import io.github.basilapi.basil.core.auth.exceptions.UserApiMappingException;
import io.github.basilapi.basil.core.exceptions.ApiInvocationException;
import io.github.basilapi.basil.core.exceptions.SpecificationParsingException;
import org.apache.jena.atlas.web.auth.HttpAuthenticator;
import org.apache.jena.atlas.web.auth.SimpleAuthenticator;
import org.apache.jena.query.ARQ;
import org.apache.jena.query.Query;
import org.apache.jena.update.UpdateRequest;

import io.github.basilapi.basil.doc.Doc;
import io.github.basilapi.basil.invoke.DirectExecutor;
import io.github.basilapi.basil.invoke.QueryExecutor;
import io.github.basilapi.basil.sparql.QueryParameter;
import io.github.basilapi.basil.sparql.Specification;
import io.github.basilapi.basil.sparql.SpecificationFactory;
import io.github.basilapi.basil.sparql.VariablesBinder;
import io.github.basilapi.basil.store.Store;
import io.github.basilapi.basil.view.Engine;
import io.github.basilapi.basil.view.View;
import io.github.basilapi.basil.view.Views;

/**
 * Created by Luca Panziera on 15/06/15.
 */
public class ApiManagerImpl implements ApiManager {
	private Store data;
	private UserManager userManager;
	private QueryExecutor executor;

	public ApiManagerImpl(Store store, UserManager um) {
		this(store, um, new DirectExecutor());
	}

	public ApiManagerImpl(Store store, UserManager um, QueryExecutor exe) {
		data = store;
		userManager = um;
		executor = exe;
		// Force initialization of Jena/ARQ
		ARQ.init();
	}

	/**
	 * Method to generate API Ids.
	 * <p/>
	 * XXX Not sure this is good, but it's an option - enridaga
	 *
	 * @return
	 * @see https://gist.github.com/LeeSanghoon/5811136
	 */

	private static String shortUUID() {
		UUID uuid = UUID.randomUUID();
		long l = ByteBuffer.wrap(uuid.toString().getBytes()).getLong();
		return Long.toString(l, Character.MAX_RADIX);
	}

	/**
	 * TODO This method does not supports UPDATE
	 */
	public String redirectUrl(String id, MultivaluedMap<String, String> parameters)
			throws IOException, ApiInvocationException {
		if (!data.existsSpec(id)) {
			throw new ApiInvocationException("Specification not found");
		}
		Specification specification = data.loadSpec(id);
		Query q = (Query) rewrite(specification, parameters);
		return specification.getEndpoint() + "?query=" + URLEncoder.encode(q.toString(), "UTF-8");
	}

	/**
	 * 
	 * @param specification
	 * @param parameters
	 * @return Object - Castable to either {@link org.apache.jena.query.Query} or {@link org.apache.jena.update.UpdateRequest}
	 * @throws IOException
	 * @throws ApiInvocationException
	 * @see org.apache.jena.query.Query
	 * @see org.apache.jena.update.UpdateRequest
	 */
	private Object rewrite(Specification specification, MultivaluedMap<String, String> parameters)
			throws IOException, ApiInvocationException {
		VariablesBinder binder = new VariablesBinder(specification);

		List<String> missing = new ArrayList<String>();
		for (QueryParameter qp : specification.getParameters()) {
			if (parameters.containsKey(qp.getName())) {
				List<String> values = parameters.get(qp.getName());
				binder.bind(qp.getName(), values.get(0));
			} else if (!qp.isOptional()) {
				missing.add(qp.getName());
			}
		}

		if (!missing.isEmpty()) {
			StringBuilder ms = new StringBuilder();
			ms.append("Missing mandatory query parameters: ");
			for (String p : missing) {
				ms.append(p);
				ms.append("\t");
			}
			ms.append("\n");
			throw new ApiInvocationException(ms.toString());
		}
		if (specification.isUpdate()) {
			UpdateRequest r = binder.toUpdate();
			return r;
		} else {
			Query q = binder.toQuery();
			if (parameters.containsKey("limit")) {
				q.setLimit(Long.parseLong(parameters.getFirst("limit")));
			}
			if (parameters.containsKey("offset")) {
				q.setOffset(Long.parseLong(parameters.getFirst("offset")));
			}
			return q;
		}
	}

	public InvocationResult invokeApi(String id, MultivaluedMap<String, String> parameters)
			throws IOException, ApiInvocationException {
		Specification specification = data.loadSpec(id);
		
		HttpAuthenticator authenticator = null;
		String[] credentials = data.credentials(id);
		if(credentials != null) {
			authenticator = new SimpleAuthenticator(credentials[0], credentials[1].toCharArray());
		}

		if (!specification.isUpdate()) {
			Query q = (Query) rewrite(specification, parameters);
			return executor.execute(q, specification.getEndpoint(), authenticator);
		} else {
			UpdateRequest r = (UpdateRequest) rewrite(specification, parameters);
			return executor.execute(r, specification.getEndpoint(), authenticator);
		}
	}

	public String createSpecification(String username, String endpoint, String body)
			throws IOException, SpecificationParsingException, UserApiMappingException {
		String id = shortUUID();
		if (body.equals("")) {
			throw new SpecificationParsingException("Body cannot be empty");
		}
		Specification specification = SpecificationFactory.create(endpoint, body);
		data.saveSpec(id, specification);
		userManager.mapUserApi(username, id);
		return id;
	}

	public String cloneSpecification(String username, String id) throws IOException, UserApiMappingException {
		String newId = shortUUID();
		Specification specification = data.loadSpec(id);
		Doc doc = data.loadDoc(id);
		Views views = listViews(id);
		data.saveSpec(newId, specification);
		userManager.mapUserApi(username, newId);
		if (!doc.isEmpty()) {
			data.saveDoc(newId, doc);
		}
		if (views.numberOf() > 0) {
			data.saveViews(newId, views);
		}
		return newId;
	}

	public void replaceSpecification(String id, String body) throws IOException, SpecificationParsingException {
		Specification oldSpec = data.loadSpec(id);
		replaceSpecification(id, oldSpec.getEndpoint(), body);
	}

	public void replaceSpecification(String id, String endpoint, String body)
			throws IOException, SpecificationParsingException {
		if (body.equals("")) {
			throw new SpecificationParsingException("Body cannot be empty");
		}
		Specification specification;
		specification = SpecificationFactory.create(endpoint, body);
		data.saveSpec(id, specification);
	}

	public boolean deleteApi(String id) throws IOException, UserApiMappingException {
		if (data.deleteSpec(id)) {
			userManager.deleteUserApiMap(id);
			return true;
		}
		return false;
	}

	public List<String> listApis() throws IOException {
		return data.listSpecs();
	}

	public Specification getSpecification(String id) throws IOException {
		return data.loadSpec(id);
	}

	public Views listViews(String id) throws IOException {
		return data.loadViews(id);
	}

	public View getView(String id, String name) throws IOException {
		return data.loadViews(id).byName(name);
	}

	public void deleteView(String id, String name) throws IOException {
		Views views = data.loadViews(id);
		views.remove(data.loadViews(id).byName(name));
		data.saveViews(id, views);
	}

	public void createView(String id, String mimeType, String name, String template, Engine engine) throws IOException {
		Views views = data.loadViews(id);
		views.put(mimeType, name, template, engine);
		data.saveViews(id, views);
	}

	public Doc getDoc(String id) throws IOException {
		return data.loadDoc(id);
	}

	public boolean deleteDoc(String id) throws IOException {
		return data.deleteDoc(id);
	}

	public boolean existsSpec(String id) {
		return data.existsSpec(id);
	}

	public void createDoc(String id, String name, String body) throws IOException {
		Doc doc = getDoc(id);
		doc.set(Doc.Field.NAME, name);
		doc.set(Doc.Field.DESCRIPTION, body);
		data.saveDoc(id, doc);
	}

	public void replaceDoc(String id, String name, String body) throws IOException {
		createDoc(id, name, body);
	}

	public String getCreatorOfApi(String id) {
		return userManager.getCreatorOfApi(id);
	}

	@Override
	public ApiInfo getInfo(String api) throws IOException {
		return data.info(api);
	}

	@Override
	public void createAlias(String id, Set<String> alias) throws IOException {
		data.saveAlias(id, alias);
	}

	@Override
	public boolean deleteAlias(String id) throws IOException {
		data.saveAlias(id, Collections.emptySet());
		return true;
	}

	@Override
	public Set<String> getAlias(String id) throws IOException {
		return data.loadAlias(id);
	}

	@Override
	public String byAlias(String alias) throws IOException {
		return data.getIdByAlias(alias);
	}

	@Override
	public String[] getCredentials(String id) throws IOException {
		return data.credentials(id);
	}

	@Override
	public void deleteCredentials(String id) throws IOException {
		data.deleteCredentials(id);
	}

	@Override
	public void createCredentials(String id, String[] credentials) throws IOException {
		data.saveCredentials(id, credentials[0], credentials[1]);
	}
}
