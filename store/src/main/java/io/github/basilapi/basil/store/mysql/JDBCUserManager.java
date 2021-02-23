/*
 * Copyright (c) 2021. Enrico Daga and Luca Panziera
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

package io.github.basilapi.basil.store.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import io.github.basilapi.basil.core.auth.User;
import io.github.basilapi.basil.core.auth.UserManager;
import io.github.basilapi.basil.core.auth.exceptions.UserApiMappingException;
import io.github.basilapi.basil.core.auth.exceptions.UserCreationException;
import org.apache.shiro.authc.credential.DefaultPasswordService;

/**
 * Created by Luca Panziera on 26/06/15.
 */
public class JDBCUserManager implements UserManager {

    private String jdbcUri;
    
    public JDBCUserManager(String jdbcUri) {
    	this.jdbcUri = jdbcUri;
	}

    public void createUser(User user) throws UserCreationException {

        Connection connect = null;
        try {
            if (user != null) {
                Class.forName("com.mysql.jdbc.Driver");
                // Setup the connection with the DB
                connect = DriverManager.getConnection(jdbcUri);
                PreparedStatement preparedStatement = connect.prepareStatement("insert into users (username, email, password) values (?, ?, ?)");
                preparedStatement.setString(1, user.getUsername());
                preparedStatement.setString(2, user.getEmail());

                DefaultPasswordService passwordService = new DefaultPasswordService();
                preparedStatement.setString(3, passwordService.encryptPassword(user.getPassword()));

                preparedStatement.executeUpdate();
                preparedStatement.close();
                preparedStatement = connect.prepareStatement("insert into users_roles (username, role_name) values (?, ?)");
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
                PreparedStatement preparedStatement = connect.prepareStatement("insert into users_roles values (?, ?)");
                preparedStatement.setString(1, username);
                preparedStatement.setString(2, apiId);
                preparedStatement.executeUpdate();
                preparedStatement.close();
                preparedStatement = connect.prepareStatement("insert into roles values (?)");
                preparedStatement.setString(1, apiId);
                preparedStatement.executeUpdate();
                preparedStatement.close();
                preparedStatement = connect.prepareStatement("insert into roles_permissions values (?, ?)");
                preparedStatement.setString(1, apiId);
                preparedStatement.setString(2, "write");
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

    public User getUser(String username) {
        Connection connect = null;
        try {
            if (username != null) {
                Class.forName("com.mysql.jdbc.Driver");
                // Setup the connection with the DB
                connect = DriverManager.getConnection(jdbcUri);
                PreparedStatement preparedStatement = connect.prepareStatement("SELECT * FROM users WHERE username = ?");
                preparedStatement.setString(1, username);
                ResultSet rs = preparedStatement.executeQuery();
                User user = null;
                if (rs.next()) {
                    user = new User();
                    user.setUsername(rs.getString("username"));
                    user.setEmail(rs.getString("email"));
                }
                preparedStatement.close();
                return user;
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (connect != null) {
                    connect.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public Set<String> getUserApis(String username) {
        Connection connect = null;
        try {
            if (username != null) {
                Class.forName("com.mysql.jdbc.Driver");
                // Setup the connection with the DB
                connect = DriverManager.getConnection(jdbcUri);
                PreparedStatement preparedStatement = connect.prepareStatement("SELECT role_name FROM users_roles WHERE username = ? AND role_name != \"default\"");
                preparedStatement.setString(1, username);
                ResultSet rs = preparedStatement.executeQuery();
                Set<String> r = new HashSet<String>();
                while (rs.next()) {
                    r.add(rs.getString("role_name"));
                }
                preparedStatement.close();
                return r;
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (connect != null) {
                    connect.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void deleteUserApiMap(String apiId) throws UserApiMappingException {
        Connection connect = null;
        try {
            if (apiId != null) {
                Class.forName("com.mysql.jdbc.Driver");
                // Setup the connection with the DB
                connect = DriverManager.getConnection(jdbcUri);
                connect.setAutoCommit(false);
                try{
	                PreparedStatement preparedStatement = connect.prepareStatement("DELETE FROM users_roles WHERE role_name = ? ");
	                preparedStatement.setString(1, apiId);
	                preparedStatement.executeUpdate();
	                preparedStatement.close();
	                preparedStatement = connect.prepareStatement("DELETE FROM roles WHERE name = ? ");
	                preparedStatement.setString(1, apiId);
	                preparedStatement.executeUpdate();
	                preparedStatement.close();
	                preparedStatement = connect.prepareStatement("DELETE FROM roles_permissions WHERE role_name = ? ");
	                preparedStatement.setString(1, apiId);
	                preparedStatement.executeUpdate();
	                preparedStatement.close();
	                connect.commit();
                }finally{
                	connect.setAutoCommit(true);
                }
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

    public String getCreatorOfApi(String id) {
        Connection connect = null;
        try {
            if (id != null) {
                Class.forName("com.mysql.jdbc.Driver");
                // Setup the connection with the DB
                connect = DriverManager.getConnection(jdbcUri);
                PreparedStatement preparedStatement = connect.prepareStatement("SELECT username FROM users_roles WHERE role_name = ?");
                preparedStatement.setString(1, id);
                ResultSet rs = preparedStatement.executeQuery();
                String r = null;
                if (rs.next()) {
                    r = rs.getString("username");
                }
                preparedStatement.close();
                return r;
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (connect != null) {
                    connect.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}
