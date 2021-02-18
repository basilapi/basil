package uk.ac.open.kmi.basil.rendering;

import java.io.IOException;

import javax.ws.rs.core.MediaType;

/**
 * No available rendering for kind of object and mime type
 * 
 * @author enridaga
 *
 */
public class CannotRenderException extends Exception {

	public CannotRenderException() {
		super("Cannot render.");
	}

	public CannotRenderException(IOException e) {
		super(e);
	}

	public CannotRenderException(MediaType type) {
		super("Cannot render " + type.toString());
	}
	
	public CannotRenderException(Object o, MediaType type) {
		super("Cannot render " + o.getClass() + " with type " + type.toString());
	}

	public CannotRenderException(String string) {
		super(string);
	}

	public CannotRenderException(String string, Throwable e) {
		super(string, e);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
