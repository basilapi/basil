package uk.ac.open.kmi.basil.core.auth;

import org.apache.shiro.crypto.RandomNumberGenerator;
import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.crypto.hash.Sha256Hash;
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
        // This will load the MySQL driver, each DB has its own driver
        Connection connect = null;
        try {
            if (user != null) {
                Class.forName("com.mysql.jdbc.Driver");
                // Setup the connection with the DB
                connect = DriverManager.getConnection(jdbcUri);
                PreparedStatement preparedStatement = connect.prepareStatement("insert into users values (?, ?, ?)");
                preparedStatement.setString(1, user.getUsername());
                RandomNumberGenerator rng = new SecureRandomNumberGenerator();
                Object salt = rng.nextBytes();
                String hashedPasswordBase64 = new Sha256Hash(user.getPassword(), salt, 1024).toBase64();
                preparedStatement.setString(2, hashedPasswordBase64);
                preparedStatement.setString(3, user.getEmail());
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
}
