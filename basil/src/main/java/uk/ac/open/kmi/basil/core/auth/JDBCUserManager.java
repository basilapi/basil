package uk.ac.open.kmi.basil.core.auth;

import org.apache.shiro.authc.credential.DefaultPasswordService;
import uk.ac.open.kmi.basil.core.auth.exceptions.UserApiMappingException;
import uk.ac.open.kmi.basil.core.auth.exceptions.UserCreationException;
import uk.ac.open.kmi.basil.rest.BasilApplication;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Created by Luca Panziera on 26/06/15.
 */
public class JDBCUserManager implements UserManager {

    private String jdbcUri = BasilApplication.Registry.JdbcUri;

    public void createUser(User user) throws UserCreationException {

        Connection connect = null;
        try {
            if (user != null) {
                Class.forName("com.mysql.jdbc.Driver");
                // Setup the connection with the DB
                connect = DriverManager.getConnection(jdbcUri);
                PreparedStatement preparedStatement = connect.prepareStatement("insert into USERS values (?, ?, ?)");
                preparedStatement.setString(1, user.getUsername());
                preparedStatement.setString(2, user.getEmail());

                DefaultPasswordService passwordService = new DefaultPasswordService();
                preparedStatement.setString(3, passwordService.encryptPassword(user.getPassword()));

                preparedStatement.executeUpdate();
                preparedStatement.close();
                preparedStatement = connect.prepareStatement("insert into USERS_ROLES values (?, ?)");
                preparedStatement.setString(1, user.getUsername());
                preparedStatement.setString(2, "default");
                preparedStatement.executeUpdate();
                preparedStatement.close();
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new UserCreationException(e.getMessage());
        } catch (SQLException e) {
            e.printStackTrace();
            throw new UserCreationException(e.getMessage());
        } finally {
            try {
                if (connect != null) {
                    connect.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }

    public void mapUserApi(String username, String apiId) throws UserApiMappingException {
        Connection connect = null;
        try {
            if (username != null) {
                Class.forName("com.mysql.jdbc.Driver");
                // Setup the connection with the DB
                connect = DriverManager.getConnection(jdbcUri);
                PreparedStatement preparedStatement = connect.prepareStatement("insert into user_roles values (DEFAULT , ?, ?)");
                preparedStatement.setString(1, username);
                preparedStatement.setString(2, apiId);
                preparedStatement.executeUpdate();
                preparedStatement.close();
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new UserApiMappingException(e.getMessage());
        } catch (SQLException e) {
            e.printStackTrace();
            throw new UserApiMappingException(e.getMessage());
        } finally {
            try {
                if (connect != null) {
                    connect.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }

}
