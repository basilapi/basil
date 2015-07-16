package uk.ac.open.kmi.basil.rest;

import java.io.File;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.glassfish.jersey.server.ResourceConfig;
import org.secnod.shiro.jersey.AuthInjectionBinder;
import org.secnod.shiro.jersey.AuthorizationFilterFeature;
import org.secnod.shiro.jersey.SubjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.open.kmi.basil.core.auth.JDBCUserManager;
import uk.ac.open.kmi.basil.store.FileStore;

import com.wordnik.swagger.jersey.listing.ApiListingResourceJSON;
import com.wordnik.swagger.jersey.listing.JerseyApiDeclarationProvider;
import com.wordnik.swagger.jersey.listing.JerseyResourceListingProvider;

/**
 * Created by Luca Panziera on 09/01/15.
 */
public class BasilApplication extends ResourceConfig implements ServletContextListener {
	private Logger log = LoggerFactory.getLogger(BasilApplication.class);

    public BasilApplication(){
        packages("com.wordnik.swagger.jaxrs.json").
				packages("uk.ac.open.kmi.basil.rest").
				register(ApiListingResourceJSON.class).
                register(JerseyApiDeclarationProvider.class).
                register(JerseyResourceListingProvider.class);
		register(new AuthorizationFilterFeature());
		register(new SubjectFactory());
		register(new AuthInjectionBinder());
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

		// JDBC setup
		String jdbc_url = ctx.getInitParameter("jdbc-config");
		Registry.JdbcUri = jdbc_url;

		ctx.setAttribute(Registry.UserManager, 	new JDBCUserManager());

	}

	public final static class Registry {
		public final static String Store = "_Store";
		public final static String UserManager = "_UserManager";
		public static String JdbcUri = "_JdbcUri";
	}
}
