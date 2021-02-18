package io.github.basilapi.basil.core;

import java.util.Date;
import java.util.Set;

public interface ApiInfo {
	String getId();
	String getName();
	Date created();
	Date modified();
	Set<String> alias();
}