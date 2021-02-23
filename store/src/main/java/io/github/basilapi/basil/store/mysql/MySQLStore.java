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

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import io.github.basilapi.basil.core.ApiInfo;
import io.github.basilapi.basil.doc.Doc;
import io.github.basilapi.basil.search.Query;
import io.github.basilapi.basil.search.Result;
import io.github.basilapi.basil.search.SearchProvider;
import io.github.basilapi.basil.sparql.Specification;
import io.github.basilapi.basil.sparql.SpecificationFactory;
import io.github.basilapi.basil.sparql.UnknownQueryTypeException;
import io.github.basilapi.basil.store.Store;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.jena.query.QueryException;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.basilapi.basil.view.Engine;
import io.github.basilapi.basil.view.View;
import io.github.basilapi.basil.view.Views;

public class MySQLStore implements Store, SearchProvider {

	private static final Logger log = LoggerFactory.getLogger(MySQLStore.class);
	private String jdbcUri;

	static final String SPEC_ENDPOINT = "spec:endpoint";
	static final String SPEC_QUERY = "spec:query";
	static final String DOC_NAME = "doc:name";
	static final String DOC_DESCRIPTION = "doc:description";
	static final String SPEC_EXPANDED_QUERY = "spec:expanded-query";
	static final String AUTH_USER = "auth:user";
	static final String AUTH_PASSWORD = "auth:password";

	public MySQLStore(String jdbcUri) {
		this.jdbcUri = jdbcUri;
		try {
			this._migrate_0_3__0_4_0();
		} catch (IOException e) {
			log.error("FATAL: Upgrade 0.3 -> 0.4.0 failed", e);
		}

	}

	private void _migrate_0_3__0_4_0() throws IOException {
		// XXX
		// Add expanded queries when not already there
		String q = "SELECT API, VALUE FROM DATA WHERE PROPERTY = 'spec:query' AND NOT EXISTS (SELECT * FROM DATA WHERE PROPERTY = 'spec:expanded-query')";
		Map<Integer, String> toUpgrade = new HashMap<Integer, String>();
		try {
			Class.forName("com.mysql.jdbc.Driver");
			try (Connection connect = DriverManager.getConnection(jdbcUri);
					PreparedStatement stmt = connect.prepareStatement(q)) {
				ResultSet rs = stmt.executeQuery();
				while (rs.next()) {
					toUpgrade.put(rs.getInt(1), rs.getString(2));
				}
			} catch (Exception e) {
				throw new IOException(e);
			}
		} catch (ClassNotFoundException e) {
			throw new IOException(e);
		}
		log.warn("To Upgrade: {}", toUpgrade.size());
		try {
			Class.forName("com.mysql.jdbc.Driver");
			try (Connection connect = DriverManager.getConnection(jdbcUri)) {
				try (PreparedStatement stmt = connect.prepareStatement(
						"INSERT INTO DATA (API, PROPERTY, VALUE) VALUES (?,?,?) ON DUPLICATE KEY UPDATE VALUE = VALUES(VALUE);")) {
					connect.setAutoCommit(false);
					Iterator<Entry<Integer, String>> it = toUpgrade.entrySet().iterator();
					while (it.hasNext()) {
						Entry<Integer, String> api = it.next();
						stmt.setInt(1, api.getKey());
						stmt.setString(2, SPEC_EXPANDED_QUERY);
						log.debug(" UPDATE ---> {}", api.getValue());
						try {
							org.apache.jena.query.Query qq = QueryFactory.create(api.getValue());
							qq.setPrefixMapping(null);
							stmt.setString(3, qq.toString());
							stmt.execute();
						} catch (Exception e) {
							log.error("Update failed for api {} {}", new Object[] { api.getKey(), api.getValue() });
						}
					}
					connect.commit();
				} catch (Exception e) {
					throw new IOException(e);
				} finally {
					connect.setAutoCommit(true);
				}
			} catch (Exception e) {
				throw new IOException(e);
			}
			log.warn("Upgrade completed.");
		} catch (ClassNotFoundException e) {
			throw new IOException(e);
		}

	}

