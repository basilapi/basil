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

import com.wordnik.swagger.jersey.listing.ApiListingResourceJSON;
import com.wordnik.swagger.jersey.listing.JerseyApiDeclarationProvider;
import com.wordnik.swagger.jersey.listing.JerseyResourceListingProvider;

import uk.ac.open.kmi.basil.alias.AliasMemCache;
import uk.ac.open.kmi.basil.core.auth.JDBCUserManager;
import uk.ac.open.kmi.basil.invoke.DirectExecutor;
import uk.ac.open.kmi.basil.invoke.QueryExecutor;
import uk.ac.open.kmi.basil.mysql.MySQLStore;
import uk.ac.open.kmi.basil.server.BasilEnvironment;

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
		MySQLStore store = new MySQLStore(environment.getJdbcConnectionUrl());
		ctx.setAttribute(Registry.Store, store);
		ctx.setAttribute(Registry.SearchProvider, store);
		ctx.setAttribute(Registry.AliasCache, new AliasMemCache(10000));
		QueryExecutor exec;
		try {
			exec = environment.getQueryExecutorClass().newInstance();
			log.info("Setting query executor: {}", exec.getClass());
		} catch (InstantiationException |IllegalAccessException |ClassNotFoundException e) {
			//throw new RuntimeException(e);
			log.error("Cannot set the query executor, falling back to default executor.", e);
			 exec = new DirectExecutor();
		}
		ctx.setAttribute(Registry.QueryExecutor,exec);
	}

	public final static class Registry {
		public static final String QueryExecutor = "_QueryExecutor";
		public final static String Store = "_Store";
		public final static String Environment ="_Environment";
		public final static String UserManager = "_UserManager";
		public final static String JdbcUri = "_JdbcUri";
		public final static String SearchProvider = "_SearchProvider";
		public static final String AliasCache = "_AliasCache";
	}
}
