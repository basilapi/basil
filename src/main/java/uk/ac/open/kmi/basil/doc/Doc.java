package uk.ac.open.kmi.basil.doc;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Doc implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8091475762370792961L;

	public enum Field {
		NAME, DESCRIPTION
	}

	private Map<Field, String> doc = new HashMap<Field, String>();

	public void set(Field field, String value) {
		doc.put(field, value);
	}
}
