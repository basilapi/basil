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
import org.apache.jena.vocabulary.RDF;

import java.util.Date;
import java.util.Set;

public class ToRDF {
    String dataNS;

    public ToRDF(){
        this("http://basilapi.github.io/data/ns/");
    }

    public ToRDF(String dataNamespace){
        this.dataNS = dataNamespace;
    }
    private void str(Graph graph, String localName, Term t, String str){
        str(graph, localName, t.node(), str);
    }
    private void str(Graph graph, String localName, Node p, String str){
        graph.add(new Triple(
                NodeFactory.createURI(dataNS + localName),
                p,
                NodeFactory.createLiteral(str)
        ));
    }
    private void datetime(Graph graph, String localName, Term t, Date d){
        graph.add(new Triple(
                NodeFactory.createURI(dataNS + localName),
                t.node(),
                ResourceFactory.createTypedLiteral(d).asNode()
        ));
    }

    private void node(Graph graph, String localName, Term t, Node n){
        graph.add(new Triple(
                NodeFactory.createURI(dataNS + localName),
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

    public Graph toGraph(ApiInfo o) {
        Graph g = new GraphMem();
        g.add(new Triple(NodeFactory.createURI(dataNS + "api/" + o.getId()), RDF.type.asNode(), Term.Api.node()));
        str(g, "api/" + o.getId(), Term.id, o.getId());
        str(g, "api/" + o.getId(), Term.name, o.getName());
        datetime(g, "api/" + o.getId(), Term.created, o.created());
        datetime(g, "api/" + o.getId(), Term.modified, o.modified());
        int c = 0;
        Node aliasContainer = NodeFactory.createBlankNode();
        node(g, "api/" + o.getId(), Term.alias, aliasContainer);
        for(String alias: o.alias()){
            c += 1;
            g.add(new Triple(aliasContainer, RDF.li(c).asNode(), NodeFactory.createLiteral(alias)));
        }
        return g;
    }

    public Graph toGraph(String id, Specification s){
        Graph g = new GraphMem();
        str(g, "api/" + id, Term.endpoint, s.getEndpoint());
        str(g, "api/" + id, Term.query, s.getQuery());
        return g;
    }

    public Graph toGraph(User o, Set<String> apis){
        Graph g = new GraphMem();
        Node s = NodeFactory.createURI(dataNS + "user/" + o.getUsername());
        g.add(new Triple(s, RDF.type.asNode(), Term.User.node()));
        str(g, o.getUsername(), Term.username, o.getUsername());
        str(g, o.getUsername(), Term.password, o.getPassword());
        str(g, o.getUsername(), Term.email, o.getEmail());
        for(String api: apis){
            Node a = NodeFactory.createURI(dataNS + "api/" + api);
            t(g, s, Term.api,a);
        }
        return g;
    }

    public Graph toGraph(String id, Doc d){
        Graph g = new GraphMem();
        str(g, id, Term.name, d.get(Doc.Field.NAME));
        str(g, id, Term.description, d.get(Doc.Field.DESCRIPTION));
        return g;
    }

    public Graph toGraph(String apiId, Views views){
        Graph g = new GraphMem();
        Node s = NodeFactory.createURI(dataNS + apiId);
        Node viewsContainer = NodeFactory.createBlankNode();
        node(g, apiId, Term.views, viewsContainer);
        int c = 0;
        for(String name: views.getNames()){
            c += 1;
            Node view = NodeFactory.createBlankNode();
            g.add(new Triple(viewsContainer, RDF.li(c).asNode(), view));
            View data = views.byName(name);
            g.add(new Triple(view, Term.extension.node(), NodeFactory.createLiteral(data.getName())));
            g.add(new Triple(view, Term.engine.node(), NodeFactory.createLiteral(data.getEngine().name())));
            g.add(new Triple(view, Term.mimeType.node(), NodeFactory.createLiteral(data.getMimeType())));
            g.add(new Triple(view, Term.template.node(), NodeFactory.createLiteral(data.getTemplate())));
        }
        return g;
    }
}
