package uk.ac.open.kmi.stoner.sparql;

import java.io.Serializable;

public class QueryParameter implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6725251315049248905L;
	private String name;
	private boolean isForcedIri = false;
	private boolean isForcedPlainLiteral = false;
	private boolean isForcedLangedLiteral = false;
	private boolean isForcedTypedLiteral = false;
	private boolean isPlain = false;
	private String lang = null;
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
		//
		this.datatype = null;
		this.lang = null;
		this.isForcedLangedLiteral = false;
		this.isForcedPlainLiteral = false;
		this.isForcedTypedLiteral = false;
	}

	public boolean isForcedPlainLiteral() {
		return isForcedPlainLiteral;
	}

	public boolean isForcedLangedLiteral() {
		return isForcedLangedLiteral;
	}

	public boolean isForcedTypedLiteral() {
		return isForcedTypedLiteral;
	}

	public boolean isPlain() {
		return isPlain;
	}

	void setPlain(boolean isPlain) {
		this.isPlain = isPlain;

		this.datatype = null;
		this.lang = null;
		this.isForcedLangedLiteral = false;
		this.isForcedIri = false;
		this.isForcedTypedLiteral = false;
		this.isForcedPlainLiteral = false;
	}

	public String getLang() {
		return lang;
	}

	void setLang(String lang) {
		this.lang = lang;
		this.isForcedLangedLiteral = true;
		//
		this.datatype = null;
		this.isForcedIri = false;
		this.isForcedPlainLiteral = false;
		this.isForcedTypedLiteral = false;

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
		this.isForcedTypedLiteral = true;
		//
		this.isForcedLangedLiteral = false;
		this.isForcedIri = false;
		this.isForcedPlainLiteral = false;
		this.lang = null;
	}

	public void setForcedPlainLiteral(boolean b) {
		this.isForcedPlainLiteral = true;
		//
		this.isForcedLangedLiteral = false;
		this.isForcedTypedLiteral = false;
		this.isForcedIri = false;
		this.datatype = null;
		this.lang = null;
	}

}