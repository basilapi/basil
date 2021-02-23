/*
 * Copyright (c) 2021. Enrico Daga and Luca Panziera
 *
 * MLicensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

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
	 * @param alias the alias name to be tested
	 * @return whether the value is a safe alias or not
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
