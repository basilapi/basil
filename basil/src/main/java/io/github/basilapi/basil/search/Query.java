package uk.ac.open.kmi.basil.search;

public interface Query {
	public void setText(String text);

	public String getText();

	public String getEndpoint();

	public String[] getNamespaces();

	public String[] getResources();

	public void setEndpoint(String endpoint);

	public void setNamespaces(String... nss);

	public void setResources(String... rss);
}
