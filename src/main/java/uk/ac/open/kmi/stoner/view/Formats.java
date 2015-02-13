package uk.ac.open.kmi.stoner.view;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Formats implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1537650181054324254L;

	private Map<String, Format> mimeTypeFormats = new HashMap<String, Format>();
	private Map<String, String> extensionMimeType = new HashMap<String, String>();

	public void put(String mimeType, String extension, String template,
			Engine engine) {
		Format format = new Format();
		format.setExtension(extension);
		format.setMimeType(mimeType);
		format.setTemplate(template, engine);
		mimeTypeFormats.remove(mimeType);
		extensionMimeType.remove(extension);
		extensionMimeType.put(extension, mimeType);
		mimeTypeFormats.put(mimeType, format);
	}

	public Format byExtension(String extension) {
		return mimeTypeFormats.get(extensionMimeType.get(extension));
	}

	public Format byMimeType(String mimeType) {
		return mimeTypeFormats.get(mimeType);
	}

	public void remove(Format format) {
		if (mimeTypeFormats.values().contains(format)) {
			mimeTypeFormats.remove(format.getMimeType());
		}
	}

	public void removeMimeType(String mimeType) {
		extensionMimeType.remove(mimeTypeFormats.get(mimeType).getExtension());
		mimeTypeFormats.remove(mimeType);
	}

	public void removeExtension(String extension) {
		mimeTypeFormats.remove(extensionMimeType.get(extension));
		extensionMimeType.remove(extension);
	}

	public boolean supportsExtension(String extension) {
		return extensionMimeType.containsKey(extension);
	}

	public boolean supportsMimeType(String mimeType) {
		return mimeTypeFormats.containsKey(mimeType);
	}

	public Set<String> getMimeTypes() {
		return Collections.unmodifiableSet(mimeTypeFormats.keySet());
	}

	public Set<String> getExtensions() {
		return Collections.unmodifiableSet(extensionMimeType.keySet());
	}

	public int numberOf() {
		return extensionMimeType.size();
	}
}
