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
import io.github.basilapi.basil.view.Engine;
import io.github.basilapi.basil.view.View;
import io.github.basilapi.basil.view.Views;
import org.apache.commons.lang3.builder.HashCodeBuilder;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TDB2Store implements Store, SearchProvider {
    private String dataNS;
    private final String location;
    final Dataset dataset;
    RDFFactory toRDF;

    public final static Logger L = LoggerFactory.getLogger(TDB2Store.class);

    private static final String selectApiInfo = "SELECT ?id ?name ?created ?modified " +
            "WHERE { GRAPH ?apiURI {" +
            "?apiURI a <" + BasilOntology.Term.Api.getIRIString() + "> ; " +
            "<" + BasilOntology.Term.id.getIRIString() + "> ?id ; " +
            "<" + BasilOntology.Term.created.getIRIString() + "> ?created ; " +
            "<" + BasilOntology.Term.modified.getIRIString() + "> ?modified . " +
            "OPTIONAL { ?apiURI <" + BasilOntology.Term.name.getIRIString() + "> ?name } . " +
            "}}";

    public TDB2Store(String location, RDFFactory factory) {
        this.toRDF = factory;
        this.location = location;
        this.dataset = TDB2Factory.connectDataset(this.location);
    }

    private void exec(UpdateRequest... updates){
        L.debug("[begin write] {} update request", updates.length);
        try {
            dataset.begin(ReadWrite.WRITE);
            for (UpdateRequest update : updates) {
                L.trace("{}", update.toString());
                UpdateProcessor up = UpdateExecutionFactory.create(update, dataset);
                up.execute();
            }
            dataset.commit();
            L.debug("[commit] {} update request", updates.length);
        } finally {
            dataset.end();
        }
    }
    private String _buildSearchQuery(Query query, boolean onlyIds) {
        StringBuilder qb = new StringBuilder();
        qb.append("SELECT ?nickname");
        if (!onlyIds) {
            qb.append(" ?p ?o ");
        }
        qb.append("\nWHERE {\n");
        qb.append("\tGRAPH ?apiURI { \n");
        qb.append("\t\t ?apiURI ");
        qb.append("<");
        qb.append(BasilOntology.Term.id.getIRIString());
        qb.append("> ?nickname .\n");
        BasilOntology.Term[] terms = new BasilOntology.Term[]{
                BasilOntology.Term.endpoint,
                BasilOntology.Term.id,
                BasilOntology.Term.name,
                BasilOntology.Term.query,
                BasilOntology.Term.expandedQuery,
                BasilOntology.Term.description
        };
        if (!onlyIds) {
            qb.append("\t\tVALUES ?p {\n");
            for(BasilOntology.Term term : terms) {
                qb.append("\t\t\t");
                qb.append("<");
                qb.append(term.getIRIString());
                qb.append("> \n");
            }
            qb.append("\t\t}\n");
            qb.append("\t\t ?apiURI ?p ?o .\n");
        }
        // Endpoint
        if (query.getEndpoint() != null) {
            qb.append("\t\t ?apiURI ");
            qb.append("<");
            qb.append(BasilOntology.Term.endpoint.getIRIString());
            qb.append("> ?endpoint .\n");
        }

        if(query.getNamespaces().length > 0 || query.getResources().length > 0) {
            qb.append("\t\t ?apiURI ");
            qb.append("<");
            qb.append(BasilOntology.Term.expandedQuery.getIRIString());
            qb.append("> ?expandedQuery .\n");
            // Namespaces
            if (query.getNamespaces().length > 0) {
                qb.append("\t\tFILTER (\n");
                for (int i = 0; i < query.getNamespaces().length; i++) {
                    if(i > 0){
                        qb.append(" && ");
                    }
                    qb.append("\t\t\tREGEX(");
                    qb.append("?expandedQuery, ");
                    qb.append("?nsi");
                    qb.append(i + 1);
                    qb.append(", \"i\") \n");
                }
                qb.append("\t\t) . \n");
            }
            // Resources
            if (query.getResources().length > 0) {
                qb.append("\t\tFILTER (\n");
                for (int i = 0; i < query.getResources().length; i++) {
                    if(i > 0){
                        qb.append(" && ");
                    }
                    qb.append("\t\t\tREGEX(");
                    qb.append("?expandedQuery, ");
                    qb.append("?rsi");
                    qb.append(i + 1);
                    qb.append(", \"i\") \n");
                }
                qb.append("\t\t) . \n");
            }
        }

//        qb.append("");

        String txt = query.getText();
        if(!txt.trim().isEmpty()) {
            String[] txts = txt.split(" ");
            qb.append("\t\tFILTER EXISTS {\n");
            for (int i = 0; i < txts.length; i++) {
                if (i > 0) {
                    qb.append("\t\tUNION\n");
                }
                qb.append("\t\t{\n");
                qb.append("\t\t\t?apiURI ?pExists ?oExists . \n");
                qb.append("\t\t\tFILTER(REGEX(?oExists, ");
                qb.append("?txt");
                qb.append(i+1); // Counter starts from 1
                qb.append(", \"i\")) .");
                qb.append("\t\t}\n");
            }
            qb.append("\t\t}\n");
        }
        qb.append("\t}\n}");
        String queryStr = qb.toString();
        L.trace("Search query (prepared): {}",queryStr);
        //System.err.println("query text: " + txt);
        return queryStr;
    }

    private void _mapSearchParameters(ParameterizedSparqlString pss, Query query) {
        String[] txts = query.getText().split(" ");
        int pos = 1;

        // Endpoint
        if (query.getEndpoint() != null) {
            pss.setLiteral("endpoint", query.getEndpoint());
        }
        // Namespaces
        if (query.getNamespaces() != null) {
            int nsi = 1;
            for (String t : query.getNamespaces()) {
                pss.setLiteral("nsi" + String.valueOf(nsi), t);
                nsi++;
            }
        }
        // Resources
        if (query.getResources() != null) {
            int nsi = 1;
            for (String t : query.getResources()) {
                pss.setLiteral("rsi" + String.valueOf(nsi), t);
                nsi++;
            }
        }

        // Text
        int nsi = 1;
        for (String t : txts) {
            pss.setLiteral("txt" + String.valueOf(nsi), t);
            nsi++;
        }
        //System.err.println(pss.toString());
        if(L.isTraceEnabled()) {
            L.trace("Search query (processed): {}", pss.toString());
        }
    }

    @Override
    public List<String> search(Query query) throws IOException {
        List<String> results = new ArrayList<String>();
        String q = this._buildSearchQuery(query, true);
        ParameterizedSparqlString psq = new ParameterizedSparqlString();
        psq.setCommandText(q);
        this._mapSearchParameters(psq, query);
        L.trace("query {}", q);
        try (QueryExecution qe = QueryExecutionFactory.create(psq.asQuery(), dataset);) {
            dataset.begin(ReadWrite.READ);
            ResultSet rs = qe.execSelect();
            while(rs.hasNext()) {
               QuerySolution qs = rs.next();
               results.add(qs.getLiteral("nickname").getLexicalForm());
           }
        } catch (Exception e) {
            throw new IOException(e);
        } finally {
            dataset.end();
        }

        return results;
    }
    @Override
    public Collection<Result> contextSearch(Query query) throws IOException {
        Map<String, Result> results = new HashMap<String, Result>();
        String q = this._buildSearchQuery(query, false); // Add context
        ParameterizedSparqlString psq = new ParameterizedSparqlString();
        psq.setCommandText(q);
        this._mapSearchParameters(psq, query);
        L.trace("query {}", q);
        try (QueryExecution qe = QueryExecutionFactory.create(psq.asQuery(), dataset);) {
            dataset.begin(ReadWrite.READ);
            ResultSet rs = qe.execSelect();
            while(rs.hasNext()) {
                QuerySolution qs = rs.next();
                String apiId = qs.getLiteral("nickname").getLexicalForm();
                if (!results.containsKey( apiId )) {
                    results.put(apiId, new QuerySolutionResult(apiId));
                }
                // FIXME This will allow only 1 value per property type...
                //  there are cases where we get more, e.g. aliases...
                ((QuerySolutionResult) results.get(apiId)).put(qs.get("p").toString(), qs.get("o").toString());
            }
        } catch (Exception e) {
            throw new IOException(e);
        } finally {
            dataset.end();
        }
        return results.values();
    }

    @Override
    public void saveSpec(String id, Specification spec) throws IOException {
        boolean exists = existsSpec(id);
        Node apiURI = toRDF.api(id);
        L.debug("Save API spec {}", apiURI);
        String deleteStr = "DELETE { GRAPH ?apiURIXXXXXXX { " +
                " ?apiURIXXXXXXX <" + BasilOntology.Term.endpoint.getIRIString() + "> ?endpoint . " +
                " ?apiURIXXXXXXX <" + BasilOntology.Term.query.getIRIString() + "> ?queryText . " +
                " ?apiURIXXXXXXX <" + BasilOntology.Term.expandedQuery.getIRIString() + "> ?expandedQueryText . " +
                " ?apiURIXXXXXXX <" + BasilOntology.Term.modified.getIRIString() + "> ?modified . " +
                "}} WHERE { " +
                "GRAPH ?apiURIXXXXXXX { " +
                " ?apiURIXXXXXXX <" + BasilOntology.Term.endpoint.getIRIString() + "> ?endpoint . " +
                " ?apiURIXXXXXXX <" + BasilOntology.Term.query.getIRIString() + "> ?queryText . " +
                " ?apiURIXXXXXXX <" + BasilOntology.Term.expandedQuery.getIRIString() + "> ?expandedQueryText . " +
                " ?apiURIXXXXXXX <" + BasilOntology.Term.modified.getIRIString() + "> ?modified . " +
                " } }";
        ParameterizedSparqlString pqs = new ParameterizedSparqlString();
        pqs.setCommandText(deleteStr);
        pqs.setParam("apiURIXXXXXXX", apiURI);
        UpdateRequest delete = pqs.asUpdate();
        L.trace("{}", delete.toString());
        //
        String insertStr = "INSERT DATA { GRAPH ?apiURIXXXXXXX { " +
                " ?apiURIXXXXXXX a <" + BasilOntology.Term.Api.getIRIString() + "> ." +
                " ?apiURIXXXXXXX <" + BasilOntology.Term.id.getIRIString() + "> ?idXXXXXXX . " +
                " ?apiURIXXXXXXX <" + BasilOntology.Term.endpoint.getIRIString() + "> ?endpointXXXXXXX . " +
                " ?apiURIXXXXXXX <" + BasilOntology.Term.expandedQuery.getIRIString() + "> ?expandedQueryTextXXXXXXX . " +
                " ?apiURIXXXXXXX <" + BasilOntology.Term.query.getIRIString() + "> ?queryTextXXXXXXX . " +
                ((!exists) ? " ?apiURIXXXXXXX <" + BasilOntology.Term.created.getIRIString() + "> ?createdXXXXXXX . " : "") +
                " ?apiURIXXXXXXX <" + BasilOntology.Term.modified.getIRIString() + "> ?modifiedXXXXXXX . " +
            "}} ";
        ParameterizedSparqlString pqs2 = new ParameterizedSparqlString();
        pqs2.setCommandText(insertStr);
        pqs2.setParam("apiURIXXXXXXX", apiURI);
        pqs2.setLiteral("endpointXXXXXXX", spec.getEndpoint());
        pqs2.setLiteral("idXXXXXXX", id);
        pqs2.setLiteral("queryTextXXXXXXX", spec.getQuery());
        pqs2.setLiteral("expandedQueryTextXXXXXXX", spec.getExpandedQuery());
        if(!exists){
            pqs2.setLiteral("createdXXXXXXX", System.currentTimeMillis());
        }
        pqs2.setLiteral("modifiedXXXXXXX", System.currentTimeMillis());
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
                " ?apiURI <" + BasilOntology.Term.endpoint.getIRIString() + "> ?endpoint . " +
                " ?apiURI <" + BasilOntology.Term.query.getIRIString() + "> ?queryText . " + "}}";
        ParameterizedSparqlString pqs2 = new ParameterizedSparqlString();
        pqs2.setCommandText(q);
        pqs2.setParam("apiURI", apiURI);
        L.trace("query {}", q);
        try (QueryExecution qe = QueryExecutionFactory.create(pqs2.asQuery(), dataset);) {
            dataset.begin(ReadWrite.READ);
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
        String query = "ASK { GRAPH ?apiURI { [] a [] } }";
        ParameterizedSparqlString pqs2 = new ParameterizedSparqlString();
        pqs2.setCommandText(query);
        pqs2.setParam("apiURI", apiURI);

        org.apache.jena.query.Query qq = pqs2.asQuery();
        L.trace("query: {}", qq);
        dataset.begin(ReadWrite.READ);
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
        Node apiURI = toRDF.api(id);
        String selectStr = "SELECT ?extension ?mimeType ?engine ?template " +
                " WHERE {" +
                "GRAPH ?apiURI {" +
                " ?apiURI <" + BasilOntology.Term.view.getIRIString() + "> [ " +
                "    <" + BasilOntology.Term.extension.getIRIString() + "> ?extension ; " +
                "    <" + BasilOntology.Term.mimeType.getIRIString() + "> ?mimeType ; " +
                "    <" + BasilOntology.Term.engine.getIRIString() + "> ?engine ; " +
                "    <" + BasilOntology.Term.template.getIRIString() + "> ?template " +
                "] . }}";

        ParameterizedSparqlString pqs = new ParameterizedSparqlString();
        pqs.setCommandText(selectStr);
        pqs.setParam("apiURI", apiURI);
        dataset.begin(ReadWrite.READ);
        Views views = new Views();
        try (QueryExecution qe = QueryExecutionFactory.create(pqs.asQuery(), dataset);) {
            ResultSet rs = qe.execSelect();
            while(rs.hasNext()){
                QuerySolution qs = rs.next();
                views.put(
                        qs.getLiteral("mimeType").getLexicalForm(),
                        qs.getLiteral("extension").getLexicalForm(),
                        qs.getLiteral("template").getLexicalForm(),
                        Engine.valueOf(qs.getLiteral("engine").getLexicalForm())
                );
            }
        } finally {
            dataset.end();
        }
        return views;
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
        Doc doc = new Doc();
        try (QueryExecution qe = QueryExecutionFactory.create(pqs.asQuery(), dataset);) {
            ResultSet rs = qe.execSelect();
            if(rs.hasNext()){
                QuerySolution qs = rs.next();
                String name = qs.getLiteral("name").getLexicalForm();
                String description = qs.getLiteral("description").getLexicalForm();

                doc.set(Doc.Field.NAME, name);
                doc.set(Doc.Field.DESCRIPTION, description);
                return doc;
            }
        } finally {
            dataset.end();
        }
        return doc; // return an empty doc
    }

    @Override
    public void saveViews(String id, Views views) throws IOException {
        Node apiURI = toRDF.api(id);
        List<UpdateRequest> requests = new ArrayList<>();
        // DELETE
        String deleteStr = "DELETE {" +
                "GRAPH ?apiURI {" +
                " ?apiURI <"+ BasilOntology.Term.view + "> ?bn ." +
                " ?bn a <"+ BasilOntology.Term.View + "> ; " +
                "  <"+ BasilOntology.Term.extension + "> ?extension ; " +
                "  <"+ BasilOntology.Term.mimeType + "> ?mimeType ; " +
                "  <"+ BasilOntology.Term.template + "> ?template . " +
                "" +
                "}" +
                "} WHERE {" +
                "GRAPH ?apiURI {" +
                " ?apiURI <"+ BasilOntology.Term.view + "> ?bn ." +
                "?bn  a <"+ BasilOntology.Term.View + "> ; " +
                "  <"+ BasilOntology.Term.extension + "> ?extension ; " +
                "  <"+ BasilOntology.Term.engine + "> ?mimeType ; " +
                "  <"+ BasilOntology.Term.mimeType + "> ?mimeType ; " +
                "  <"+ BasilOntology.Term.template + "> ?template ." +
                "" +
                "}" +
                "}";
        // INSERT
        ParameterizedSparqlString pqs = new ParameterizedSparqlString();
        pqs.setCommandText(deleteStr);
        pqs.setParam("apiURI", apiURI);
        UpdateRequest delete = pqs.asUpdate();
        L.trace("{}", delete.toString());
        //
        requests.add(delete);
        //
        for(String viewName : views.getNames()){
            View view = views.byName(viewName);
            String insertStr = "INSERT DATA { GRAPH ?apiURI { " +
                    " ?apiURI <" + BasilOntology.Term.view.getIRIString() + "> [ " +
                    "     a    <" + BasilOntology.Term.View.getIRIString() + "> ; " +
                    "         <" + BasilOntology.Term.extension.getIRIString() + "> ?extension ; " +
                    "         <" + BasilOntology.Term.mimeType.getIRIString() + "> ?mimeType ; " +
                    "         <" + BasilOntology.Term.engine.getIRIString() + "> ?engine ; " +
                    "         <" + BasilOntology.Term.template.getIRIString() + "> ?template " +
                    "] }} ";
            ParameterizedSparqlString pqs2 = new ParameterizedSparqlString();
            pqs2.setCommandText(insertStr);
            pqs2.setParam("apiURI", apiURI);
            pqs2.setLiteral("extension", view.getName());
            pqs2.setLiteral("mimeType", view.getMimeType());
            pqs2.setLiteral("engine", view.getEngine().name());
            pqs2.setLiteral("template", view.getTemplate());
            UpdateRequest insert = pqs2.asUpdate();
            L.trace("{}", insert.toString());
            requests.add(insert);
        }

        L.debug("Sending {} update requests", requests.size());
        exec( requests.toArray(new UpdateRequest[requests.size()]));
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

    /**
     * This will delete the whole API data!
     *
     * @param id
     * @return
     * @throws IOException
     */
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
        //System.out.println("save alias: " + alias.size());
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
        dataset.begin(ReadWrite.READ);
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

        return null; // XXX Needs to return null if no credentials found
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
        L.trace("{}", pqs2.asQuery().toString());
        Set<String> set = new HashSet<String>();
        dataset.begin(ReadWrite.READ);
        try (QueryExecution qe = QueryExecutionFactory.create(pqs2.asQuery(), dataset);) {
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

    static class QuerySolutionResult implements Result {

        private String id;
        private Map<String, String> context;
        private int hashCode;

        public QuerySolutionResult(String id) {
            this.id = id;
            this.context = new HashMap<String, String>();
            this.hashCode = new HashCodeBuilder().append(this.id).hashCode();
        }

        public int hashCode() {
            return hashCode;
        };

        @Override
        public String id() {
            return id;
        }

        public void put(String property, String value) {
            context.put(property, value);
        }

        @Override
        public Map<String, String> context() {
            return Collections.unmodifiableMap(context);
        }
    }
}
