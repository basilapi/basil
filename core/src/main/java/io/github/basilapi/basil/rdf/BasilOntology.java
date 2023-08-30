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

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.iri.IRI;
import org.apache.jena.iri.IRIFactory;

public class BasilOntology {
    public static String NS = "http://basilapi.github.io/ontology/ns#";

    public enum Term {
        // ApiInfo
        Api, id, name, created, modified, alias,
        endpoint, query, update,
        // User
        User, username, password, email,
        // Doc
        description,
        // View
        view, View, extension, mimeType, template, engine,
        // api
        api,
        expandedQuery;

        public String getIRIString(){
            return NS + name();
        }

        public IRI iri(){
            return IRIFactory.iriImplementation().construct(getIRIString());
        }

        public Node node(){
            return NodeFactory.createURI(getIRIString());
        }

    }
}
