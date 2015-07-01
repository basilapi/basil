package uk.ac.open.kmi.basil.core.auth;

import uk.ac.open.kmi.basil.core.auth.exceptions.UserApiMappingException;
import uk.ac.open.kmi.basil.core.auth.exceptions.UserCreationException;

import java.util.Set;

/**
 * Created by Luca Panziera on 26/06/15.
 */
public interface UserManager {
    void createUser(User user) throws UserCreationException;
    void mapUserApi(String username, String apiId) throws UserApiMappingException;

    User getUser(String username);

    Set<String> getUserApis(String username);

    void deleteUserApiMap(String id) throws UserApiMappingException;

    String getCreatorOfApi(String id);
}
