package io.github.basilapi.basil.rest.msg;

public class ErrorMessage extends SimpleMessage {

	public ErrorMessage(Exception e) {
		super(e.getMessage());
		setError(e);
	}
	
	public ErrorMessage(String msg) {
		this(new Exception(msg));
	}
}
