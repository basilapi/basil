package uk.ac.open.kmi.basil.core;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;

import uk.ac.open.kmi.basil.core.auth.UserManager;
import uk.ac.open.kmi.basil.core.auth.exceptions.UserApiMappingException;
import uk.ac.open.kmi.basil.core.exceptions.ApiInvocationException;
import uk.ac.open.kmi.basil.core.exceptions.SpecificationParsingException;
import uk.ac.open.kmi.basil.doc.Doc;
import uk.ac.open.kmi.basil.sparql.QueryParameter;
import uk.ac.open.kmi.basil.sparql.Specification;
import uk.ac.open.kmi.basil.sparql.SpecificationFactory;
import uk.ac.open.kmi.basil.sparql.VariablesBinder;
import uk.ac.open.kmi.basil.store.Store;
import uk.ac.open.kmi.basil.view.Engine;
import uk.ac.open.kmi.basil.view.View;
import uk.ac.open.kmi.basil.view.Views;

import javax.ws.rs.core.MultivaluedMap;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Luca Panziera on 15/06/15.
 */
public class ApiManagerImpl implements ApiManager {
    private Store data;
    private UserManager userManager;

    public ApiManagerImpl(Store store, UserManager um) {
        data = store;
        userManager = um;
    }

    /**
     * Method to generate API Ids.
     * <p/>
     * XXX Not sure this is good, but it's an option - enridaga
     *
     * @return
     * @see https://gist.github.com/LeeSanghoon/5811136
     */

    private static String shortUUID() {
        UUID uuid = UUID.randomUUID();
        long l = ByteBuffer.wrap(uuid.toString().getBytes()).getLong();
        return Long.toString(l, Character.MAX_RADIX);
    }

    public InvocationResult invokeApi(String id, MultivaluedMap<String, String> parameters) throws IOException, ApiInvocationException {
        if (!data.existsSpec(id)) {
            throw new ApiInvocationException("Specification not found");
        }

        Specification specification = data.loadSpec(id);
        VariablesBinder binder = new VariablesBinder(specification);

        List<String> missing = new ArrayList<String>();
        for (QueryParameter qp : specification.getParameters()) {
            if (parameters.containsKey(qp.getName())) {
                List<String> values = parameters.get(qp.getName());
                binder.bind(qp.getName(), values.get(0));
            } else if (!qp.isOptional()) {
                missing.add(qp.getName());
            }
        }

        if (!missing.isEmpty()) {
            StringBuilder ms = new StringBuilder();
            ms.append("Missing mandatory query parameters: ");
            for (String p : missing) {
                ms.append(p);
                ms.append("\t");
            }
            ms.append("\n");
            throw new ApiInvocationException(ms.toString());
        }

        Query q = binder.toQuery();
        if(parameters.containsKey("limit")){
        	q.setLimit(Long.parseLong(parameters.getFirst("limit")));
        }
        if(parameters.containsKey("offset")){
        	q.setOffset(Long.parseLong(parameters.getFirst("offset")));
        }
        
        QueryExecution qe = QueryExecutionFactory.sparqlService(
                specification.getEndpoint(), q);

        if (q.isSelectType()) {
            return new InvocationResult(qe.execSelect(), q);
        } else if (q.isConstructType()) {
            return new InvocationResult(qe.execConstruct(), q);
        } else if (q.isAskType()) {
            return new InvocationResult(qe.execAsk(), q);
        } else if (q.isDescribeType()) {
            return new InvocationResult(qe.execDescribe(), q);
        } else {
            throw new ApiInvocationException("Unsupported query type: " + q.getQueryType());
        }

    }

    public String createSpecification(String username, String endpoint, String body) throws IOException, SpecificationParsingException, UserApiMappingException {
        String id = shortUUID();
        if (body.equals("")) {
            throw new SpecificationParsingException("Body cannot be empty");
        }
        Specification specification = SpecificationFactory.create(endpoint, body);
        data.saveSpec(id, specification);
        userManager.mapUserApi(username, id);
        return id;
    }

    public String cloneSpecification(String username, String id) throws IOException, UserApiMappingException {
        String newId = shortUUID();
        Specification specification = data.loadSpec(id);
        Doc doc = data.loadDoc(id);
        Views views = listViews(id);
        data.saveSpec(newId, specification);
        userManager.mapUserApi(username, newId);
        if (!doc.isEmpty()) {
            data.saveDoc(newId, doc);
        }
        if (views.numberOf() > 0) {
            data.saveViews(newId, views);
        }
        return newId;
    }

    public void replaceSpecification(String id, String body) throws IOException, SpecificationParsingException {
    	Specification oldSpec = data.loadSpec(id);
    	replaceSpecification(id, oldSpec.getEndpoint(), body);
    }
    
    public void replaceSpecification(String id, String endpoint, String body) throws IOException, SpecificationParsingException {
        if (body.equals("")) {
            throw new SpecificationParsingException("Body cannot be empty");
        }
        Specification specification = SpecificationFactory.create(endpoint, body);
        data.saveSpec(id, specification);
    }

    public boolean deleteApi(String id) throws IOException, UserApiMappingException {
        if(data.deleteSpec(id)){
        	userManager.deleteUserApiMap(id);
        	return true;
        }
        return false;
    }

    public List<String> listApis() throws IOException {
        return data.listSpecs();
    }

    public Specification getSpecification(String id) throws IOException {
        return data.loadSpec(id);
    }

    public Views listViews(String id) throws IOException {
        return data.loadViews(id);
    }

    public View getView(String id, String name) throws IOException {
        return data.loadViews(id).byName(name);
    }

    public void deleteView(String id, String name) throws IOException {
        Views views = data.loadViews(id);
        views.remove(data.loadViews(id).byName(name));
        data.saveViews(id, views);
    }

    public void createView(String id, String mimeType, String name, String template, Engine engine) throws IOException {
        Views views = data.loadViews(id);
        views.put(mimeType, name, template, engine);
        data.saveViews(id, views);
    }

    public Doc getDoc(String id) throws IOException {
        return data.loadDoc(id);
    }

    public boolean deleteDoc(String id) throws IOException {
        return data.deleteDoc(id);
    }

    public boolean existsSpec(String id) throws IOException{
    	return data.existsSpec(id);
    }

    public void createDoc(String id, String name, String body) throws IOException {
        Doc doc = getDoc(id);
        doc.set(Doc.Field.NAME, name);
        doc.set(Doc.Field.DESCRIPTION, body);
        data.saveDoc(id, doc);
    }

    public void replaceDoc(String id, String name, String body) throws IOException {
        createDoc(id, name, body);
    }

    public String getCreatorOfApi(String id) {
        return userManager.getCreatorOfApi(id);
    }

    @Override
    public ApiInfo getInfo(String api) throws IOException {
    	return data.info(api);
    }
}
