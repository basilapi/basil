package uk.ac.open.kmi.stoner;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.ws.rs.core.Application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.open.kmi.stoner.store.FileStore;

/**
 * Created by Luca Panziera on 09/01/15.
 */
public class Stoner extends Application implements ServletContextListener {
	private Logger log = LoggerFactory.getLogger(Stoner.class);

	@Override
	public Set<Class<?>> getClasses() {
		Set<Class<?>> s = new HashSet<Class<?>>();
		s.add(HelloWorldResource.class);
		s.add(SpecificationResource.class);
		s.add(ApiResource.class);
		s.add(ExplainResource.class);
		s.add(FormatResource.class);
		return s;
	}

	public final static class Registry {
		public final static String Store = "_Store";
	}

	public void contextDestroyed(ServletContextEvent arg0) {

	}

	public void contextInitialized(ServletContextEvent arg0) {
		ServletContext ctx = arg0.getServletContext();
		String h = ctx.getInitParameter("file-store-home");
		File home = new File(h);
		log.info("Preparing file store: {}", home);
		home.mkdirs();
		ctx.setAttribute(Registry.Store, new FileStore(home));
	}
}
