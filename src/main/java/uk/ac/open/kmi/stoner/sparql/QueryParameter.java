package uk.ac.open.kmi.stoner.sparql;

import java.io.Serializable;

/**
 * A query parameter of the produced Specification. Includes information about
 * how the param value needs to be prepared to be bound in the SPARQL context.
 * 
 * @author enridaga
 *
 */
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

	/**
	 * Name of the parameter.
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name of the parameter.
	 * 
	 * @param name
	 */
	void setName(String name) {
		this.name = name;
	}

	/**
	 * If the value has to be treated as IRI in the SPARQL query.
	 * 
	 * @return boolean true or false
	 */
	public boolean isIri() {
		return type == Type.IRI;
	}

	/**
	 * The value has to be treated as IRI in the SPARQL query.
	 */
	void setIri() {
		this.type = Type.IRI;
		this.datatype = null;
		this.lang = null;
	}

	/**
	 * Whether the value has to treated as plain literal in the SPARQL query.
	 * 
	 * @return boolean true or false.
	 */
	public boolean isPlainLiteral() {
		return type == Type.PlainLiteral;
	}

	/**
	 * Whether the value has to be treated as langed literal in the SPARQL
	 * query. The value of {@link #getLang()} has to be used as lang.
	 * 
	 * @return boolean true or false.
	 */
	public boolean isLangedLiteral() {
		return type == Type.LangedLiteral;
	}

	/**
	 * Whether the value has to be treated as typed literal in the SPARQL query.
	 * The value of {@link #getDatatype()} has to be used as datatype.
	 * 
	 * @return boolean true or false.
	 */
	public boolean isTypedLiteral() {
		return type == Type.TypedLiteral;
	}

	/**
	 * In case of langed literal, use this value as lang.
	 * 
	 * @return the lang
	 */
	public String getLang() {
		return lang;
	}

	/**
	 * Sets this query parameter as langed literal, specifying what lang is to
	 * be used.
	 * 
	 * @param lang
	 */
	void setLang(String lang) {
		this.lang = lang;
		this.type = Type.LangedLiteral;
		this.datatype = null;
	}

	/**
	 * Consider thie parameter as optional.
	 * 
	 * @return boolean true or false.
	 */
	public boolean isOptional() {
		return isOptional;
	}

	/**
	 * Consider this parameter as optional
	 * 
	 * @param isOptional
	 */
	void setOptional(boolean isOptional) {
		this.isOptional = isOptional;
	}

	/**
	 * Datatype to be used when preparing the value of the typed literal to be
	 * bound in the SPARQL query.
	 * 
	 * @return the datatype iri as String.
	 */
	public String getDatatype() {
		return datatype;
	}

	/**
	 * Sets this query parameter as typed literal, specifying what datatype is
	 * to be used.
	 * 
	 * @param datatype
	 */
	void setDatatype(String datatype) {
		this.datatype = datatype;
		this.type = Type.TypedLiteral;
		this.lang = null;
	}

	/**
	 * The value of this query parameter has to be used as simple plain literal.
	 * 
	 */
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