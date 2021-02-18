package io.github.basilapi.basil.server;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.webapp.WebAppContext;

public class Standalone {
	public static void main(String[] args) {
		System.out.println("#1: welcome to the world's helthiest food");
		BasilCli cli = new BasilCli(args);
		cli.parse();
		Server server = new Server();
		ServerConnector connector = new ServerConnector(server);

		connector.setIdleTimeout(1000 * 60 * 60);
		connector.setSoLingerTime(-1);
		connector.setPort(cli.getPort());
		server.setConnectors(new Connector[] { connector });
		System.out.println("#2: basil is starting on port " + cli.getPort());
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
		System.out.println("#3: resources setup");

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
