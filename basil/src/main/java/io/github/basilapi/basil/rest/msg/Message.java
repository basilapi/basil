package io.github.basilapi.basil.rest.msg;

public interface Message {
	void setText(String text);

	void setLocation(String location);

	void setError(Exception e);

	boolean isError();

	boolean hasLocation();
	
	String getText();
	
	String getLocation();

	String asTXT();
	
	String asJSON();
}
