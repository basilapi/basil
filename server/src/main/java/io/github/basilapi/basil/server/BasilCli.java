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

package io.github.basilapi.basil.server;

import java.io.PrintStream;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class BasilCli {
	private int port = 8080;
	private String[] args;

	private static PrintStream O = System.out;
	private static PrintStream E = System.err;
	
	public int getPort() {
		return port;
	}

	private Options options = new Options();

	public BasilCli(String[] args) {
		this.args = args;
		options.addOption("h", "help", false, "Show this help.");
		options.addOption("p", "port", true, "Set the port the server will listen to (defaults to 8080).");
	}

	/**
	 * Prints help.
	 */
	private void help() {
		String syntax = "java [java-opts] -jar [jarfile] ";
		new HelpFormatter().printHelp(syntax, options);
		System.exit(0);
	}

	/**
	 * Parses command line arguments and acts upon them.
	 */
	public void parse() {
		CommandLineParser parser = new BasicParser();
		CommandLine cmd = null;
		try {
			cmd = parser.parse(options, args);
			if (cmd.hasOption('h'))
				help();
			if (cmd.hasOption('p')) {
				port = Integer.parseInt(cmd.getOptionValue('p'));
				if (port < 0 && port > 65535) {
					O.println("Invalid port number " + port + ". Must be in the range [0,65535].");
					System.exit(100);
				}
			}
		} catch (ParseException e) {
			E.println("Failed to parse comand line properties");
			e.printStackTrace();
			help();
		}
	}
}
