package uk.ac.open.kmi.basil.server;

import java.io.PrintStream;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.webapp.WebAppContext;

public class Standalone {
	private static class Cli {

		private String[] args = null;
		private Options options = new Options();

		public Cli(String[] args) {
			this.args = args;
			options.addOption("h", "help", false, "Show this help.");
			options.addOption("p", "port", true,
					"Set the port the server will listen to (defaults to 8080).");
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
						O.println("Invalid port number " + port
								+ ". Must be in the range [0,65535].");
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

	private static PrintStream O = System.out;
	private static PrintStream E = System.err;
	private static int port = 8080;

	public static void main(String[] args) {
		System.out.println("#1: welcome to the world's helthiest food");
		new Cli(args).parse();
		Server server = new Server();
		ServerConnector connector = new ServerConnector(server);

		connector.setIdleTimeout(1000 * 60 * 60);
		connector.setSoLingerTime(-1);
		connector.setPort(port);
		server.setConnectors(new Connector[] { connector });
		System.out.println("#2: basil is starting on port " + port);
		WebAppContext root = new WebAppContext();
		root.setContextPath("/");

		String webxmlLocation = Standalone.class
				.getResource("/WEB-INF/web.xml").toString();
		root.setDescriptor(webxmlLocation);
		
		String resLocation = Standalone.class
				.getResource("/static").toString();
		root.setResourceBase(resLocation);
		root.setParentLoaderPriority(true);
		server.setHandler(root);
		System.out.println("#3: done");

		try {
			server.start();
			System.out.println("#4: enjoy");
			server.join();
			System.out.println("#5: stopping server");
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(100);
		}
		System.out.println("#6: thank you");
	}

}
