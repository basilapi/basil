package uk.ac.open.kmi.basil.view;

import java.io.Serializable;

import org.apache.commons.lang3.builder.HashCodeBuilder;

public class View implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5880624222164431668L;

	private String mimeType;
	private String name;
	private String template;
	private Engine engine;

	public String getMimeType() {
		return mimeType;
	}

	void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public String getName() {
		return name;
	}

	void setExtension(String extension) {
		this.name = extension;
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
		if (o instanceof View) {
			View f = (View) o;
			return f.engine.equals(this.engine)
					&& f.name.equals(this.name)
					&& f.mimeType.equals(this.mimeType)
					&& f.template.equals(this.template);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(engine).append(mimeType)
				.append(name).append(template).build();
	}
}
