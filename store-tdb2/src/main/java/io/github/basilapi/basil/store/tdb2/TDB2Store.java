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

import io.github.basilapi.basil.core.ApiInfo;
import io.github.basilapi.basil.doc.Doc;
import io.github.basilapi.basil.rdf.BasilOntology;
import io.github.basilapi.basil.rdf.RDFFactory;
import io.github.basilapi.basil.search.Query;
import io.github.basilapi.basil.search.Result;
import io.github.basilapi.basil.search.SearchProvider;
import io.github.basilapi.basil.sparql.Specification;
import io.github.basilapi.basil.sparql.SpecificationFactory;
import io.github.basilapi.basil.sparql.UnknownQueryTypeException;
import io.github.basilapi.basil.store.Store;
import io.github.basilapi.basil.view.Views;
import org.apache.commons.collections4.ListUtils;
import org.apache.jena.atlas.lib.DateTimeUtils;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.datatypes.xsd.XSDDateTime;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.mem.GraphMem;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.tdb2.TDB2Factory;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class TDB2Store implements Store, SearchProvider {
    private final String location;
    final Dataset dataset;
    RDFFactory toRDF;

    public final static Logger L = LoggerFactory.getLogger(TDB2Store.class);

    private static final String selectApiInfo = "SELECT ?id ?name ?created ?modified " +
            "WHERE { GRAPH ?apiURI {" +
            "?apiURI <" + BasilOntology.Term.id.getIRIString() + "> ?id ; " +
            "<" + BasilOntology.Term.created.getIRIString() + "> ?created ; " +
            "<" + BasilOntology.Term.modified.getIRIString() + "> ?modified . " +
            "OPTIONAL { ?apiURI <" + BasilOntology.Term.name.getIRIString() + "> ?name } . " +
            "}}";

    public TDB2Store(String location, RDFFactory rdfFactory) {
        toRDF = rdfFactory;
        this.location = location;
        this.dataset = TDB2Factory.connectDataset(this.location);
    }

    private void exec(UpdateRequest... updates){
        L.debug("[begin write] {} update request", updates.length);
        dataset.begin(ReadWrite.WRITE);
        for(UpdateRequest update : updates) {
            L.trace("{}", update.toString());
            UpdateProcessor up = UpdateExecutionFactory.create(update, dataset);
            up.execute();
        }
        dataset.commit();
        L.debug("[commit] {} update request", updates.length);
//        dataset.begin(ReadWrite.READ);
//        L.error("{}", dataset.listNames().next());
//        dataset.end();
    }

    @Override
    public Collection<Result> contextSearch(Query query) throws IOException {
        return null;
    }

    @Override
    public Collection<String> search(Query query) throws IOException {
        return null;
    }

    @Override
    public void saveSpec(String id, Specification spec) throws IOException {
        boolean exists = existsSpec(id);
        // ApiInfo need to exist!
        Node apiURI = toRDF.api(id);
        L.debug("Save API spec {}", apiURI);
        String deleteStr = "DELETE { GRAPH ?apiURI { " +
                " ?apiURI <" + BasilOntology.Term.id.getIRIString() + "> ?id . " +
                " ?apiURI <" + BasilOntology.Term.endpoint.getIRIString() + "> ?endpoint . " +
                " ?apiURI <" + BasilOntology.Term.query.getIRIString() + "> ?queryText . " +
                " ?apiURI <" + BasilOntology.Term.modified.getIRIString() + "> ?modified . " +
                "}} WHERE { " +
                "GRAPH ?apiURI { " +
                " ?apiURI <" + BasilOntology.Term.id.getIRIString() + "> ?id . " +
                " ?apiURI <" + BasilOntology.Term.endpoint.getIRIString() + "> ?endpoint . " +
                " ?apiURI <" + BasilOntology.Term.query.getIRIString() + "> ?queryText . " +
                " ?apiURI <" + BasilOntology.Term.modified.getIRIString() + "> ?modified . " +
                " } }";
        ParameterizedSparqlString pqs = new ParameterizedSparqlString();
        pqs.setCommandText(deleteStr);
        pqs.setParam("apiURI", apiURI);
        UpdateRequest delete = pqs.asUpdate();
        L.trace("{}", delete.toString());
        //
        String insertStr = "INSERT DATA { GRAPH ?apiURI { " +
                " ?apiURI <" + BasilOntology.Term.id.getIRIString() + "> ?id . " +
                " ?apiURI <" + BasilOntology.Term.endpoint.getIRIString() + "> ?endpoint . " +
                " ?apiURI <" + BasilOntology.Term.query.getIRIString() + "> ?queryText . " +
                ((!exists) ? " ?apiURI <" + BasilOntology.Term.created.getIRIString() + "> ?created . " : "") +
                " ?apiURI <" + BasilOntology.Term.modified.getIRIString() + "> ?modified . " +
            "}} ";
        ParameterizedSparqlString pqs2 = new ParameterizedSparqlString();
        pqs2.setCommandText(insertStr);
        pqs2.setParam("apiURI", apiURI);
        pqs2.setLiteral("endpoint", spec.getEndpoint());
        pqs2.setLiteral("id", id);
        pqs2.setLiteral("queryText", spec.getQuery());
        if(!exists){
            pqs2.setLiteral("created", System.currentTimeMillis());
        }
        pqs2.setLiteral("modified", System.currentTimeMillis());
        UpdateRequest insert = pqs2.asUpdate();
        L.trace("{}", insert.toString());
        L.debug("Sending two update requests");
        exec( delete, insert);
    }

    @Override
    public Specification loadSpec(String id) throws IOException {
        L.debug("Load spec {}", id);
        Node apiURI = toRDF.api(id);
        String q = "SELECT ?endpoint ?queryText WHERE { " +
                "GRAPH ?apiURI {" +
                " ?apiURI <" + BasilOntology.Term.id.getIRIString() + "> ?id . " +
                " ?apiURI <" + BasilOntology.Term.endpoint.getIRIString() + "> ?endpoint . " +
                " ?apiURI <" + BasilOntology.Term.query.getIRIString() + "> ?queryText . " + "}}";
        ParameterizedSparqlString pqs2 = new ParameterizedSparqlString();
        pqs2.setCommandText(q);
        pqs2.setParam("apiURI", apiURI);
        pqs2.setLiteral("id", id); // redundant but hey...
        L.trace("query {}", q);
        dataset.begin(ReadWrite.READ);
        try (QueryExecution qe = QueryExecutionFactory.create(pqs2.asQuery(), dataset);) {
            ResultSet rs = qe.execSelect();
            if(!rs.hasNext()){
                throw new IOException("Spec id does not exists!");
            }
            QuerySolution qs = rs.next();
            return SpecificationFactory.create(
                    qs.get("endpoint").asLiteral().getLexicalForm(),
                    qs.get("queryText").asLiteral().getLexicalForm());
        } catch (UnknownQueryTypeException e) {
            throw new IOException(e);
        } finally {
            dataset.end();
        }
    }

    @Override
    public boolean existsSpec(String id) {
        L.debug("Exists API {}", id);
        Node apiURI = toRDF.api(id);
        String query = "ASK { GRAPH ?apiURI { [] <"+ BasilOntology.Term.id.getIRIString() +"> [] } }";
        ParameterizedSparqlString pqs2 = new ParameterizedSparqlString();
        pqs2.setCommandText(query);
        pqs2.setParam("apiURI", apiURI);
        dataset.begin(ReadWrite.READ);
        org.apache.jena.query.Query qq = pqs2.asQuery();
        L.trace("query: {}", qq);
        try (QueryExecution qe = QueryExecutionFactory.create(qq, dataset);) {
            boolean rs = qe.execAsk();
            return rs;
        } finally {
            dataset.end();
        }
    }

    @Override
    public List<String> listSpecs() throws IOException {
        L.debug("List specs");
        List<String> specs = new ArrayList<>();
        String list = "SELECT ?id WHERE { GRAPH ?g { ?apiURI <" + BasilOntology.Term.id.getIRIString() + "> ?id }}";
        dataset.begin(ReadWrite.READ);
        try (QueryExecution qe = QueryExecutionFactory.create(list, dataset);) {
            ResultSet rs = qe.execSelect();
            while(rs.hasNext()){
                QuerySolution qs = rs.next();
                specs.add(qs.getLiteral("id").getLexicalForm());
            }
        } finally {
            dataset.end();
        }
        return Collections.unmodifiableList(specs);
    }

    @Override
    public List<ApiInfo> list() throws IOException {
        List<ApiInfo> specs = new ArrayList<>();
        String list = selectApiInfo;
        L.trace("query {}", list);
        dataset.begin(ReadWrite.READ);
        try (QueryExecution qe = QueryExecutionFactory.create(list, dataset);) {
            ResultSet rs = qe.execSelect();
            while(rs.hasNext()){
                QuerySolution qs = rs.next();
                ApiInfo apiInfo = buildApiInfo(qs);
                specs.add(apiInfo);
            }
        } finally {
            dataset.end();
        }
        return Collections.unmodifiableList(specs);
    }

    @Override
    public Views loadViews(String id) throws IOException {
        return null;
    }

    @Override
    public Doc loadDoc(String id) throws IOException {
        Node apiURI = toRDF.api(id);
        String query = "SELECT ?name ?description WHERE { GRAPH ?apiURI { " +
                " ?apiURI <"+ BasilOntology.Term.id.getIRIString() +"> ?id ; " +
                "  <"+ BasilOntology.Term.name.getIRIString() +"> ?name ; " +
                "  <"+ BasilOntology.Term.description.getIRIString() +"> ?description  . " +
                "}}";
        ParameterizedSparqlString pqs = new ParameterizedSparqlString();
        pqs.setCommandText(query);
        pqs.setParam("apiURI", apiURI);
        dataset.begin(ReadWrite.READ);
        Date value = null;
        try (QueryExecution qe = QueryExecutionFactory.create(pqs.asQuery(), dataset);) {
            ResultSet rs = qe.execSelect();
            if(rs.hasNext()){
                QuerySolution qs = rs.next();
                String name = qs.getLiteral("name").getLexicalForm();
                String description = qs.getLiteral("description").getLexicalForm();
                Doc doc = new Doc();
                doc.set(Doc.Field.NAME, name);
                doc.set(Doc.Field.DESCRIPTION, description);
                return doc;
            }
        } finally {
            dataset.end();
        }
        return null; // TODO Check what happens here and if it is correct
    }

    @Override
    public void saveViews(String id, Views views) throws IOException {

    }

    private UpdateRequest deleteDoc(Node apiURI){
        String deleteStr = "DELETE { GRAPH ?apiURI { " +
                " ?apiURI <" + BasilOntology.Term.name.getIRIString() + "> ?name . " +
                " ?apiURI <" + BasilOntology.Term.description.getIRIString() + "> ?description . " +
                "}} WHERE { " +
                "GRAPH ?apiURI { " +
                " ?apiURI <" + BasilOntology.Term.name.getIRIString() + "> ?name . " +
                " ?apiURI <" + BasilOntology.Term.description.getIRIString() + "> ?description . " +
                " } }";
        ParameterizedSparqlString pqs = new ParameterizedSparqlString();
        pqs.setCommandText(deleteStr);
        pqs.setParam("apiURI", apiURI);
        UpdateRequest delete = pqs.asUpdate();
        return delete;
    }
    @Override
    public void saveDoc(String id, Doc doc) throws IOException {
        Node apiURI = toRDF.api(id);
        String name = doc.get(Doc.Field.NAME);
        String desc = doc.get(Doc.Field.DESCRIPTION);
        UpdateRequest delete = deleteDoc(apiURI);
        L.trace("{}", delete.toString());
        //
        String insertStr = "INSERT DATA { GRAPH ?apiURI { " +
                " ?apiURI <" + BasilOntology.Term.name.getIRIString() + "> ?name . " +
                " ?apiURI <" + BasilOntology.Term.description.getIRIString() + "> ?description . " +
                "}} ";
        ParameterizedSparqlString pqs2 = new ParameterizedSparqlString();
        pqs2.setCommandText(insertStr);
        pqs2.setParam("apiURI", apiURI);
        pqs2.setLiteral("name", name);
        pqs2.setLiteral("description", desc);
        UpdateRequest insert = pqs2.asUpdate();
        L.trace("{}", insert.toString());
        L.debug("Sending two update requests");
        exec( delete, insert);
    }

    @Override
    public boolean deleteDoc(String id) throws IOException {
        UpdateRequest delete = deleteDoc(toRDF.api(id));
        exec(delete);
        return true; // TODO check if this is correct
    }

    @Override
    public boolean deleteSpec(String id) throws IOException {
        boolean exists = existsSpec(id);
        if(!exists){
            return exists;
        }
        Node apiURI = toRDF.api(id);
        String clearStr = "CLEAR GRAPH ?apiURI ";
        ParameterizedSparqlString pqs = new ParameterizedSparqlString();
        pqs.setCommandText(clearStr);
        pqs.setParam("apiURI", apiURI);
        UpdateRequest clear = pqs.asUpdate();
        exec(clear);
        return true;
    }


    private Date createdOrModified(String id, boolean wantCreated) throws IOException {
        L.debug("Get info for API {} {}", id, wantCreated ? "[created]" : "[modified]");
        String property;
        if(wantCreated){
            property = BasilOntology.Term.created.getIRIString();
        }else{
            property = BasilOntology.Term.modified.getIRIString();
        }
        Node apiURI = toRDF.api(id);
        String query = "SELECT ?value WHERE { GRAPH ?apiURI { ?apiURI <"+ property +"> ?value } }";
        ParameterizedSparqlString pqs = new ParameterizedSparqlString();
        pqs.setCommandText(query);
        pqs.setParam("apiURI", apiURI);
        dataset.begin(ReadWrite.READ);
        Date value = null;
        try (QueryExecution qe = QueryExecutionFactory.create(pqs.asQuery(), dataset);) {
            ResultSet rs = qe.execSelect();
            if(rs.hasNext()){
                QuerySolution qs = rs.next();
                value = new java.util.Date(qs.getLiteral("value").getLong());
            }
        } finally {
            dataset.end();
        }
        return value;
    }

    public Date created(String id) throws IOException {
        return createdOrModified(id, true);
    }
    @Override
    public Date modified(String id) throws IOException {
        return createdOrModified(id, false);
    }

    private ApiInfo buildApiInfo(QuerySolution qs){
        return new ApiInfo(){

            @Override
            public String getId() {
                return qs.get("id").asLiteral().getLexicalForm();
            }

            @Override
            public String getName() {
                if(qs.get("name") != null) {
                    return qs.get("name").asLiteral().getLexicalForm();
                }
                return null;
            }

            @Override
            public Date created() {
                Literal val = qs.get("created").asLiteral();
                // Milliseconds timestamp expected
                java.util.Date time=new java.util.Date(val.getLong());
                return time;
            }

            @Override
            public Date modified() {
                Literal val = qs.get("modified").asLiteral();
                // Milliseconds timestamp expected
                java.util.Date time=new java.util.Date(val.getLong());
                return time;
            }

            @Override
            public Set<String> alias() {
                // We do this on demand ...
                return getAlias(getId());
            }
        };
    }
    @Override
    public ApiInfo info(String id) throws IOException {
        L.debug("Get info");
        Node apiURI = toRDF.api(id);
        ParameterizedSparqlString pqs = new ParameterizedSparqlString();
        pqs.setCommandText(selectApiInfo);
        pqs.setParam("apiURI", apiURI);
        dataset.begin(ReadWrite.READ);
        ApiInfo info = null;
        try (QueryExecution qe = QueryExecutionFactory.create(pqs.asQuery(), dataset);) {
            ResultSet rs = qe.execSelect();
            if(rs.hasNext()){
                QuerySolution qs = rs.next();
                info = buildApiInfo(qs);
            }
        } finally {
            dataset.end();
        }
        return info;
    }

    @Override
    public void saveAlias(String id, Set<String> alias) throws IOException {
        // clear all aliases
        String deleteStr = "DELETE {" +
                " GRAPH ?apiURI {" +
                "?apiURI <" + BasilOntology.Term.alias.getIRIString() + "> ?alias " +
                "}" +
                "} WHERE {" +
                " GRAPH ?apiURI {" +
                " ?apiURI <" + BasilOntology.Term.alias.getIRIString() + "> ?alias " +
                "}" +
                "}";
        Node apiURI = toRDF.api(id);
        ParameterizedSparqlString psq = new ParameterizedSparqlString();
        psq.setCommandText(deleteStr);
        psq.setParam("apiURI", apiURI);
        UpdateRequest delete = psq.asUpdate();
        // reload all aliases
        StringBuilder insertStrBld = new StringBuilder();
        insertStrBld.append("INSERT DATA {" +
                " GRAPH ?apiURI {");
        for(String a: alias){
            if(!a.trim().isEmpty()){
                insertStrBld.append(
                        " ?apiURI <" + BasilOntology.Term.alias.getIRIString() + "> "
                );
                insertStrBld.append("\"\"\"");
                insertStrBld.append(a);
                insertStrBld.append("\"\"\" . ");
            }
        }
        insertStrBld.append("}}");
        ParameterizedSparqlString psq2 = new ParameterizedSparqlString();
        psq2.setCommandText(insertStrBld.toString());
        psq2.setParam("apiURI", apiURI);
        UpdateRequest insert = psq2.asUpdate();
        exec(delete, insert);
    }

    @Override
    public Set<String> loadAlias(String id) throws IOException {
        return getAlias(id);
    }

    @Override
    public String getIdByAlias(String alias) throws IOException {
        String qStr = "SELECT ?id WHERE {" +
                "GRAPH ?any {" +
                "[] <" + BasilOntology.Term.alias.getIRIString() + "> ?alias ;" +
                " <" + BasilOntology.Term.id.getIRIString() + "> ?id ." +
                "}} LIMIT 1";
        ParameterizedSparqlString psq = new ParameterizedSparqlString();
        psq.setCommandText(qStr);
        psq.setLiteral("alias", alias);
        dataset.begin();
        org.apache.jena.query.Query query = psq.asQuery();
        try (QueryExecution qe = QueryExecutionFactory.create(query, dataset);) {
            ResultSet rs = qe.execSelect();
            if(rs.hasNext()){
                return rs.next().getLiteral("id").getLexicalForm();
            }
        } finally {
            dataset.end();
        }
        return null; // TODO double check this is correct
    }

    @Override
    public String[] credentials(String id) throws IOException {
        Node apiURI = toRDF.api(id);
        String qStr = "SELECT ?username ?password WHERE { GRAPH ?apiURI {" +
                " ?apiURI <" + BasilOntology.Term.username.getIRIString() + "> ?username ; " +
                "<" + BasilOntology.Term.password.getIRIString() + "> ?password ; " +
                "}}";
        ParameterizedSparqlString psq = new ParameterizedSparqlString();
        psq.setCommandText(qStr);
        psq.setParam("apiURI", apiURI);
        dataset.begin();
        org.apache.jena.query.Query query = psq.asQuery();
        try (QueryExecution qe = QueryExecutionFactory.create(query, dataset);) {
            ResultSet rs = qe.execSelect();
            if(rs.hasNext()){
                QuerySolution qs =  rs.next();
                return Arrays.asList(qs.getLiteral("username").getLexicalForm(),
                        qs.getLiteral("password").getLexicalForm()).toArray(new String[2]);
            }
        } finally {
            dataset.end();
        }

        return new String[0]; // TODO Check this is correct
    }

    private UpdateRequest buildDeleteCredentials(Node apiURI){
        String deleteStr = "DELETE { GRAPH ?apiURI { " +
                " ?apiURI <" + BasilOntology.Term.username.getIRIString() + "> ?username . " +
                " ?apiURI <" + BasilOntology.Term.password.getIRIString() + "> ?password . " +
                "}} WHERE { " +
                "GRAPH ?apiURI { " +
                " ?apiURI <" + BasilOntology.Term.username.getIRIString() + "> ?username . " +
                " ?apiURI <" + BasilOntology.Term.password.getIRIString() + "> ?password . " +
                " } }";
        ParameterizedSparqlString pqs = new ParameterizedSparqlString();
        pqs.setCommandText(deleteStr);
        pqs.setParam("apiURI", apiURI);
        UpdateRequest delete = pqs.asUpdate();
        return delete;
    }

    @Override
    public void saveCredentials(String id, String user, String password) throws IOException {
        // DELETE
        Node apiURI = toRDF.api(id);
        UpdateRequest delete = buildDeleteCredentials(apiURI);
        L.trace("{}", delete.toString());
        // INSERT
        String insertStr = "INSERT DATA { GRAPH ?apiURI { " +
                " ?apiURI <" + BasilOntology.Term.username.getIRIString() + "> ?username . " +
                " ?apiURI <" + BasilOntology.Term.password.getIRIString() + "> ?password . " +
                "}} ";
        ParameterizedSparqlString pqs2 = new ParameterizedSparqlString();
        pqs2.setCommandText(insertStr);
        pqs2.setParam("apiURI", apiURI);
        pqs2.setLiteral("username", user);
        pqs2.setLiteral("password", password);
        UpdateRequest insert = pqs2.asUpdate();
        L.trace("{}", insert.toString());
        L.debug("Sending two update requests");
        exec( delete, insert);
    }

    @Override
    public void deleteCredentials(String id) throws IOException {
        Node apiURI = toRDF.api(id);
        UpdateRequest delete = buildDeleteCredentials(apiURI);
        exec(delete);
    }

    private Set<String> getAlias(String id){
        L.debug("Load spec {}", id);
        Node apiURI = toRDF.api(id);
        String q = "SELECT ?alias WHERE { " +
                "GRAPH ?apiURI {" +
                " ?apiURI <" + BasilOntology.Term.alias.getIRIString() + "> ?alias . " + "}}";
        ParameterizedSparqlString pqs2 = new ParameterizedSparqlString();
        pqs2.setCommandText(q);
        pqs2.setParam("apiURI", apiURI);
        L.trace("{}", q);
        Set<String> set = new HashSet<String>();
        dataset.begin(ReadWrite.READ);
        try (QueryExecution qe = QueryExecutionFactory.create(q, dataset);) {
            ResultSet rs = qe.execSelect();
            while(rs.hasNext()){
                QuerySolution qs = rs.next();
                set.add(qs.getLiteral("alias").getLexicalForm());
            }
        } finally {
            dataset.end();
        }
        return Collections.unmodifiableSet(set);
    }



    public Graph getAsMemGraph(String id){
        Node apiURI = toRDF.api(id);
        dataset.begin();
        Graph g = dataset.asDatasetGraph().getGraph(apiURI);
        GraphMem gm = new GraphMem();
        Iterator<Triple> it = g.find();
        while(it.hasNext())
            gm.add(it.next());
        dataset.end();
        return gm;
    }
}
