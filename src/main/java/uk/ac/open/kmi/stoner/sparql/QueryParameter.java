package uk.ac.open.kmi.stoner.sparql;

import java.io.Serializable;

public class QueryParameter implements Serializable {

	public enum Type {
		IRI, TypedLiteral, LangedLiteral, PlainLiteral
	}

	private static final long serialVersionUID = 6725251315049248905L;
	private String name;
	private String lang = null;
	private boolean isOptional;
	private String datatype = null;
	private Type type = Type.PlainLiteral; // defaults to plain literal

	public String getName() {
		return name;
	}

	void setName(String name) {
		this.name = name;
	}

	public boolean isIri() {
		return type == Type.IRI;
	}

	void setIri() {
		this.type = Type.IRI;
		this.datatype = null;
		this.lang = null;
	}

	public boolean isPlainLiteral() {
		return type == Type.PlainLiteral;
	}

	public boolean isLangedLiteral() {
		return type == Type.LangedLiteral;
	}

	public boolean isTypedLiteral() {
		return type == Type.TypedLiteral;
	}

	public String getLang() {
		return lang;
	}

	void setLang(String lang) {
		this.lang = lang;
		this.type = Type.LangedLiteral;
		this.datatype = null;
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
		this.type = Type.TypedLiteral;
		this.lang = null;
	}

	public void setPlainLiteral() {
		this.type = Type.PlainLiteral;
		this.datatype = null;
		this.lang = null;
	}

	@Override
	public boolean equals(Object obj) {
		boolean eq = (obj instanceof QueryParameter)
				&& (((QueryParameter) obj).type.equals(this.type))
				&& (((QueryParameter) obj).getName().equals(this.getName()));
		if (this.isLangedLiteral()) {
			return this.getLang().equals((((QueryParameter) obj).getLang()));
		} else if (this.isTypedLiteral()) {
			return this.getDatatype().equals(
					((QueryParameter) obj).getDatatype());
		}
		return eq;
	}
}