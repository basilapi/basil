package uk.ac.open.kmi.basil.rendering;

import java.io.InputStream;
import java.util.Map;

import javax.ws.rs.core.MediaType;

public abstract class Renderer<T> {
	private T input;

	public Renderer(T input) {
		this.input = input;
	}
	
	protected T getInput(){
		return this.input;
	}

	public abstract InputStream stream(MediaType type, String g, Map<String, String> pref) throws CannotRenderException;

	public abstract String render(MediaType type, String g, Map<String, String> pref) throws CannotRenderException;
}
