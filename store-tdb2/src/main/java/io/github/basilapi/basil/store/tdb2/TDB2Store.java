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
import org.apache.jena.graph.Node;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.ResultSet;
import org.apache.jena.tdb2.TDB2Factory;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class TDB2Store implements Store, SearchProvider {
    private final String location;
    private final Dataset dataset;
    RDFFactory toRDF;

    public final static Logger L = LoggerFactory.getLogger(TDB2Store.class);

    public TDB2Store(String location, RDFFactory rdfFactory) {
        toRDF = rdfFactory;
        this.location = location;
        this.dataset = TDB2Factory.connectDataset(this.location);
    }

    private void exec(UpdateRequest... updates){
        L.error("[begin write] {} update request", updates.length);
        dataset.begin(ReadWrite.WRITE);
        for(UpdateRequest update : updates) {
            L.error("{}", update.toString());
            UpdateProcessor up = UpdateExecutionFactory.create(update, dataset);
            up.execute();
        }
        dataset.commit();
        L.error("[commit] {} update request", updates.length);
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
        Node specURI = toRDF.api(id);
        L.debug("Save spec {}", specURI);
        String deleteStr = "DELETE { GRAPH ?specURI { ?specURI ?p ?o }} WHERE { GRAPH ?specURI { ?specURI ?p ?o } }";
        ParameterizedSparqlString pqs = new ParameterizedSparqlString();
        pqs.setCommandText(deleteStr);
        pqs.setParam("specURI", specURI);
        UpdateRequest delete = pqs.asUpdate();
        L.trace("{}", delete.toString());
        //
        String insertStr = "INSERT DATA { GRAPH ?specURI { " +
            " ?specURI <" + BasilOntology.Term.endpoint.getIRIString() + "> ?endpoint . " +
            " ?specURI <" + BasilOntology.Term.query.getIRIString() + "> ?queryText . " +
            "}} ";
        ParameterizedSparqlString pqs2 = new ParameterizedSparqlString();
        pqs2.setCommandText(insertStr);
        pqs2.setParam("specURI", specURI);
        pqs2.setLiteral("endpoint", spec.getEndpoint());
        pqs2.setLiteral("queryText", spec.getQuery());
        UpdateRequest insert = pqs2.asUpdate();
        L.trace("{}", insert.toString());
        exec( delete, insert);
    }

    @Override
    public Specification loadSpec(String id) throws IOException {
        L.debug("Load spec {}", id);
        String specURI = toRDF.api(id).toString();
        String q = "SELECT ?endpoint ?queryText WHERE { " +
                "GRAPH <" + specURI + "> {" +
                "<" + specURI + "> <" + BasilOntology.Term.endpoint.getIRIString() + "> ?endpoint . " +
                "<" + specURI + "> <" + BasilOntology.Term.query.getIRIString() + "> ?queryText . " +
                "}" +
                "}";
        L.trace("{}", q);
        dataset.begin(ReadWrite.READ);
        try (QueryExecution qe = QueryExecutionFactory.create(q, dataset);) {
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
        return false;
    }

    @Override
    public List<String> listSpecs() throws IOException {
        return null;
    }

    @Override
    public List<ApiInfo> list() throws IOException {
        return null;
    }

    @Override
    public Views loadViews(String id) throws IOException {
        return null;
    }

    @Override
    public Doc loadDoc(String id) throws IOException {
        return null;
    }

    @Override
    public void saveViews(String id, Views views) throws IOException {

    }

    @Override
    public void saveDoc(String id, Doc doc) throws IOException {

    }

    @Override
    public boolean deleteDoc(String id) throws IOException {
        return false;
    }

    @Override
    public boolean deleteSpec(String id) throws IOException {
        return false;
    }

    @Override
    public Date created(String id) throws IOException {
        return null;
    }

    @Override
    public Date modified(String id) throws IOException {
        return null;
    }

    @Override
    public ApiInfo info(String id) throws IOException {
        return null;
    }

    @Override
    public void saveAlias(String id, Set<String> alias) throws IOException {

    }

    @Override
    public Set<String> loadAlias(String id) throws IOException {
        return null;
    }

    @Override
    public String getIdByAlias(String alias) throws IOException {
        return null;
    }

    @Override
    public String[] credentials(String id) throws IOException {
        return new String[0];
    }

    @Override
    public void saveCredentials(String id, String user, String password) throws IOException {

    }

    @Override
    public void deleteCredentials(String id) throws IOException {

    }
}
