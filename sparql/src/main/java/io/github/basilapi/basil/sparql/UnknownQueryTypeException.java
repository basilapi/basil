package io.github.basilapi.basil.sparql;

import io.github.basilapi.basil.core.exceptions.SpecificationParsingException;

public class UnknownQueryTypeException extends SpecificationParsingException {

	public UnknownQueryTypeException(String q) {
		super("Unknown query type:" + q);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
