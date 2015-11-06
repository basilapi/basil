package uk.ac.open.kmi.basil.rendering;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import javax.ws.rs.core.MediaType;

import org.apache.commons.io.output.ByteArrayOutputStream;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.hp.hpl.jena.sparql.resultset.JSONOutput;
import com.hp.hpl.jena.sparql.resultset.XMLOutput;

public class BooleanRenderer implements Renderer<Boolean> {

	@Override
	public InputStream stream(Boolean o, MediaType type) throws CannotRenderException {
		return new ByteArrayInputStream(render(o, type).getBytes(StandardCharsets.UTF_8));
	}

	@Override
	public String render(Boolean b, MediaType type) throws CannotRenderException {

		// text/plain
		if (MediaType.TEXT_PLAIN_TYPE.equals(type)) {
			if (b) {
				return "True\n";
			} else {
				return "False\n";
			}
		}

		// xml
		if (MediaType.TEXT_XML_TYPE.equals(type) || MediaType.APPLICATION_XML_TYPE.equals(type)) {
			StringBuilder p = new StringBuilder();
			p.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
			p.append("\n");
			p.append("<data>");
			p.append("\n\t");
			p.append("<vars>");
			// boolean
			p.append("\n\t\t");
			p.append("<var>");
			p.append("boolean");
			p.append("</var>");

			p.append("\n\t");
			p.append("</vars>");

			p.append("\n\t");
			p.append("<items>");

			p.append("\n\t");
			p.append("<item>");

			p.append("\n\t\t");
			String v = "boolean";

			p.append("<");
			p.append(v);
			p.append(" ");
			p.append("type=\"");
			p.append("boolean");
			p.append("\"");
			p.append(">");
			if (b)
				p.append("true");
			else
				p.append("false");
			p.append("</");
			p.append(v);
			p.append(">");

			p.append("\n\t");
			p.append("</item>");

			p.append("\n\t");
			p.append("</items>");

			p.append("\n");
			p.append("</data>");
			p.append("\n");
			return p.toString();
		}

		if (MediaType.APPLICATION_JSON_TYPE.equals(type)) {
			JsonObject o = new JsonObject();
			o.add("bool", new JsonPrimitive(b));
			return o.toString() + "\n";
		}

		// sparql-results+xml
		if (MoreMediaType.SPARQL_RESULTS_XML_TYPE.equals(type)) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			XMLOutput xOut = new XMLOutput(null);
			xOut.format(baos, b);
			return new String(baos.toByteArray());
		}

		// sparql-results+json
		if (MoreMediaType.SPARQL_RESULTS_JSON_TYPE.equals(type)) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			JSONOutput xOut = new JSONOutput();
			xOut.format(baos, b);
			return new String(baos.toByteArray());
		}

		throw new CannotRenderException();
	}
}
