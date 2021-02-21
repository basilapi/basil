package io.github.basilapi.basil.alias;

import java.io.IOException;

public class BadAliasException extends IOException {
	private String alias;

	private static final long serialVersionUID = 1L;

	public BadAliasException(String alias) {
		this.alias = alias;
	}

	@Override
	public String getMessage() {
		return "The alias provided does not respects the pattern: " + AliasUtils.REGEX.pattern();
	}

	public String getBadAlias() {
		return alias;
	}
}
