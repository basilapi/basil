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

package io.github.basilapi.basil.rest.auth;

import java.nio.charset.Charset;
import java.util.Base64;

import javax.ws.rs.core.HttpHeaders;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * To check for HTTP BASIC Auth credentials
 * This authentication is stateless, i.e. it does not starts a session.
 * 
 * @author enridaga
 *
 */
public class StatelessBasicAuth {

	private static Logger log = LoggerFactory.getLogger(StatelessBasicAuth.class);

	public boolean authenticate(HttpHeaders httpHeaders) {
		return login(SecurityUtils.getSubject(), httpHeaders);
	}
	

	public boolean login(Subject subject, HttpHeaders httpHeaders) {
		log.trace("Attempting basic authentication (stateless)");
		final String authorization = httpHeaders.getHeaderString("Authorization");
		if (authorization != null && authorization.startsWith("Basic")) {
			log.trace("Using HTTP Authorization Header");
			// Authorization: Basic base64credentials
			String base64Credentials = authorization.substring("Basic".length()).trim();
			String credentials = new String(Base64.getDecoder().decode(base64Credentials), Charset.forName("UTF-8"));
			// credentials = username:password
			final String[] values = credentials.split(":", 2);
			UsernamePasswordToken token = new UsernamePasswordToken(values[0], values[1]);
			try {
				subject.login(token);
				Session session = subject.getSession();
	            session.setAttribute(AuthResource.CURRENT_USER_KEY, values[0]);
				log.debug("OK");
				return true;
			} catch (IncorrectCredentialsException | UnknownAccountException ice) {
				log.trace("Authentication failed (stateless): {}", ice.getMessage());
			}
		}
		log.debug("FAILED");
		return false;
	}
}
