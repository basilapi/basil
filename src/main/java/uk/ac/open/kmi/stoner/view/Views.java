package uk.ac.open.kmi.stoner.view;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class Views implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1537650181054324254L;

	private Map<String, View> views = new HashMap<String, View>();

	public void put(String mimeType, String name, String template, Engine engine) {
		View view = new View();
		view.setExtension(name);
		view.setMimeType(mimeType);
		view.setTemplate(template, engine);
		views.put(name, view);
	}

	public View byName(String name) {
		return views.get(name);
	}

	public boolean exists(String name) {
		return views.containsKey(name);
	}

	public Set<View> byMimeType(String mimeType) {
		Set<View> viewset = new HashSet<View>();
		for (Entry<String, View> e : views.entrySet()) {
			if (e.getValue().getMimeType().equals(mimeType)) {
				viewset.add(e.getValue());
			}
		}
		return Collections.unmodifiableSet(viewset);
	}

	public void remove(View view) {
		views.remove(view.getName());
	}

	public void remove(String name) {
		views.remove(name);
	}

	public boolean supportsMimeType(String mimeType) {
		for (Entry<String, View> e : views.entrySet()) {
			if (e.getValue().getMimeType().equals(mimeType)) {
				return true;
			}
		}
		return false;
	}

	public Set<String> getMimeTypes() {
		Set<String> mimeTypes = new HashSet<String>();
		for (Entry<String, View> e : views.entrySet()) {
			mimeTypes.add(e.getValue().getMimeType());
		}
		return Collections.unmodifiableSet(mimeTypes);
	}

	public Set<String> getNames() {
		return Collections.unmodifiableSet(views.keySet());
	}

	public int numberOf() {
		return views.size();
	}
}
