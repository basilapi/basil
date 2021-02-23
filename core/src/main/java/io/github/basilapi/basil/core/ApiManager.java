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
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.MultivaluedMap;

import io.github.basilapi.basil.core.auth.exceptions.UserApiMappingException;
import io.github.basilapi.basil.core.exceptions.ApiInvocationException;
import io.github.basilapi.basil.core.exceptions.SpecificationParsingException;
import io.github.basilapi.basil.doc.Doc;
import io.github.basilapi.basil.sparql.Specification;
import io.github.basilapi.basil.view.Engine;
import io.github.basilapi.basil.view.View;
import io.github.basilapi.basil.view.Views;

/**
 * Created by Luca Panziera on 15/06/15.
 */
public interface ApiManager {
	String redirectUrl(String id, MultivaluedMap<String, String> parameters) throws IOException, ApiInvocationException;
	
    InvocationResult invokeApi(String id, MultivaluedMap<String, String> parameters) throws IOException, ApiInvocationException;

    String createSpecification(String username, String endpoint, String body) throws SpecificationParsingException, UserApiMappingException, IOException;

    String cloneSpecification(String username, String id) throws IOException, UserApiMappingException;

    void replaceSpecification(String id, String endpoint, String body) throws IOException, SpecificationParsingException;
    
    void replaceSpecification(String id, String body) throws IOException, SpecificationParsingException;

    boolean deleteApi(String id) throws IOException, UserApiMappingException;

    List<String> listApis() throws IOException;

    Specification getSpecification(String id) throws IOException;

    Views listViews(String id) throws IOException;

    View getView(String id, String name) throws IOException;

    void deleteView(String id, String name) throws IOException;

    void createView(String id, String mimeType, String name, String template, Engine engine) throws IOException;

    Doc getDoc(String id) throws IOException;

    boolean deleteDoc(String id) throws IOException;

    boolean existsSpec(String id);

    void createDoc(String id, String name, String body) throws IOException;

    void replaceDoc(String id, String name, String body) throws IOException;

    String getCreatorOfApi(String id) throws IOException;

	ApiInfo getInfo(String api) throws IOException;
	
	/**
	 * 
	 * @param id
	 * @param name
	 * @param body
	 * @since 0.5.0
	 * @throws IOException
	 */
	void createAlias(String id, Set<String> alias) throws IOException;
	
	/**
	 * 
	 * @param id
	 * @return
	 * @since 0.5.0
	 * @throws IOException
	 */
	boolean deleteAlias(String id) throws IOException;
	
	/**
	 * 
	 * @param id
	 * @return
	 * @since 0.5.0
	 * @throws IOException - If no alias is set
	 */
	Set<String> getAlias(String id) throws IOException;

	/**
	 * 
	 * @param alias
	 * @return
	 * @since 0.5.0
	 * @throws IOException - If id does not exist for that alias
	 */
	String byAlias(String alias) throws IOException;

	/**
	 * 
	 * @param id
	 * @return
	 * @since 0.6.0
	 * @throws IOException
	 */
	String[] getCredentials(String id) throws IOException;

	/**
	 * 
	 * @param id
	 * @return
	 * @since 0.6.0
	 * @throws IOException
	 */
	void deleteCredentials(String id) throws IOException;

	/**
	 * 
	 * @param id
	 * @param credentials
	 * @since 0.6.0
	 * @throws IOException
	 */
	void createCredentials(String id, String[] credentials) throws IOException;
}
