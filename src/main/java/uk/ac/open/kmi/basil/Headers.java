package uk.ac.open.kmi.basil;

public final class Headers {
	private Headers() {
	}

	public static final String PREFIX = "X-Stoner-";
	public static final String Error = PREFIX + "Error";
	public static final String Endpoint = PREFIX + "Endpoint";
	public static final String Api = PREFIX + "Api";
	public static final String Spec = PREFIX + "Spec";
	public static final String View = PREFIX + "View";
	public static final String Store = PREFIX + "Store";
	public static final String Type = PREFIX + "Type";

	public static String getHeader(String parameter) {
		return PREFIX + parameter.substring(0, 1).toUpperCase()
				+ parameter.substring(1).toLowerCase();
	}

	public static String asParameter(String Header) {
		return Header.substring(PREFIX.length()).toLowerCase();
	}
}
