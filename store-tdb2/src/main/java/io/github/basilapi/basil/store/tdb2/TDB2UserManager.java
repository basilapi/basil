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
import io.github.basilapi.basil.rdf.BasilOntology;
import io.github.basilapi.basil.rdf.RDFFactory;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.tdb2.TDB2Factory;

import java.util.Iterator;
import java.util.Set;

public class TDB2UserManager implements UserManager {
    private final RDFFactory toRDF;

    private String location;

    private Dataset dataset;
    public TDB2UserManager(String tdb2location, RDFFactory RDFFactory){
        this.toRDF = RDFFactory;
        this.location = tdb2location;
        this.dataset = TDB2Factory.connectDataset(tdb2location);
    }

    public RDFFactory getToRDF(){
        return toRDF;
    }
    
    @Override
    public void createUser(User user) throws UserCreationException {
        Graph g = toRDF.toGraph(user);
        // A named graph foreach user
        dataset.begin(ReadWrite.WRITE);
        dataset.asDatasetGraph().addGraph(toRDF.user(user.getUsername()), g);
        dataset.commit();
    }

    @Override
    public void mapUserApi(String username, String apiId) throws UserApiMappingException {
        dataset.begin(ReadWrite.WRITE);
        dataset.asDatasetGraph().getGraph(toRDF.user(username)).add(
                new Triple(toRDF.user(username), BasilOntology.Term.api.node(), toRDF.api(apiId))
        );
        dataset.commit();
    }

    @Override
    public User getUser(String username) {
        dataset.begin(ReadWrite.READ);
        Graph g = dataset.asDatasetGraph().getGraph(toRDF.user(username));
        User u = toRDF.makeUser(g);
        dataset.end();;
        return u;
    }

    @Override
    public Set<String> getUserApis(String username) {
        dataset.begin(ReadWrite.READ);
        Graph g = dataset.asDatasetGraph().getGraph(toRDF.user(username));
        Set<String> u = toRDF.makeUserApis(g);
        dataset.end();;
        return u;
    }

    @Override
    public void deleteUserApiMap(String id) throws UserApiMappingException {
        Node user = toRDF.user(getCreatorOfApi(id));
        dataset.begin(ReadWrite.WRITE);
        dataset.asDatasetGraph().getGraph(user).remove(user, BasilOntology.Term.api.node(), toRDF.api(id));
        dataset.commit();
    }

    @Override
    public String getCreatorOfApi(String id) {
        dataset.begin(ReadWrite.READ);
        Iterator<Quad> it = dataset.asDatasetGraph().find(null, null, BasilOntology.Term.api.node(), toRDF.api(id) );
        String creator = null;
        if(it.hasNext()){
            creator = it.next().asTriple().getSubject().getLocalName();
        }
        dataset.end();
        return creator;
    }
}
