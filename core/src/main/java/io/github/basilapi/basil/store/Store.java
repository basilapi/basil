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

package io.github.basilapi.basil.store;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Set;

import io.github.basilapi.basil.core.ApiInfo;
import io.github.basilapi.basil.doc.Doc;
import io.github.basilapi.basil.sparql.Specification;
import io.github.basilapi.basil.view.Views;

public interface Store {

	void saveSpec(String id, Specification spec) throws IOException;

	Specification loadSpec(String id) throws IOException;

	boolean existsSpec(String id);

	List<String> listSpecs() throws IOException;

	List<ApiInfo> list() throws IOException;

	Views loadViews(String id) throws IOException;

	Doc loadDoc(String id) throws IOException;

	void saveViews(String id, Views views) throws IOException;

	void saveDoc(String id, Doc doc) throws IOException;

	boolean deleteDoc(String id) throws IOException;

	boolean deleteSpec(String id) throws IOException;

	Date created(String id) throws IOException;

	Date modified(String id) throws IOException;

	ApiInfo info(String id) throws IOException;
	
	/**
	 * @since 0.5.0
	 * @param id
	 * @param alias
	 */
	void saveAlias(String id, Set<String> alias) throws IOException;

	/**
	 * @since 0.5.0
	 * @param id
	 * @return
	 */
	Set<String> loadAlias(String id) throws IOException;

	/**
	 * @since 0.5.0
	 * @param id
	 * @return
	 */
	String getIdByAlias(String alias) throws IOException;
	
	/**
	 * @param id
	 * @return - null if no credentials
	 * @since 0.6.0
	 * @throws IOException
	 */
	String[] credentials(String id) throws IOException;
	
	/**
	 * @param id
	 * @param user
	 * @param password
	 * @since 0.6.0
	 * @throws IOException
	 */
	void saveCredentials(String id, String user, String password) throws IOException;
	

	/**
	 * @param id
	 * @since 0.6.0
	 * @throws IOException
	 */
	void deleteCredentials(String id) throws IOException;

}
