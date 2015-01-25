package uk.ac.open.kmi.stoner.sparql;

public interface QueryParameter {

	public abstract boolean isNeat();

	public abstract String getParameterName() throws ParameterException;

	public abstract boolean isForcedIri() throws ParameterException;

	public abstract boolean isForcedTypedLiteral() throws ParameterException;

	public abstract boolean isForcedPlainLiteral() throws ParameterException;

	public abstract boolean isForcedLangedLiteral() throws ParameterException;

	public abstract boolean isPlain() throws ParameterException;

	public abstract boolean isOptional() throws ParameterException;

	public abstract String getLang() throws ParameterException;

}