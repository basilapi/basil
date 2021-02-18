package uk.ac.open.kmi.basil.search;

import java.util.Arrays;
import java.util.List;

public class SimpleQuery implements Query {

	private String text = "";
	private String endpoint = null;
	private List<String> nss = null;
	private List<String> rss = null;

	@Override
	public void setText(String text) {
		this.text = text;
	}

	@Override
	public String getText() {
		return this.text;
	}

	@Override
	public String getEndpoint() {
		return endpoint;
	}

	@Override
	public String[] getNamespaces() {
		if (nss != null) {
			return nss.toArray(new String[nss.size()]);
		}
		return new String[] {};
	}

	@Override
	public String[] getResources() {
		if (rss != null) {
			return rss.toArray(new String[rss.size()]);
		}
		return new String[] {};
	}

	@Override
	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	@Override
	public void setNamespaces(String... nss) {
		this.nss = Arrays.asList(nss);
	}

	@Override
	public void setResources(String... rss) {
		this.rss = Arrays.asList(rss);
	}
}
