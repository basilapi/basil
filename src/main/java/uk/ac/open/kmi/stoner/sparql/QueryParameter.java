package uk.ac.open.kmi.stoner.sparql;


public class QueryParameter {

	private String name;
	private boolean isForcedIri = false;
	private boolean isForcedPlainLiteral = false;
	private boolean isForcedLangedLiteral = false;
	private boolean isForcedTypedLiteral = false;
	private boolean isPlain = false;
	private String lang = null;
	private boolean isNeat = true;
	private boolean isOptional;
	private String datatype = null;

	public String getName() {
		return name;
	}

	void setName(String name) {
		this.name = name;
	}

	public boolean isForcedIri() {
		return isForcedIri;
	}

	void setForcedIri(boolean isForcedIri) {
		this.isForcedIri = isForcedIri;
	}

	public boolean isForcedPlainLiteral() {
		return isForcedPlainLiteral;
	}

	void setForcedPlainLiteral(boolean isForcedPlainLiteral) {
		this.isForcedPlainLiteral = isForcedPlainLiteral;
	}

	public boolean isForcedLangedLiteral() {
		return isForcedLangedLiteral;
	}

	void setForcedLangedLiteral(boolean isForcedLangedLiteral) {
		this.isForcedLangedLiteral = isForcedLangedLiteral;
	}

	public boolean isForcedTypedLiteral() {
		return isForcedTypedLiteral;
	}

	void setForcedDatatype(boolean isForcedDatatype) {
		this.isForcedTypedLiteral = isForcedDatatype;
	}

	public boolean isPlain() {
		return isPlain;
	}

	void setPlain(boolean isPlain) {
		this.isPlain = isPlain;
	}

	public String getLang() {
		return lang;
	}

	void setLang(String lang) {
		this.lang = lang;
	}

	public boolean isNeat() {
		return isNeat;
	}

	void setNeat(boolean isNeat) {
		this.isNeat = isNeat;
	}

	public boolean isOptional() {
		return isOptional;
	}

	void setOptional(boolean isOptional) {
		this.isOptional = isOptional;
	}

	public String getDatatype() {
		return datatype;
	}

	void setDatatype(String datatype) {
		this.datatype = datatype;
	}

}