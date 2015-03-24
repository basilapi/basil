package uk.ac.open.kmi.basil.server;

import java.io.File;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.webapp.WebAppContext;

public class Standalone {

	public static void main(String[] args) {
		System.out.println("#1: welcome to the world's helthiest food");
		Server server = new Server();
        ServerConnector connector = new ServerConnector(server);
        // Set some timeout options to make debugging easier.
        connector.setIdleTimeout(1000 * 60 * 60);
        connector.setSoLingerTime(-1);
        connector.setPort(8080);
        server.setConnectors(new Connector[] {connector});
        System.out.println("#2: basil server is starting");
        WebAppContext root = new WebAppContext();
        root.setContextPath("/");
        //System.out.println(Standalone.class.getResource("/"));
        String webxmlLocation = Standalone.class.getResource("/WEB-INF/web.xml").toString();
        root.setDescriptor(webxmlLocation);
        String resLocation = new File(".").getAbsolutePath();
        root.setResourceBase(resLocation);
        root.setParentLoaderPriority(true);
        server.setHandler(root);
        System.out.println("#3: server ready");
        //initWebApp(root);

        try {
            server.start();
            System.out.println("#4: enjoy");
            server.join();
            System.out.println("#5: stopping server");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(100);
        }
        System.out.println("#6: bye bye");
	}

}
