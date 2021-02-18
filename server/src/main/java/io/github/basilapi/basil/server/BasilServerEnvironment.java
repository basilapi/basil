package io.github.basilapi.basil.server;

import org.apache.shiro.config.Ini;
import org.apache.shiro.web.env.IniWebEnvironment;

import io.github.basilapi.basil.invoke.DirectExecutor;
import io.github.basilapi.basil.invoke.QueryExecutor;

public class BasilServerEnvironment extends IniWebEnvironment implements BasilEnvironment {

	private String jdbcConnectionUrl;

	public BasilServerEnvironment() {
		String iniPath = System.getProperty("basil.configurationFile");
		Ini ini = Ini.fromResourcePath(iniPath);
		setIni(ini);

		StringBuilder sb = new StringBuilder().append("jdbc:mysql://").append(getIni().get("").get("ds.serverName"))
				.append(":").append(getIni().get("").get("ds.port")).append("/")
				.append(getIni().get("").get("ds.databaseName")).append("?user=")
				.append(getIni().get("").get("ds.user")).append("&password=")
				.append(getIni().get("").get("ds.password"));

		if(getIni().get("").get("ds.verifyServerCertificate") != null){
			sb.append("&verifyServerCertificate=");
			sb.append(getIni().get("").get("ds.verifyServerCertificate"));
		}
		if(getIni().get("").get("ds.useSSL") != null){
			sb.append("&useSSL=");
			sb.append(getIni().get("").get("ds.useSSL"));
		}
		jdbcConnectionUrl =  sb.toString();
	}

	@Override
	public void setIni(Ini ini) {
		super.setIni(ini);
	}

	public String getJdbcConnectionUrl() {
		return jdbcConnectionUrl;
	}

	@Override
	public Class<? extends QueryExecutor> getQueryExecutorClass() throws ClassNotFoundException {
		String clazz = getIni().get("").get("queryExecutor");
		if (clazz != null) {
			Class<?> cl = this.getClass().getClassLoader().loadClass(clazz);
			if(QueryExecutor.class.isAssignableFrom(cl) ){
				return (Class<? extends QueryExecutor>) cl;
			}else{
				throw new ClassNotFoundException(cl+" is not a QueryExecutor");
			}
		}
		return DirectExecutor.class;
	}
}