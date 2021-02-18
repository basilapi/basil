package io.github.basilapi.basil.alias;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * 
 * @author enridaga
 * @since 0.5.0
 */
public final class AliasUtils {
	final static Pattern REGEX = Pattern.compile("^[0-9a-zA-Z\\-]{5,16}$");

	/**
	 * An alias is safe if it matches {AliasUtils.REGEX}
	 * 
	 * @param alias
	 * @return
	 */
	public static final boolean isSafe(String alias) {
		return REGEX.matcher(alias).find();
	}

	public static final void test(Set<String> alias) throws BadAliasException {
		for (String t : alias) {
			if (!isSafe(t)) {
				throw new BadAliasException(t);
			}
		}
	}
}
