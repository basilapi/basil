/*
 * Copyright (c) 2023. Enrico Daga and Luca Panziera
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

import io.github.basilapi.basil.rdf.RDFFactory;
import io.github.basilapi.basil.sparql.Specification;
import io.github.basilapi.basil.sparql.SpecificationFactory;
import io.github.basilapi.basil.sparql.UnknownQueryTypeException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class TDB2StoreTest {
    private static String location = TDB2UserManagerTest.class.getClassLoader().getResource(".").getPath() + "/tdb2-user";

    private TDB2Store X;

    @Before
    public void before(){

        if(new File(location).exists()){
            new File(location).delete();
        }
        new File(location).mkdirs();

        X = new TDB2Store(location, new RDFFactory("http://www.example.org/"));
    }

    @AfterClass
    public static void afterClass(){
        new File(location).delete();
    }

    @Test
    public void testSaveSpec() throws UnknownQueryTypeException, IOException {
        Specification s = SpecificationFactory.create("http://www.example.org/sparql", "SELECT * WHERE { ?a ?b ?c}");
        X.saveSpec("test-spect-id", s);
        Specification s1 = X.loadSpec("test-spect-id");
        Assert.assertTrue(s.equals(s1));
    }
}
