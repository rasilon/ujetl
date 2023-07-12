package com.rasilon.ujetl;

import com.beust.jcommander.*;

/**
 * @author derryh
 * 
 */
public class CopyingAppCommandParser {

	@Parameter(names = {"-config","--config"}, description = "Application Config file for this run")
	private String configFile;
	
	@Parameter(names = {"-log4j","--log4j"}, description = "Log4J config file for this run")
	private String log4jConfigFile = "/etc/ujetl/default_log4j_config.properties";

	public CopyingAppCommandParser(String[] args) {
		super();
		new JCommander(this, args);
	}

	public String getConfigFile() {
		return configFile;
	}

}
