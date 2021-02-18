package uk.ac.open.kmi.basil.rendering;

import java.io.OutputStream;
import java.util.Iterator;
import java.util.function.Function;

import org.apache.jena.atlas.iterator.Iter;
//import org.apache.jena.atlas.iterator.Transform;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.system.StreamOps;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFWriter;
import org.apache.jena.riot.system.StreamRDFWriterFactory;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.util.iterator.WrappedIterator;

public class RDFStreamer {
	static {
		StreamRDFWriter.register(Lang.RDFXML, RDFFormat.RDFXML);
		StreamRDFWriter.register(Lang.RDFJSON, RDFFormat.RDFXML);
		StreamRDFWriter.register(RDFFormat.RDFXML, new StreamRDFXMLWriterFactory());
		StreamRDFWriter.register(RDFFormat.RDFJSON, new StreamRDFJSONWriterFactory());
	}

	public static void stream(OutputStream os, ResultSet rs, RDFFormat format, Function<QuerySolution, Iterator<Triple>> adapter) {
		StreamRDF stream = StreamRDFWriter.getWriterStream(os, format);
		Iterator<Triple> iter = WrappedIterator.createIteratorIterator(Iter.map(rs, adapter));
		stream.start();
		StreamOps.sendTriplesToStream(iter, stream);
		stream.finish();
	}
	

	public static void streamQuads(OutputStream os, ResultSet rs, RDFFormat format, Function<QuerySolution, Iterator<Quad>> adapter) {
		StreamRDF stream = StreamRDFWriter.getWriterStream(os, format);
		Iterator<Quad> iter = WrappedIterator.createIteratorIterator(Iter.map(rs, adapter));
		stream.start();
		StreamOps.sendQuadsToStream(iter, stream);
		stream.finish();
	}
	
	static class StreamRDFXMLWriterFactory implements StreamRDFWriterFactory{
		@Override
		public StreamRDF create(OutputStream output, RDFFormat format) {
			return new WriterStreamRDFXML(output);
		}
	}

	static class StreamRDFJSONWriterFactory implements StreamRDFWriterFactory{
		@Override
		public StreamRDF create(OutputStream output, RDFFormat format) {
			return new WriterStreamRDFJSON(output);
		}
	}
}
