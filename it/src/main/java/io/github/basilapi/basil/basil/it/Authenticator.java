package io.github.basilapi.basil.it;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Authenticator {
	private static final Logger log = LoggerFactory.getLogger(Authenticator.class);
	public CloseableHttpResponse authenticate(String serverBaseUrl, String username, String password, HttpClientContext httpClientContext) throws Exception {
		log.trace("Authenticating {}", username);
		HttpPost post = new HttpPost(serverBaseUrl + "/basil/auth/login");
		post.addHeader("Content-type", "application/json");
		BasicHttpEntity e = new BasicHttpEntity();
		e.setContent(IOUtils.toInputStream("{username: \"" + username + "\", password: \""
				+ password + "\"}"));
		post.setEntity(e);
		return HttpClients.createDefault().execute(post, httpClientContext);
	}
}
