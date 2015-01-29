package uk.ac.open.kmi.stoner.format;

import java.io.Serializable;

import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Format implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5880624222164431668L;

	private String mimeType;
	private String extension;
	private String template;
	private Engine engine;

	public String getMimeType() {
		return mimeType;
	}

	void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public String getExtension() {
		return extension;
	}

	void setExtension(String extension) {
		this.extension = extension;
	}

	public String getTemplate() {
		return template;
	}

	void setTemplate(String template, Engine engine) {
		this.template = template;
		this.engine = engine;
	}

	public Engine getEngine() {
		return this.engine;
	}

	public boolean equals(Object o) {
		if (o instanceof Format) {
			Format f = (Format) o;
			return f.engine.equals(this.engine)
					&& f.extension.equals(this.extension)
					&& f.mimeType.equals(this.mimeType)
					&& f.template.equals(this.template);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(engine).append(mimeType)
				.append(extension).append(template).build();
	}
}
