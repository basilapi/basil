package io.github.basilapi.basil.view;

public class EngineExecutionException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public EngineExecutionException(Exception wrapped) {
		super(wrapped);
	}

	public EngineExecutionException(String message) {
		super(message);
	}
}
