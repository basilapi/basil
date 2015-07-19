package uk.ac.open.kmi.basil.rest;

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
import uk.ac.open.kmi.basil.server.BasilEnvironment;
import uk.ac.open.kmi.basil.store.JdbcStore;

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
		log.info("Initializing context.");
		ServletContext ctx = arg0.getServletContext();

		String envClass = ctx.getInitParameter("basilEnvironmentClass");
		BasilEnvironment environment;
		try {
			environment = (BasilEnvironment) Class.forName(envClass).newInstance();
		} catch (InstantiationException | IllegalAccessException
				| ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
		// JDBC setup
		ctx.setAttribute(Registry.Environment, environment);
		ctx.setAttribute(Registry.UserManager, 	new JDBCUserManager(environment.getJdbcConnectionUrl()));
		ctx.setAttribute(Registry.Store, new JdbcStore(environment.getJdbcConnectionUrl()));
		

	}

	public final static class Registry {
		public final static String Store = "_Store";
		public final static String Environment ="_Environment";
		public final static String UserManager = "_UserManager";
		public final static String JdbcUri = "_JdbcUri";
	}
}
