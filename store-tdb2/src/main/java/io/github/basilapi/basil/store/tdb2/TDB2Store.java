/*
 * Copyright (c) 2022. Enrico Daga and Luca Panziera
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

package io.github.basilapi.basil.store.tdb2;

import io.github.basilapi.basil.core.ApiInfo;
import io.github.basilapi.basil.doc.Doc;
import io.github.basilapi.basil.search.Query;
import io.github.basilapi.basil.search.Result;
import io.github.basilapi.basil.search.SearchProvider;
import io.github.basilapi.basil.sparql.Specification;
import io.github.basilapi.basil.store.Store;
import io.github.basilapi.basil.view.Views;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class TDB2Store implements Store, SearchProvider {
    @Override
    public Collection<Result> contextSearch(Query query) throws IOException {
        return null;
    }

    @Override
    public Collection<String> search(Query query) throws IOException {
        return null;
    }

    @Override
    public void saveSpec(String id, Specification spec) throws IOException {

    }

    @Override
    public Specification loadSpec(String id) throws IOException {
        return null;
    }

    @Override
    public boolean existsSpec(String id) {
        return false;
    }

    @Override
    public List<String> listSpecs() throws IOException {
        return null;
    }

    @Override
    public List<ApiInfo> list() throws IOException {
        return null;
    }

    @Override
    public Views loadViews(String id) throws IOException {
        return null;
    }

    @Override
    public Doc loadDoc(String id) throws IOException {
        return null;
    }

    @Override
    public void saveViews(String id, Views views) throws IOException {

    }

    @Override
    public void saveDoc(String id, Doc doc) throws IOException {

    }

    @Override
    public boolean deleteDoc(String id) throws IOException {
        return false;
    }

    @Override
    public boolean deleteSpec(String id) throws IOException {
        return false;
    }

    @Override
    public Date created(String id) throws IOException {
        return null;
    }

    @Override
    public Date modified(String id) throws IOException {
        return null;
    }

    @Override
    public ApiInfo info(String id) throws IOException {
        return null;
    }

    @Override
    public void saveAlias(String id, Set<String> alias) throws IOException {

    }

    @Override
    public Set<String> loadAlias(String id) throws IOException {
        return null;
    }

    @Override
    public String getIdByAlias(String alias) throws IOException {
        return null;
    }

    @Override
    public String[] credentials(String id) throws IOException {
        return new String[0];
    }

    @Override
    public void saveCredentials(String id, String user, String password) throws IOException {

    }

    @Override
    public void deleteCredentials(String id) throws IOException {

    }
}
