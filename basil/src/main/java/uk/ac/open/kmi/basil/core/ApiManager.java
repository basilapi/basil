package uk.ac.open.kmi.basil.core;

import uk.ac.open.kmi.basil.core.auth.exceptions.UserApiMappingException;
import uk.ac.open.kmi.basil.core.exceptions.ApiInvocationException;
import uk.ac.open.kmi.basil.core.exceptions.SpecificationParsingException;
import uk.ac.open.kmi.basil.doc.Doc;
import uk.ac.open.kmi.basil.sparql.Specification;
import uk.ac.open.kmi.basil.view.Engine;
import uk.ac.open.kmi.basil.view.View;
import uk.ac.open.kmi.basil.view.Views;

import javax.ws.rs.core.MultivaluedMap;

import java.io.IOException;
import java.util.List;

/**
 * Created by Luca Panziera on 15/06/15.
 */
public interface ApiManager {
    InvocationResult invokeApi(String id, MultivaluedMap<String, String> parameters) throws IOException, ApiInvocationException;

    String createSpecification(String username, String endpoint, String body) throws SpecificationParsingException, UserApiMappingException;

    String cloneSpecification(String username, String id) throws IOException, UserApiMappingException;

    void replaceSpecification(String id, String endpoint, String body) throws IOException, SpecificationParsingException;
    
    void replaceSpecification(String id, String body) throws IOException, SpecificationParsingException;

    boolean deleteApi(String id) throws IOException, UserApiMappingException;

    List<String> listApis();

    Specification getSpecification(String id) throws IOException;

    Views listViews(String id) throws IOException;

    View getView(String id, String name) throws IOException;

    void deleteView(String id, String name) throws IOException;

    void createView(String id, String mimeType, String name, String template, Engine engine) throws IOException;

    Doc getDoc(String id) throws IOException;

    boolean deleteDoc(String id) throws IOException;

    boolean existsSpec(String id);

    void createDoc(String id, String name, String body) throws IOException;

    void replaceDoc(String id, String name, String body) throws IOException;

    String getCreatorOfApi(String id);
}
