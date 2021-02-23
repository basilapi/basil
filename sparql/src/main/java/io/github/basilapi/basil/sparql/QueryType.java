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

package io.github.basilapi.basil.sparql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum QueryType {

	SELECT, ASK, CONSTRUCT, INSERT, DELETE;

	private static Logger log = LoggerFactory.getLogger(QueryType.class);

	private QueryType() {
	}


	public static QueryType guessQueryType(String q) throws UnknownQueryTypeException {

		// Get index of first occurrence of select, insert, ask, construct, and delete

		int pos = 0;
		boolean openQuote = false;
		boolean openUri = false;
		boolean openComment = false;
		boolean waitForToken = true;
		boolean openPrefix = false;

		while (pos < q.length()) {
			// check current char
			char c = q.charAt(pos);

			// If encounter escape char, move pos +2 and continue
			if (c == '\\') {
				pos += 2;
				continue;
			}

			// Inside a comment
			if (c == '#' && !openQuote && !openUri && !openComment) {
				openComment = true;
			} else if (openComment && c == '\n') {
				openComment = false;
			} else if (openComment) {
				pos += 1;
				continue;
			}

			// Inside a uri
			if (c == '<' && !openQuote && !openUri) {
				openUri = true;
			} else if (c == '>' && openUri) {
				openUri = false;
			} else if (openUri) {
				pos += 1;
				continue;
			}

			// Inside quotes
			if (c == '"') {
				openQuote = !openQuote;
			} else if (openQuote) {
				pos += 1;
				continue;
			}

			// Inside quotes
			if (c == '"') {
				openQuote = !openQuote;
			} else if (openQuote) {
				pos += 1;
				continue;
			}

			// Statements to ignore
			if (waitForToken) {
				for (String TKN : new String[] { "prefix" }) {
					if (q.length() > pos + TKN.length()) {
						// Test token select
						String sub = q.substring(pos, pos + TKN.length());
						boolean match = false;
						if (sub.toLowerCase().equals(TKN.toLowerCase())) {
							match = true;
						}
						log.trace("[{}] {} --{}-- -> {}", pos, TKN, sub, match);
						if (match) {
							switch (TKN) {
							case "prefix":
								openPrefix = true;
								pos += TKN.length();
								continue;
							}
						}
					}
				}
			}

			// If open prefix and char is :, exit prefix
			if (openPrefix && c == ':') {
				openPrefix = false;
				pos += 1;
				continue;
			}

			if (waitForToken && !openPrefix) {
				// Query Types Keywords
				for (QueryType qt : QueryType.values()) {
					String TKN = qt.toString();
					if (q.length() > pos + TKN.length()) {
						// Test token select
						String sub = q.substring(pos, pos + TKN.length());
						boolean match = false;
						if (sub.toLowerCase().equals(TKN.toLowerCase())) {
							match = true;
						}
						log.trace("[{}] {} --{}-- -> {}", pos, TKN, sub, match);
						if (match) {
							log.debug("Guessed query type {}", TKN);
							return QueryType.valueOf(TKN.toUpperCase());
						}
					}
				}
			}

			pos += 1;
			// If token was a space
			if (Character.isWhitespace(c)) {
				waitForToken = true;
			} else {
				waitForToken = false;
			}
		}

		throw new UnknownQueryTypeException(q);
	}

	public static boolean isUpdate(String query) throws UnknownQueryTypeException {
		QueryType qt = guessQueryType(query);

		if (qt.equals(SELECT) || qt.equals(ASK) || qt.equals(CONSTRUCT)) {
			return false;
		}

		return true;
	}
}
