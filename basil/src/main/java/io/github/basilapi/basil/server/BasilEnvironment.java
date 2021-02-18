package io.github.basilapi.basil.server;

import io.github.basilapi.basil.invoke.QueryExecutor;

public interface BasilEnvironment {

	String getJdbcConnectionUrl();

	Class<? extends QueryExecutor> getQueryExecutorClass() throws ClassNotFoundException;
}