	/**
	 * Gets a API Db identifier from a nickname
	 * 
	 * @param nickname
	 * @return int - The Db Id of the API
	 */
	private int _dbId(String nickname) throws IOException {
		String q = "SELECT ID FROM APIS WHERE NICKNAME = ? LIMIT 1;";
		int dbId = 0;
		try {
			Class.forName("com.mysql.jdbc.Driver");
			try (Connection connect = DriverManager.getConnection(jdbcUri);
					PreparedStatement stmt = connect.prepareStatement(q)) {
				stmt.setString(1, nickname);
				ResultSet rs = stmt.executeQuery();
				if (rs.next()) {
					dbId = rs.getInt(1);
				}
			} catch (Exception ignore) {
			}
		} catch (ClassNotFoundException e) {
			throw new IOException(e);
		}
		return dbId;
	}

	private int _createDbId(String nickname) throws IOException {
		String q = "INSERT INTO APIS (NICKNAME) VALUES (?);";
		long dbId = 0;
		try {
			Class.forName("com.mysql.jdbc.Driver");
			try (Connection connect = DriverManager.getConnection(jdbcUri);
					PreparedStatement stmt = connect.prepareStatement(q, Statement.RETURN_GENERATED_KEYS)) {
				stmt.setString(1, nickname);
				int affectedRows = stmt.executeUpdate();

				if (affectedRows == 0) {
					throw new SQLException("Creating user failed, no affected rows.");
				}
				try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						dbId = generatedKeys.getInt(1);
					} else {
						throw new SQLException("Creating user failed, no ID obtained.");
					}
				}
			} catch (Exception e) {
				throw e;
			}
		} catch (Exception e) {
			throw new IOException(e);
		}
		return (int) dbId;
	}

	private void _saveData(String id, Map<String, String> data) throws IOException {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			try (Connection connect = DriverManager.getConnection(jdbcUri)) {
				connect.setAutoCommit(false);
				try {
					int dbId = _dbId(id);
					if (dbId == 0) {
						// Let's create it first
						dbId = _createDbId(id);
					}
					String q = "INSERT INTO DATA (API, PROPERTY, VALUE) VALUES (?,?,?) ON DUPLICATE KEY UPDATE VALUE = VALUES(VALUE);";
					for (Entry<String, String> entry : data.entrySet()) {
						try (PreparedStatement stmt = connect.prepareStatement(q)) {
							stmt.setInt(1, dbId);
							stmt.setString(2, entry.getKey());
							stmt.setString(3, entry.getValue());
							stmt.executeUpdate();
						}
					}
					q = "UPDATE APIS SET MODIFIED = NOW() WHERE ID = ?";
					try (PreparedStatement stmt = connect.prepareStatement(q)) {
						stmt.setInt(1, dbId);
						stmt.executeUpdate();
					}
					connect.commit();
				} catch (IOException e) {
					connect.rollback();
				} finally {
					connect.setAutoCommit(true);
				}
			}
		} catch (ClassNotFoundException | SQLException e) {
			throw new IOException(e);
		}
	}

	private Map<String, String> _loadData(String id) throws IOException {
		int dbId = _dbId(id);
		String q = "SELECT PROPERTY, VALUE FROM DATA WHERE API = ?;";
		Map<String, String> data = new HashMap<String, String>();
		try {
			Class.forName("com.mysql.jdbc.Driver");
			try (Connection connect = DriverManager.getConnection(jdbcUri)) {

				try (PreparedStatement stmt = connect.prepareStatement(q)) {
					stmt.setInt(1, dbId);
					ResultSet rs = stmt.executeQuery();
					while (rs.next()) {
						data.put(rs.getString(1), rs.getString(2));
					}
				}
			}
		} catch (ClassNotFoundException | SQLException e) {
			throw new IOException(e);
		}
		return data;
	}

	private void _deleteData(String id, String... properties) throws IOException {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			try (Connection connect = DriverManager.getConnection(jdbcUri)) {
				connect.setAutoCommit(false);
				try {
					int dbId = _dbId(id);
					if (dbId == 0) {
						// Let's create it first
						dbId = _createDbId(id);
					}
					String q = "DELETE FROM DATA WHERE PROPERTY = ? AND API = ?";
					for (String property : properties) {
						try (PreparedStatement stmt = connect.prepareStatement(q)) {
							stmt.setString(1, property);
							stmt.setInt(2, dbId);
							stmt.executeUpdate();
						}
					}
					connect.commit();
				} finally {
					connect.setAutoCommit(true);
				}
			}
		} catch (ClassNotFoundException | SQLException | IOException e) {
			throw new IOException(e);
		}
	}

	private void _deleteDbId(String id) throws IOException {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			try (Connection connect = DriverManager.getConnection(jdbcUri)) {

				String q = "DELETE FROM APIS WHERE NICKNAME = ?";

				try (PreparedStatement stmt = connect.prepareStatement(q)) {
					stmt.setString(1, id);
					stmt.executeUpdate();
				}
			}
		} catch (ClassNotFoundException | SQLException e) {
			throw new IOException(e);
		}
	}

	@Override
	public void saveSpec(String id, Specification spec) throws IOException {
		Map<String, String> data = new HashMap<String, String>();
		data.put(SPEC_ENDPOINT, spec.getEndpoint());
		data.put(SPEC_QUERY, spec.getQuery());
		// Add a copy as expanded query

		String expandedQuery = null;
		try {
			org.apache.jena.query.Query q = QueryFactory.create(spec.getQuery());
			q.setPrefixMapping(null);
			expandedQuery = q.toString();
		} catch (QueryException qe) {
			// may be update
			try {
				UpdateRequest q = UpdateFactory.create(spec.getQuery());
				q.setPrefixMapping(null);
				expandedQuery = q.toString();
			} catch (QueryException qe2) {
				// some parameterized queries are not supported by the SPARQL 1.1 parser (e.g.
				// Insert data { ?_uri ...)
				// In those cases we just keep the original syntax
				expandedQuery = spec.getQuery();
			}

		}
		data.put(SPEC_EXPANDED_QUERY, expandedQuery);
		_saveData(id, data);
	}

	@Override
	public Specification loadSpec(String id) throws IOException {
		Map<String, String> data = _loadData(id);
		try {
			return SpecificationFactory.create(data.get(SPEC_ENDPOINT), data.get(SPEC_QUERY));
		} catch (UnknownQueryTypeException e) {
			throw new IOException(e);
		}
	}

	@Override
	public boolean existsSpec(String id) {
		try {
			return (_dbId(id) != 0);
		} catch (IOException e) {
			// FIXME Nothing to do here?
			return false;
		}
	}

	@Override
	public List<String> listSpecs() throws IOException {
		String q = "SELECT NICKNAME FROM APIS ORDER BY CREATED DESC;";
		List<String> data = new ArrayList<String>();
		try {
			Class.forName("com.mysql.jdbc.Driver");
			try (Connection connect = DriverManager.getConnection(jdbcUri)) {
				try (PreparedStatement stmt = connect.prepareStatement(q)) {
					ResultSet rs = stmt.executeQuery();
					while (rs.next()) {
						data.add(rs.getString(1));
					}
				}
			}
		} catch (ClassNotFoundException | SQLException e) {
			throw new IOException(e);
		}
		return data;
	}

	@Override
	public Views loadViews(String id) throws IOException {
		String q = "SELECT VIEW, LANGUAGE, TYPE, TEMPLATE, VIEWS.CREATED as CREATED, VIEWS.MODIFIED AS MODIFIED FROM VIEWS, APIS WHERE VIEWS.API = APIS.ID AND APIS.NICKNAME = ? ORDER BY VIEWS.MODIFIED DESC, VIEWS.CREATED DESC;";
		Views views = new Views();
		try {
			Class.forName("com.mysql.jdbc.Driver");
			try (Connection connect = DriverManager.getConnection(jdbcUri)) {

				try (PreparedStatement stmt = connect.prepareStatement(q)) {
					stmt.setString(1, id);
					ResultSet rs = stmt.executeQuery();
					while (rs.next()) {
						views.put(rs.getString("TYPE"), rs.getString("VIEW"), rs.getString("TEMPLATE"),
								Engine.byContentType(rs.getString("LANGUAGE")));
					}
				}
			}
		} catch (ClassNotFoundException | SQLException e) {
			throw new IOException(e);
		}
		return views;
	}

	@Override
	public Doc loadDoc(String id) throws IOException {
		Map<String, String> data = _loadData(id);
		Doc doc = new Doc();
		doc.set(Doc.Field.NAME, data.get(DOC_NAME));
		doc.set(Doc.Field.DESCRIPTION, data.get(DOC_DESCRIPTION));
		return doc;
	}

	@Override
	public void saveViews(String id, Views views) throws IOException {
		/**
		 * FIXME This method is very inefficient... It does not harm for the moment but
		 * we should change it. - enridaga
		 */
		try {
			Class.forName("com.mysql.jdbc.Driver");

			try (Connection connect = DriverManager.getConnection(jdbcUri)) {
				connect.setAutoCommit(false);
				try {
					int dbId = _dbId(id);
					if (dbId == 0) {
						// Let's create it first
						dbId = _createDbId(id);
					}
					// delete views not in
					String d = "DELETE FROM VIEWS WHERE API = " + dbId;
					try (PreparedStatement stmt = connect.prepareStatement(d)) {
						// stmt.setInt(1, dbId);
						stmt.executeUpdate();
					}
					String q = "INSERT INTO VIEWS (API, VIEW, LANGUAGE, TYPE, TEMPLATE ) VALUES (?,?,?,?,?) ON DUPLICATE "
							+ "KEY UPDATE LANGUAGE = VALUES(LANGUAGE), TYPE = VALUES(TYPE), TEMPLATE = VALUES(TEMPLATE), MODIFIED = NOW();";
					try (PreparedStatement stmt = connect.prepareStatement(q)) {
						for (String name : views.getNames()) {
							stmt.setInt(1, dbId);
							stmt.setString(2, name);
							View v = views.byName(name);
							stmt.setString(3, v.getEngine().getContentType());
							stmt.setString(4, v.getMimeType());
							stmt.setString(5, v.getTemplate());
							stmt.executeUpdate();
						}
					}
					connect.commit();
				} catch (IOException e) {
					connect.rollback();
				} finally {
					connect.setAutoCommit(true);
				}
			}
		} catch (ClassNotFoundException | SQLException e) {
			throw new IOException(e);
		}
	}

	@Override
	public void saveDoc(String id, Doc doc) throws IOException {
		Map<String, String> data = new HashMap<String, String>();
		data.put(DOC_NAME, doc.get(Doc.Field.NAME));
		data.put(DOC_DESCRIPTION, doc.get(Doc.Field.DESCRIPTION));
		_saveData(id, data);
	}

	@Override
	public boolean deleteDoc(String id) throws IOException {
		_deleteData(id, DOC_NAME, DOC_DESCRIPTION);
		return true;
	}

	@Override
	public boolean deleteSpec(String id) throws IOException {
		// This deletes the API as a whole
		_deleteDbId(id);
		return true;
	}

	private String _buildSearchQuery(Query query, boolean onlyIds) {
		StringBuilder qb = new StringBuilder();
		qb.append("SELECT APIS.NICKNAME");
		if (!onlyIds)
			qb.append(", DATA.PROPERTY, DATA.VALUE ");

		qb.append(" FROM APIS ");

		// Endpoint
		if (query.getEndpoint() != null) {
			qb.append(" INNER JOIN DATA AS E ON APIS.ID=E.API AND E.PROPERTY = ");
			qb.append("'");
			qb.append(SPEC_ENDPOINT);
			qb.append("' AND E.VALUE = ? ");
		}

		// Namespaces
		if (query.getNamespaces() != null || query.getResources() != null) {
			qb.append(" INNER JOIN DATA AS E ON APIS.ID=E.API AND E.PROPERTY = ");
			qb.append("'");
			qb.append(SPEC_EXPANDED_QUERY);
			qb.append("'");
			if (query.getNamespaces() != null) {
				for (int i = 0; i < query.getNamespaces().length; i++) {
					qb.append(" AND E.VALUE LIKE ? ");
				}
			}
			if (query.getResources() != null) {
				for (int i = 0; i < query.getResources().length; i++) {
					qb.append(" AND E.VALUE LIKE ? ");
				}
			}
		}

		qb.append(" INNER JOIN DATA ON APIS.ID=DATA.API");
		String txt = query.getText();
		String[] txts = txt.split(" ");
		for (int i = 0; i < txts.length; i++) {
			qb.append(" AND DATA.VALUE LIKE ? ");
		}
		return qb.toString();
	}

	private void _mapSearchParameters(PreparedStatement stmt, Query query) throws SQLException {
		String[] txts = query.getText().split(" ");
		int pos = 1;

		// Endpoint
		if (query.getEndpoint() != null) {
			stmt.setString(pos, query.getEndpoint());
		}
		// Namespaces
		if (query.getNamespaces() != null) {
			for (String t : query.getNamespaces()) {
				stmt.setString(pos, new StringBuilder().append("%<").append(t).append("%").toString());
				pos++;
			}
		}
		// Resources
		if (query.getResources() != null) {
			for (String t : query.getResources()) {
				stmt.setString(pos, new StringBuilder().append("%<").append(t).append(">%").toString());
				pos++;
			}
		}

		// Text
		for (String t : txts) {
			stmt.setString(pos, new StringBuilder().append("%").append(t).append("%").toString());
			pos++;
		}
	}

	@Override
	public List<String> search(Query query) throws IOException {
		List<String> results = new ArrayList<String>();
		String q = this._buildSearchQuery(query, true);
		try {
			Class.forName("com.mysql.jdbc.Driver");
			try (Connection connect = DriverManager.getConnection(jdbcUri)) {
				try (PreparedStatement stmt = connect.prepareStatement(q)) {
					this._mapSearchParameters(stmt, query);
					ResultSet r = stmt.executeQuery();
					while (r.next()) {
						String id = r.getString(1);
						if (!results.contains(id))
							results.add(id);
					}
				}
			}
		} catch (ClassNotFoundException e) {
			throw new IOException(e);
		} catch (SQLException e) {
			log.error("SQL State: {}", e.getSQLState());
			throw new IOException(e);
		}
		return results;
	}

	@Override
	public Collection<Result> contextSearch(Query query) throws IOException {
		Map<String, Result> results = new HashMap<String, Result>();
		String q = this._buildSearchQuery(query, false);
		try {
			Class.forName("com.mysql.jdbc.Driver");
			try (Connection connect = DriverManager.getConnection(jdbcUri)) {
				try (PreparedStatement stmt = connect.prepareStatement(q)) {
					this._mapSearchParameters(stmt, query);
					ResultSet r = stmt.executeQuery();
					while (r.next()) {
						if (!results.containsKey(r.getString(1))) {
							results.put(r.getString(1), new ResultSetResult(r.getString(1)));
						}
						((ResultSetResult) results.get(r.getString(1))).put(r.getString(2), r.getString(3));
					}
				}
			}
		} catch (ClassNotFoundException e) {
			throw new IOException(e);
		} catch (SQLException e) {
			log.error("SQL State: {}", e.getSQLState());
			throw new IOException(e);
		}
		return results.values();
	}

	@Override
	public Date created(String id) throws IOException {
		long created = -1;
		try {
			Class.forName("com.mysql.jdbc.Driver");
			try (Connection connect = DriverManager.getConnection(jdbcUri)) {
				String q = "SELECT CREATED FROM APIS WHERE NICKNAME = ?";
				try (PreparedStatement stmt = connect.prepareStatement(q)) {
					stmt.setString(1, id);
					ResultSet s = stmt.executeQuery();
					while (s.next()) {
						created = s.getLong(1);
					}
				}
			}
			return new Date(created);
		} catch (ClassNotFoundException | SQLException e) {
			throw new IOException(e);
		}
	}

	@Override
	public Date modified(String id) throws IOException {
		long created = -1;
		try {
			Class.forName("com.mysql.jdbc.Driver");
			try (Connection connect = DriverManager.getConnection(jdbcUri)) {
				String q = "SELECT MODIFIED FROM APIS WHERE NICKNAME = ?";
				try (PreparedStatement stmt = connect.prepareStatement(q)) {
					stmt.setString(1, id);
					ResultSet s = stmt.executeQuery();
					while (s.next()) {
						created = s.getLong(1);
					}
				}
			}
			return new Date(created);
		} catch (ClassNotFoundException | SQLException e) {
			throw new IOException(e);
		}
	}

	@Override
	public String[] credentials(String id) throws IOException {
		String username = null;
		String password = null;
		try {
			Class.forName("com.mysql.jdbc.Driver");
			try (Connection connect = DriverManager.getConnection(jdbcUri)) {
				String q = "SELECT DATA.PROPERTY, DATA.VALUE FROM APIS JOIN DATA ON DATA.API = APIS.ID AND DATA.PROPERTY IN ('"
						+ AUTH_USER + "','" + AUTH_PASSWORD + "') WHERE APIS.NICKNAME = ?";
				try (PreparedStatement stmt = connect.prepareStatement(q)) {
					stmt.setString(1, id);
					ResultSet s = stmt.executeQuery();
					while (s.next()) {
						if (s.getString(1).equals(AUTH_USER)) {
							username = s.getString(2);
						} else if (s.getString(1).equals(AUTH_PASSWORD)) {
							password = s.getString(2);
						} else
							throw new IOException("This should never happen");
					}
				}
			}
			if (username == null || password == null) {
				// no credentials
				return null;
			}
			return new String[] { username, password };
		} catch (ClassNotFoundException | SQLException e) {
			throw new IOException(e);
		}
	}

	@Override
	public void saveCredentials(String id, String user, String password) throws IOException {
		Map<String, String> data = new HashMap<String, String>();
		data.put(AUTH_USER, user);
		data.put(AUTH_PASSWORD, password);
		_saveData(id, data);
	}

	@Override
	public void deleteCredentials(String id) throws IOException {
		_deleteData(id, AUTH_USER, AUTH_PASSWORD);
	}

	@Override
	public ApiInfo info(String id) throws IOException {
		try {
			ApiInfo apiInfo = null;
			Class.forName("com.mysql.jdbc.Driver");
			try (Connection connect = DriverManager.getConnection(jdbcUri)) {
				String q = "SELECT DATA.VALUE, APIS.CREATED, APIS.MODIFIED FROM APIS LEFT JOIN DATA ON DATA.API = APIS.ID AND DATA.PROPERTY='doc:name' WHERE APIS.NICKNAME = ?";
				try (PreparedStatement stmt = connect.prepareStatement(q)) {
					stmt.setString(1, id);
					ResultSet s = stmt.executeQuery();
					while (s.next()) {
						final String name = s.getString(1);
						final Timestamp created = s.getTimestamp(2);
						final Timestamp modified = s.getTimestamp(3);
						apiInfo = new ApiInfo() {
							@Override
							public Date created() {
								return new Date(created.getTime());
							}

							@Override
							public String getId() {
								return id;
							}

							@Override
							public String getName() {
								return name == null ? "" : name;
							}

							public Date modified() {
								if (modified == null) {
									return new Date(created.getTime());
								}
								return new Date(modified.getTime());
							};

							public Set<String> alias() {
								try {
									return MySQLStore.this.loadAlias(id);
								} catch (IOException e) {
									log.error("", e);
									return Collections.emptySet();
								}
							};
						};
					}
				}
			}
			return apiInfo;
		} catch (ClassNotFoundException | SQLException e) {
			throw new IOException(e);
		}

	}

	public List<ApiInfo> list() throws IOException {
		try {
			List<ApiInfo> apiInfo = new ArrayList<ApiInfo>();
			Class.forName("com.mysql.jdbc.Driver");
			try (Connection connect = DriverManager.getConnection(jdbcUri)) {
				String q = "SELECT DATA.VALUE, APIS.CREATED, APIS.MODIFIED, APIS.NICKNAME FROM APIS INNER JOIN DATA ON DATA.API = APIS.ID AND DATA.PROPERTY='doc:name' ORDER BY APIS.MODIFIED DESC,APIS.CREATED DESC;";
				try (PreparedStatement stmt = connect.prepareStatement(q)) {
					ResultSet s = stmt.executeQuery();
					while (s.next()) {
						final String name = s.getString(1);
						final Timestamp created = s.getTimestamp(2);
						final Timestamp modified = s.getTimestamp(3);
						final String nickname = s.getString(4);
						apiInfo.add(new ApiInfo() {
							@Override
							public Date created() {
								return new Date(created.getTime());
							}

							@Override
							public String getId() {
								return nickname;
							}

							@Override
							public String getName() {
								return name;
							}

							public Date modified() {
								return new Date(modified.getTime());
							};

							@Override
							public Set<String> alias() {
								try {
									return MySQLStore.this.loadAlias(nickname);
								} catch (IOException e) {
									return Collections.emptySet();
								}
							}
						});
					}
				}
			}
			return apiInfo;
		} catch (ClassNotFoundException | SQLException e) {
			throw new IOException(e);
		}
	};

	@Override
	public String getIdByAlias(String alias) throws IOException {
		log.trace("get id by alias: {}", alias);
		try {
			String q = "SELECT APIS.NICKNAME FROM ALIAS INNER JOIN APIS ON ALIAS.API = APIS.ID WHERE ALIAS.ALIAS = ? LIMIT 1";
			Class.forName("com.mysql.jdbc.Driver");
			try (Connection connect = DriverManager.getConnection(jdbcUri)) {
				try (PreparedStatement stmt = connect.prepareStatement(q)) {
					stmt.setString(1, alias);
					ResultSet s = stmt.executeQuery();
					while (s.next()) {
						return s.getString(1);
					}
				}
			}
			throw new IOException("Not Found");
		} catch (ClassNotFoundException | SQLException e) {
			throw new IOException(e);
		}
	}

	@Override
	public Set<String> loadAlias(String id) throws IOException {
		log.trace("load alias: {}", id);
		try {
			String q = "SELECT ALIAS FROM ALIAS WHERE API = ?";
			int dbId = _dbId(id);
			Set<String> alias = new HashSet<String>();
			Class.forName("com.mysql.jdbc.Driver");
			try (Connection connect = DriverManager.getConnection(jdbcUri)) {
				try (PreparedStatement stmt = connect.prepareStatement(q)) {
					stmt.setInt(1, dbId);
					ResultSet s = stmt.executeQuery();
					while (s.next()) {
						alias.add(s.getString(1));
					}
				}
			}
			return Collections.unmodifiableSet(alias);
		} catch (ClassNotFoundException | SQLException e) {
			throw new IOException(e);
		}
	}

	@Override
	public void saveAlias(String id, Set<String> alias) throws IOException {
		log.trace("save alias {}: {}", id, alias);
		try {
			int intId = _dbId(id);
			String d = "DELETE FROM ALIAS WHERE API = ?";
			String i = "INSERT INTO ALIAS (API, ALIAS) VALUES (?,?)";
			Class.forName("com.mysql.jdbc.Driver");
			try (Connection connect = DriverManager.getConnection(jdbcUri)) {
				boolean acs = connect.getAutoCommit();
				connect.setAutoCommit(false);
				try (PreparedStatement stmtd = connect.prepareStatement(d);
						PreparedStatement stmti = connect.prepareStatement(i)) {
					stmtd.setInt(1, intId);
					boolean dr = stmtd.execute();
					log.trace("delete alias {}: {}", id, dr);
					Iterator<String> aliasi = alias.iterator();
					while (aliasi.hasNext()) {
						stmti.setInt(1, intId);
						stmti.setString(2, aliasi.next());
						boolean ir = stmti.execute();
						log.trace("insert alias {}: {}", id, ir);
					}
				}
				connect.commit();
				connect.setAutoCommit(acs);
			}
		} catch (ClassNotFoundException | SQLException e) {
			throw new IOException(e);
		}
	}

	class ResultSetResult implements Result {

		private String id;
		private Map<String, String> context;
		private int hashCode;

		public ResultSetResult(String id) {
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
