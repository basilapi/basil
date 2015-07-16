package uk.ac.open.kmi.basil.store;

import uk.ac.open.kmi.basil.doc.Doc;
import uk.ac.open.kmi.basil.sparql.Specification;
import uk.ac.open.kmi.basil.view.Views;

import java.io.IOException;
import java.util.List;

public interface Store {

	void saveSpec(String id, Specification spec) throws IOException;

	Specification loadSpec(String id) throws IOException;

	boolean existsSpec(String id) throws IOException;

	List<String> listSpecs() throws IOException;

	Views loadViews(String id) throws IOException;

	Doc loadDoc(String id) throws IOException;

	void saveViews(String id, Views views) throws IOException;
	
	void saveDoc(String id, Doc doc) throws IOException;

	boolean deleteDoc(String id) throws IOException;

	boolean deleteSpec(String id) throws IOException;
}
