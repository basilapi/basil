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
import io.github.basilapi.basil.doc.Doc;
import io.github.basilapi.basil.rdf.RDFFactory;
import io.github.basilapi.basil.sparql.Specification;
import io.github.basilapi.basil.sparql.SpecificationFactory;
import io.github.basilapi.basil.sparql.UnknownQueryTypeException;
import org.apache.commons.io.FileUtils;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TDB2StoreTest {
    static final Logger L = LoggerFactory.getLogger(TDB2StoreTest.class);
    private static String location = TDB2UserManagerTest.class.getClassLoader().getResource(".").getPath() + "/tdb2-user";

    private static TDB2Store X;


    public static void setup() throws IOException {
        L.trace("Setup");
        File fsLocation = new File(location);
        if (fsLocation.exists()) {
            boolean isIt = fsLocation.delete();
            FileUtils.forceDelete(fsLocation);
        }
        File tdb2Loc = new File(location);
        tdb2Loc.mkdirs();
        X = new TDB2Store(location, new RDFFactory("http://www.example.org/"));
    }

    @BeforeClass
    public static void beforeClass() throws IOException {
        setup();
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

    @Test
    public void testE_Alias() throws UnknownQueryTypeException, IOException {
        //printAll();
        String id1 = "test-spec-id1";
        Set<String> alias = new HashSet<String>();
        String a = "my-alias-for-id1";
        alias.add(a);
        X.saveAlias(id1, alias);
        ApiInfo info = X.info(id1);
        Assert.assertTrue(info.alias().size() == 1);
        Assert.assertTrue(info.alias().iterator().next().equals(a));
    }

    @Test
    public void testF_MoreOnAlias() throws UnknownQueryTypeException, IOException {
        //printAll();
        String id2 = "test-spec-id2";
        Set<String> alias = new HashSet<String>();
        String a2 = "my-alias2";
        alias.add(a2);
        String a3 = "my-alias3";
        alias.add(a3);
        String a4 = "my-alias4";
        alias.add(a4);
        X.saveAlias(id2, alias);
        Set<String> aliasSet = X.loadAlias(id2);
        Assert.assertTrue(aliasSet.size() == 4);
        //Assert.assertTrue(info.alias().iterator().next().equals(a));
    }

    @Test
    public void testG_GetIdByAlias() throws UnknownQueryTypeException, IOException {
//        printAll();
        String id2 = "test-spec-id2";
        String a2 = "my-alias2";
        String a3 = "my-alias3";
        String a4 = "my-alias4";
        Assert.assertTrue(X.getIdByAlias(a2).equals(id2));
        Assert.assertTrue(X.getIdByAlias(a3).equals(id2));
        Assert.assertTrue(X.getIdByAlias(a4).equals(id2));
    }

    @Test
    public void testH_Credentials() throws UnknownQueryTypeException, IOException {
        String id = "test-spec-id1";
        String user = "my-username";
        String password = "my-password";
        Assert.assertTrue(X.credentials(id).length == 0);
        X.saveCredentials(id, user, password);
        Assert.assertTrue(X.credentials(id).length == 2);
        Assert.assertTrue(X.credentials(id) [0].equals(user));
        Assert.assertTrue(X.credentials(id) [1].equals(password));
    }

    @Test
    public void testI_MoreOnCredentials() throws UnknownQueryTypeException, IOException {

        String id = "test-spec-id1";
        X.deleteCredentials(id);
        Assert.assertTrue(X.credentials(id).length == 0);

        String user = "my-username2222";
        String password = "my-password222";
        X.saveCredentials(id, user, password);
        Assert.assertTrue(X.credentials(id).length == 2);
        Assert.assertTrue(X.credentials(id) [0].equals(user));
        Assert.assertTrue(X.credentials(id) [1].equals(password));
    }

    @Test
    public void testJ_Doc() throws UnknownQueryTypeException, IOException {

        String id = "test-spec-id1";
        String name = "My beatiful web api";
        String desc = "My beatiful web api description";
        Doc d = new Doc();
        d.set(Doc.Field.NAME, name);
        d.set(Doc.Field.DESCRIPTION, desc);
        Assert.assertTrue(X.loadDoc(id) == null);
        X.saveDoc(id, d);
        Doc d2 = X.loadDoc(id);
        Assert.assertEquals(name, d.get(Doc.Field.NAME));
        Assert.assertEquals(desc, d.get(Doc.Field.DESCRIPTION));
    }

    @Test
    public void testK_MoreOnDoc() throws UnknownQueryTypeException, IOException {

        String id = "test-spec-id1";
        String name = "My beatiful web api";
        String desc = "My beatiful web api description";

        Doc d2 = X.loadDoc(id);
        Doc d = new Doc();
        d.set(Doc.Field.NAME, name);
        d.set(Doc.Field.DESCRIPTION, desc);
        Assert.assertEquals(name, d.get(Doc.Field.NAME));
        Assert.assertEquals(desc, d.get(Doc.Field.DESCRIPTION));

        X.deleteDoc(id);

        Assert.assertTrue(X.loadDoc(id) == null);

        name ="Changed name";
        desc ="Changed desc";
        d.set(Doc.Field.NAME, name);
        d.set(Doc.Field.DESCRIPTION, desc);

        X.saveDoc(id, d);

        d2 = X.loadDoc(id);
        Assert.assertEquals(name, d.get(Doc.Field.NAME));
        Assert.assertEquals(desc, d.get(Doc.Field.DESCRIPTION));
    }

    @Test
    public void testL_Graph()  {
        String id = "test-spec-id1";
        Graph g = X.getAsMemGraph(id);
        Assert.assertEquals(11, g.size());
    }


    private void printAll(){
        X.dataset.begin();
        java.util.Iterator<Node> graphs = X.dataset.asDatasetGraph().listGraphNodes();
        while(graphs.hasNext()){
            Node ng = graphs.next();
            Graph g = X.dataset.asDatasetGraph().getGraph(ng);
            Iterator<Triple> trit = g.find();
            while(trit.hasNext()){
                System.err.println( ng.toString() + " -- " + trit.next());
            }
        }
        X.dataset.end();
    }
}