package uk.ac.open.kmi.basil.search;

import java.io.IOException;
import java.util.Collection;

public interface SearchProvider {

	public Collection<Result> contextSearch(Query query) throws IOException;
	public Collection<String> search(Query query) throws IOException;
}
