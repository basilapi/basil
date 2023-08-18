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

import io.github.basilapi.basil.core.ApiInfo;
import io.github.basilapi.basil.rdf.RDFFactory;
import io.github.basilapi.basil.sparql.Specification;
import io.github.basilapi.basil.sparql.SpecificationFactory;
import io.github.basilapi.basil.sparql.UnknownQueryTypeException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.File;
import java.io.IOException;
import java.util.List;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TDB2StoreTest {
    private static String location = TDB2UserManagerTest.class.getClassLoader().getResource(".").getPath() + "/tdb2-user";

    private TDB2Store X;

    @Before
    public void before() {

        if (new File(location).exists()) {
            new File(location).delete();
        }
        new File(location).mkdirs();

        X = new TDB2Store(location, new RDFFactory("http://www.example.org/"));
    }

    @AfterClass
    public static void beforeClass() {
        new File(location).delete();
    }


    @AfterClass
    public static void afterClass() {
        new File(location).delete();
    }

    @Test
    public void testA_SaveSpec() throws UnknownQueryTypeException, IOException {
        Specification s = SpecificationFactory.create("http://www.example.org/sparql", "SELECT * WHERE { ?a ?b ?c}");
        String id = "test-spect-id";
        X.saveSpec(id, s);
        Specification s1 = X.loadSpec(id);
        Assert.assertTrue(s.equals(s1));
        Assert.assertEquals(1, X.listSpecs().size());
        Assert.assertEquals(1, X.list().size());
        Assert.assertTrue(X.existsSpec(id));
    }

    @Test
    public void testB_DeleteSpec() throws UnknownQueryTypeException, IOException {
        String id = "test-spect-id";
        boolean result = X.deleteSpec(id);
        Assert.assertTrue(result);
    }

    @Test
    public void testC_ListApiInfo() throws UnknownQueryTypeException, IOException {
        String id1 = "test-spec-id1";
        Specification s1 = SpecificationFactory.create("http://www.example.org/sparql", "SELECT ?a1 WHERE { ?a1 ?b1 ?c1 }");
        String id2 = "test-spec-id2";
        Specification s2 = SpecificationFactory.create("http://www.example.org/sparql", "SELECT ?a2 WHERE { ?a2 ?b2 ?c2 }");

        X.saveSpec(id1, s1);
        Specification s1_1 = X.loadSpec(id1);
        Assert.assertTrue(s1_1.getEndpoint().equals(s1.getEndpoint()));
        Assert.assertTrue(s1_1.getQuery().equals(s1.getQuery()));
        Assert.assertTrue(s1_1.equals(s1));

        X.saveSpec(id2, s2);
        Specification s2_2 = X.loadSpec(id2);
        Assert.assertTrue(s2_2.getEndpoint().equals(s2.getEndpoint()));
        System.err.println(s2_2.getQuery());
        System.err.println(s2.getQuery());
        Assert.assertTrue(s2_2.getQuery().equals(s2.getQuery()));
        Assert.assertTrue(s2_2.equals(s2));

        Assert.assertEquals(2, X.listSpecs().size());
        Assert.assertTrue(X.existsSpec(id1));
        Assert.assertTrue(X.existsSpec(id2));

        List<ApiInfo> list = X.list();
        Assert.assertEquals(2, list.size());
        ApiInfo first = list.get(0);
        ApiInfo second = list.get(1);
        ApiInfo info1;
        ApiInfo info2;
        if (first.getId().equals(id1)) {
            info1 = first;
            info2 = second;
        } else {
            info1 = second;
            info2 = first;
        }

        Assert.assertTrue(info1.getId().equals(id1));
        Assert.assertNull(info1.getName());

        Assert.assertTrue(info2.getId().equals(id2));
        Assert.assertNull(info2.getName());

        Assert.assertTrue(info1.created().before(info2.created()));
        Assert.assertTrue(info1.modified().before(info2.modified()));

    }

    @Test
    public void testD_GetApiInfo() throws UnknownQueryTypeException, IOException {
        // We assume there are still 2 specs
        Assert.assertEquals(2, X.listSpecs().size());

        // Let's get the second
        ApiInfo info = X.info("test-spec-id2");
        Assert.assertTrue(info.getId().equals("test-spec-id2"));
        Assert.assertNull(info.getName());
        Assert.assertNotNull(info.created());
        Assert.assertNotNull(info.modified());
    }
}