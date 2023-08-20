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

package io.github.basilapi.basil.rdf;

import io.github.basilapi.basil.core.ApiInfo;
import io.github.basilapi.basil.core.auth.User;
import io.github.basilapi.basil.doc.Doc;
import io.github.basilapi.basil.sparql.Specification;
import io.github.basilapi.basil.view.View;
import io.github.basilapi.basil.view.Views;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.mem.GraphMem;
import io.github.basilapi.basil.rdf.BasilOntology.Term;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.RDF;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class RDFFactory {
    String dataNS;

    public RDFFactory(){
        this("http://basilapi.github.io/data/ns/");
    }

    public RDFFactory(String dataNamespace){
        this.dataNS = dataNamespace;
    }

    public String getDataNS(){
        return dataNS;
    }

    private void str(Graph graph, Node subject, Term t, String str){
        str(graph, subject, t.node(), str);
    }
    private void str(Graph graph, Node s, Node p, String str){
        graph.add(new Triple(
                s,
                p,
                NodeFactory.createLiteral(str)
        ));
    }
    private void datetime(Graph graph, Node subject, Term t, Date d){
        graph.add(new Triple(
                subject,
                t.node(),
                ResourceFactory.createTypedLiteral(d).asNode()
        ));
    }

    private void node(Graph graph, Node subject, Term t, Node n){
        graph.add(new Triple(
                subject,
                t.node(),
                n
        ));
    }
    private void t(Graph graph, Node n1, Term t, Node n2){
        graph.add(new Triple(
                n1,
                t.node(),
                n2
        ));
    }

//    public Graph toGraph(ApiInfo o) {
//        Graph g = new GraphMem();
//        g.add(new Triple(api(o.getId()), RDF.type.asNode(), Term.Api.node()));
//        str(g, api(o.getId()), Term.id, o.getId());
//        str(g, api(o.getId()), Term.name, o.getName());
//        datetime(g, api(o.getId()), Term.created, o.created());
//        datetime(g, api(o.getId()), Term.modified, o.modified());
//        for(String alias: o.alias()){
//            node(g, api(o.getId()), Term.alias, NodeFactory.createLiteral(alias));
//        }
//        return g;
//    }

//    public Graph toGraph(String id, Specification s){
//        Graph g = new GraphMem();
//        str(g, api(id), Term.endpoint, s.getEndpoint());
//        str(g, api(id), Term.query, s.getQuery());
//        return g;
//    }

//    public Graph toGraph(String userName, Set<String> apis){
//        Graph g = new GraphMem();
//        Node s = user( userName);
//        for(String api: apis){
//            Node a = api( api);
//            t(g, s, Term.api, a);
//        }
//        return g;
//    }

    public Graph toGraph(User o){
        Graph g = new GraphMem();
        Node s = user(o.getUsername());
        g.add(new Triple(s, RDF.type.asNode(), Term.User.node()));
        Node u = user(o.getUsername());
        str(g, u, Term.username, o.getUsername());
        str(g, u, Term.password, o.getPassword());
        str(g, u, Term.email, o.getEmail());
        return g;
    }

    public Node user(String username){
        return NodeFactory.createURI(dataNS + "user/" + username);
    }

    public Node api(String apiId){
        return NodeFactory.createURI(dataNS + "api/" + apiId);
    }

    public Graph toGraph(String id, Doc d){
        Graph g = new GraphMem();
        str(g, api(id), Term.name, d.get(Doc.Field.NAME));
        str(g, api(id), Term.description, d.get(Doc.Field.DESCRIPTION));
        return g;
    }

    public Graph toGraph(String apiId, Views views){
        Graph g = new GraphMem();
        Node s = api(apiId);
        for(String name: views.getNames()){
            Node view = NodeFactory.createBlankNode();
            node(g, s, Term.view, view);
            View data = views.byName(name);
            g.add(new Triple(view, Term.extension.node(), NodeFactory.createLiteral(data.getName())));
            g.add(new Triple(view, Term.engine.node(), NodeFactory.createLiteral(data.getEngine().name())));
            g.add(new Triple(view, Term.mimeType.node(), NodeFactory.createLiteral(data.getMimeType())));
            g.add(new Triple(view, Term.template.node(), NodeFactory.createLiteral(data.getTemplate())));
        }
        return g;
    }

    public User makeUser(Graph g){
        User user = new User();
        String username = g.find(null, Term.username.node(), null).next().getObject().getLiteral().getLexicalForm();
        String password = g.find(null, Term.password.node(), null).next().getObject().getLiteral().getLexicalForm();
        String email = g.find(null, Term.email.node(), null).next().getObject().getLiteral().getLexicalForm();
        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(email);
        return user;
    }

    public Set<String> makeUserApis(Graph g) {
        ExtendedIterator<Triple> it = g.find(null, Term.api.node(), null);
        Set<String> s = new HashSet<String>();
        while(it.hasNext()){
            s.add(it.next().getObject().getURI().substring((dataNS + "api/").length()));
        }
        return s;
    }
}
