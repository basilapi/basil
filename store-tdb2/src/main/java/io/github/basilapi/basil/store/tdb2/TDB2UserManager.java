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

import io.github.basilapi.basil.core.auth.User;
import io.github.basilapi.basil.core.auth.UserManager;
import io.github.basilapi.basil.core.auth.exceptions.UserApiMappingException;
import io.github.basilapi.basil.core.auth.exceptions.UserCreationException;

import java.util.Set;

public class TDB2UserManager implements UserManager {
    @Override
    public void createUser(User user) throws UserCreationException {

    }

    @Override
    public void mapUserApi(String username, String apiId) throws UserApiMappingException {

    }

    @Override
    public User getUser(String username) {
        return null;
    }

    @Override
    public Set<String> getUserApis(String username) {
        return null;
    }

    @Override
    public void deleteUserApiMap(String id) throws UserApiMappingException {

    }

    @Override
    public String getCreatorOfApi(String id) {
        return null;
    }
}
