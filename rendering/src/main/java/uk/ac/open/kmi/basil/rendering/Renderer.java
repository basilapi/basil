package uk.ac.open.kmi.basil.rendering;

import java.io.InputStream;

import javax.ws.rs.core.MediaType;

public interface Renderer<T> {

	public InputStream stream(T o, MediaType type) throws CannotRenderException;
	
	public String render(T o, MediaType type) throws CannotRenderException;
}
