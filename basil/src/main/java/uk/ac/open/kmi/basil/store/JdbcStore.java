package uk.ac.open.kmi.basil.store;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import uk.ac.open.kmi.basil.doc.Doc;
import uk.ac.open.kmi.basil.sparql.Specification;
import uk.ac.open.kmi.basil.sparql.SpecificationFactory;
import uk.ac.open.kmi.basil.view.Engine;
import uk.ac.open.kmi.basil.view.View;
import uk.ac.open.kmi.basil.view.Views;

public class JdbcStore implements Store {

	private String jdbcUri;

	static final String SPEC_ENDPOINT = "spec:endpoint";
	static final String SPEC_QUERY = "spec:query";
	static final String DOC_NAME = "doc:name";
	static final String DOC_DESCRIPTION = "doc:description";

	public JdbcStore(String jdbcUri) {
		this.jdbcUri = jdbcUri;
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
					PreparedStatement stmt = connect.prepareStatement(q,
							Statement.RETURN_GENERATED_KEYS)) {
				stmt.setString(1, nickname);
				int affectedRows = stmt.executeUpdate();

				if (affectedRows == 0) {
					throw new SQLException(
							"Creating user failed, no affected rows.");
				}
				try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						dbId = generatedKeys.getInt(1);
					} else {
						throw new SQLException(
								"Creating user failed, no ID obtained.");
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

	private void _saveData(String id, Map<String, String> data)
			throws IOException {
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
						try (PreparedStatement stmt = connect
								.prepareStatement(q)) {
							stmt.setInt(1, dbId);
							stmt.setString(2, entry.getKey());
							stmt.setString(3, entry.getValue());
							stmt.executeUpdate();
						}
					}
					connect.commit();
				} catch(IOException e){
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

	private void _deleteData(String id, String... properties)
			throws IOException {
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
						try (PreparedStatement stmt = connect
								.prepareStatement(q)) {
							stmt.setString(1, property);
							stmt.setInt(1, dbId);
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
		_saveData(id, data);
	}

	@Override
	public Specification loadSpec(String id) throws IOException {
		Map<String, String> data = _loadData(id);
		return SpecificationFactory.create(data.get(SPEC_ENDPOINT),
				data.get(SPEC_QUERY));
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
						views.put(rs.getString("TYPE"), rs.getString("VIEW"),
								rs.getString("TEMPLATE"),
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
		 * FIXME This method is very inefficient...
		 * It does not harm for the moment but we should change it. - enridaga
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
						//stmt.setInt(1, dbId);
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
				}  catch(IOException e){
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

}
