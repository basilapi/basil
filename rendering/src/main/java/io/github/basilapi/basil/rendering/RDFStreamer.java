/*
 * Copyright (c) 2021. Enrico Daga and Luca Panziera
 *
 * MLicensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.github.basilapi.basil.rendering;

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
