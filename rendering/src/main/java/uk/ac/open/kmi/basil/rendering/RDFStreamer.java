package uk.ac.open.kmi.basil.rendering;

import java.io.OutputStream;
import java.util.Iterator;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.iterator.Transform;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.system.StreamOps;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFWriter;
import org.apache.jena.riot.system.StreamRDFWriterFactory;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.util.iterator.WrappedIterator;

public class RDFStreamer {
	static {
		StreamRDFWriter.register(Lang.RDFXML, RDFFormat.RDFXML);
		StreamRDFWriter.register(Lang.RDFJSON, RDFFormat.RDFXML);
		StreamRDFWriter.register(RDFFormat.RDFXML, new StreamRDFXMLWriterFactory());
		StreamRDFWriter.register(RDFFormat.RDFJSON, new StreamRDFJSONWriterFactory());
	}

	public static void stream(OutputStream os, ResultSet rs, RDFFormat format, Transform<QuerySolution, Iterator<Triple>> adapter) {
		StreamRDF stream = StreamRDFWriter.getWriterStream(os, format);
		Iterator<Triple> iter = WrappedIterator.createIteratorIterator(Iter.map(rs, adapter));
		stream.start();
		StreamOps.sendTriplesToStream(iter, stream);
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
