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

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import io.github.basilapi.basil.alias.AliasMemCache;
import io.github.basilapi.basil.store.mysql.JDBCUserManager;
import io.github.basilapi.basil.invoke.DirectExecutor;
import io.github.basilapi.basil.invoke.QueryExecutor;
import io.github.basilapi.basil.store.mysql.MySQLStore;
import org.glassfish.jersey.server.ResourceConfig;
import org.secnod.shiro.jersey.AuthInjectionBinder;
import org.secnod.shiro.jersey.AuthorizationFilterFeature;
import org.secnod.shiro.jersey.SubjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
				packages("io.github.basilapi.basil.rest").
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
