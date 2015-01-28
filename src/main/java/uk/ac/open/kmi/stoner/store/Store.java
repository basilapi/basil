package uk.ac.open.kmi.stoner.store;

import java.io.IOException;

import uk.ac.open.kmi.stoner.sparql.Specification;

public interface Store {

	public void saveSpec(String id, Specification spec) throws IOException;

	public Specification loadSpec(String id) throws IOException;

}
