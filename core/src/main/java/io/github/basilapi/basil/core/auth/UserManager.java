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

package io.github.basilapi.basil.core.auth;

import io.github.basilapi.basil.core.auth.exceptions.UserApiMappingException;
import io.github.basilapi.basil.core.auth.exceptions.UserCreationException;

import java.util.Set;

/**
 * Created by Luca Panziera on 26/06/15.
 */
public interface UserManager {
    void createUser(User user) throws UserCreationException;
    void mapUserApi(String username, String apiId) throws UserApiMappingException;

    User getUser(String username);

    Set<String> getUserApis(String username);

    void deleteUserApiMap(String id) throws UserApiMappingException;

    String getCreatorOfApi(String id);
}
