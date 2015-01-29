package uk.ac.open.kmi.stoner.format;

import java.io.StringReader;
import java.io.Writer;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

public enum Engine {
	MUSTACHE("template/mustache");
	private String contentType;

	Engine(String contentType) {
		this.contentType = contentType;
	}

	public String getContentType() {
		return contentType;
	}

	public void exec(Writer writer, String template, Items data) {
		if(this.equals(MUSTACHE)){
			MustacheFactory mf = new DefaultMustacheFactory();
			Mustache mustache = mf
					.compile(new StringReader(template), template);
			mustache.execute(writer, data);
		}
	}

	public static Engine byContentType(String contentType) {
		if (MUSTACHE.contentType.equals(contentType)) {
			return MUSTACHE;
		}
		return null;
	}
}
