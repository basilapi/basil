package uk.ac.open.kmi.stoner.store;

import java.io.IOException;
import java.util.List;

import uk.ac.open.kmi.stoner.sparql.Specification;
import uk.ac.open.kmi.stoner.view.Views;

public interface Store {

	public void saveSpec(String id, Specification spec) throws IOException;

	public Specification loadSpec(String id) throws IOException;

	public boolean existsSpec(String id);

	public List<String> listSpecs();

	public Views loadViews(String id) throws IOException;

	public void saveViews(String id, Views views) throws IOException;
}
