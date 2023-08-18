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
import io.github.basilapi.basil.core.auth.exceptions.UserApiMappingException;
import io.github.basilapi.basil.core.auth.exceptions.UserCreationException;
import io.github.basilapi.basil.rdf.RDFFactory;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

public class TDB2UserManagerTest {
    private static String location = TDB2UserManagerTest.class.getClassLoader().getResource(".").getPath() + "/tdb2-user";

    private TDB2UserManager X;

    @Before
    public void before(){

        if(new File(location).exists()){
            new File(location).delete();
        }
        new File(location).mkdirs();

        X = new TDB2UserManager(location, new RDFFactory("http://www.example.org/"));
    }

    @AfterClass
    public static void afterClass(){
        new File(location).delete();
    }

    @Test
    public void testCreateUser() throws UserCreationException {
        User user = new User();
        String username = "username";
        String password = "fh34fh34hf934f98134f";
        String email = "example@example.com";
        user.setEmail(email);
        user.setUsername(username);
        user.setPassword(password);
        X.createUser(user);
        User user1 = X.getUser(username);
        Assert.assertEquals(user.getEmail(), user1.getEmail());
        Assert.assertEquals(user.getUsername(), user1.getUsername());
        Assert.assertEquals(user.getPassword(), user1.getPassword());
    }

    @Test
    public void testMapApi() throws UserApiMappingException {
        String username = "pluto";
        X.mapUserApi(username, "api1");
        Assert.assertTrue(X.getUserApis(username).size() == 1);
        Assert.assertTrue(X.getUserApis(username).contains("api1"));
    }


    @Test
    public void testMapApis() throws UserApiMappingException {
        String username = "pippo";
        X.mapUserApi(username, "api1");
        X.mapUserApi(username, "api2");
        X.mapUserApi(username, "api3");
        X.mapUserApi(username, "api4");
        Assert.assertTrue(X.getUserApis(username).size() == 4);
        Assert.assertTrue(X.getUserApis(username).contains("api1"));
        Assert.assertTrue(X.getUserApis(username).contains("api2"));
        Assert.assertTrue(X.getUserApis(username).contains("api3"));
        Assert.assertTrue(X.getUserApis(username).contains("api4"));
    }

    @Test
    public void testGetCreatorOfApi() throws UserApiMappingException {
        String username1 = "pippo1";
        String username2 = "pippo2";
        String username3 = "pippo3";
        X.mapUserApi(username1, "api1");
        X.mapUserApi(username1, "api2");
        X.mapUserApi(username2, "api3");
        X.mapUserApi(username3, "api4");
        Assert.assertTrue(X.getCreatorOfApi("api1").equals(username1));
        Assert.assertTrue(X.getCreatorOfApi("api2").equals(username1));
        Assert.assertTrue(X.getCreatorOfApi("api3").equals(username2));
        Assert.assertTrue(X.getCreatorOfApi("api4").equals(username3));
    }


    @Test
    public void testDeleteApiMap() throws UserApiMappingException {
        String username1 = "pippo1";
        String username2 = "pippo2";
        String username3 = "pippo3";
        X.mapUserApi(username1, "api1");
        X.mapUserApi(username1, "api2");
        X.mapUserApi(username2, "api3");
        X.mapUserApi(username3, "api4");
        Assert.assertTrue(X.getCreatorOfApi("api1").equals(username1));
        Assert.assertTrue(X.getCreatorOfApi("api2").equals(username1));
        Assert.assertTrue(X.getCreatorOfApi("api3").equals(username2));
        Assert.assertTrue(X.getCreatorOfApi("api4").equals(username3));
        //
        X.deleteUserApiMap("api1");
        Assert.assertTrue(X.getCreatorOfApi("api1") == null);
    }
}
