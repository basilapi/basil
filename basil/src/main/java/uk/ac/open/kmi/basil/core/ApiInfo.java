package uk.ac.open.kmi.basil.core;

import java.util.Date;

public interface ApiInfo {
	String getId();
	String getName();
	Date created();
	Date modified();
}
