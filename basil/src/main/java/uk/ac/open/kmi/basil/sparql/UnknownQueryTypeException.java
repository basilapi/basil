package uk.ac.open.kmi.basil.sparql;

import uk.ac.open.kmi.basil.core.exceptions.SpecificationParsingException;

public class UnknownQueryTypeException extends SpecificationParsingException {

	public UnknownQueryTypeException(String q) {
		super("Unknown query type:" + q);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
