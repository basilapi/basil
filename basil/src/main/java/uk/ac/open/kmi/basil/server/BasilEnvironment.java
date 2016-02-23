package uk.ac.open.kmi.basil.server;

import uk.ac.open.kmi.basil.invoke.QueryExecutor;

public interface BasilEnvironment {

	String getJdbcConnectionUrl();

	Class<? extends QueryExecutor> getQueryExecutorClass() throws ClassNotFoundException;
}
