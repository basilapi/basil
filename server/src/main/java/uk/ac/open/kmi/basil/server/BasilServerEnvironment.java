package uk.ac.open.kmi.basil.server;

import org.apache.shiro.config.Ini;
import org.apache.shiro.web.env.IniWebEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BasilServerEnvironment extends IniWebEnvironment implements BasilEnvironment {
	
	private String jdbcConnectionUrl;
	private Logger log = LoggerFactory.getLogger(BasilServerEnvironment.class);
	
	public BasilServerEnvironment() {
		String iniPath = System.getProperty("basil.configurationFile");
		Ini ini = Ini.fromResourcePath(iniPath);
		setIni(ini);
		
		jdbcConnectionUrl = new StringBuilder()
		.append("jdbc:mysql://")
		.append(getIni().get("").get("ds.serverName"))
		.append(":")
		.append(getIni().get("").get("ds.port"))
		.append("/")
		.append(getIni().get("").get("ds.databaseName"))
		.append("?user=")
		.append(getIni().get("").get("ds.user"))
		.append("&password=")
		.append(getIni().get("").get("ds.password")).toString();
		//log.trace("jdbc connection url is: {}", jdbcConnectionUrl);
	}
	
	@Override
	public void setIni(Ini ini) {
		super.setIni(ini);
	}
	
	public String getJdbcConnectionUrl(){
		return jdbcConnectionUrl;
	}
}
