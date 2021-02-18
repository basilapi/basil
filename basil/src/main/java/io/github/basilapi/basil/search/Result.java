package uk.ac.open.kmi.basil.search;

import java.util.Map;

public interface Result {
	public String id();
	public Map<String,String> context();
}
