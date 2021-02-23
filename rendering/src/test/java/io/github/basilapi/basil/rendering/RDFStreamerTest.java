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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.iterator.Transform;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.system.StreamOps;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFWriter;
import org.apache.jena.util.iterator.WrappedIterator;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * FIXME - These tests use a live service at http://data.open.ac.uk and should better be moved to a separate module
 * @author ed4565
 *
 */
public class RDFStreamerTest {
	final static Logger log = LoggerFactory.getLogger(RDFStreamerTest.class);

	@Rule
	public TestName test = new TestName();

	@Test
	public void test() {
		log.info("{}", test.getMethodName());
		// prefixMappings.setNsPrefixes(prefixes);
		OutputStream os = new ByteArrayOutputStream();
		StreamRDF stream = StreamRDFWriter.getWriterStream(os, Lang.RDFTHRIFT);
		QueryExecution qe = QueryExecutionFactory.sparqlService(
				"http://data.open.ac.uk/sparql", "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> SELECT ?A ?B ?C WHERE {?A a ?B . ?A rdf:type ?C} LIMIT 100");

		Transform<QuerySolution, Iterator<Triple>> m = new Transform<QuerySolution, Iterator<Triple>>() {
			Integer rowIndex = 0;

			@Override
			public Iterator<Triple> apply(QuerySolution t) {
				return convert(t);
			}
			
			public Iterator<Triple> convert(QuerySolution qs) {
				rowIndex++;
				String ns = "http://www.example.org/test/row#";
				String pns = "http://www.example.org/test/col#";
				Resource subject = ResourceFactory.createResource(ns + Integer.toString(rowIndex));
				Property property;
				List<Triple> list = new ArrayList<Triple>();
				Iterator<String> cn = qs.varNames();
				while (cn.hasNext()) {
					String c = cn.next();
					property = ResourceFactory.createProperty(pns + c);
					list.add(new Triple(subject.asNode(), property.asNode(), qs.get(c).asNode()));
				}
				return list.iterator();
			}
		};
		Iterator<Triple> iter = WrappedIterator.createIteratorIterator(Iter.map(qe.execSelect(), m));
		stream.start();
		StreamOps.sendTriplesToStream(iter, stream);
		stream.finish();
		log.info("Stream: {}", os);
	}

	@Test
	public void testNT() {
		log.info("{}", test.getMethodName());
		OutputStream os = new ByteArrayOutputStream();
		QueryExecution qe = QueryExecutionFactory.sparqlService(
				"http://data.open.ac.uk/sparql", "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> SELECT ?A ?B ?C WHERE {?A a ?B . ?A rdf:type ?C} LIMIT 100");
		RDFStreamer.stream(os, qe.execSelect(), RDFFormat.NTRIPLES, new SimpleTripleAdapter("http://www.example.org/test/"));
	}

	/**
	 * FIXME - to understand why this test works intermittently
	 * @throws IOException
	 */
	@Ignore 
	@Test
	public void testRDFXML() throws IOException {
		log.info("{}", test.getMethodName());
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		QueryExecution qe = QueryExecutionFactory.sparqlService(
				"http://data.open.ac.uk/sparql", "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> SELECT ?A ?B ?C WHERE {?A a ?B . ?A rdf:type ?C} LIMIT 100");
		RDFStreamer.stream(os, qe.execSelect(), RDFFormat.RDFXML, new SimpleTripleAdapter("http://www.example.org/test/"));
		InputStream in = new ByteArrayInputStream(os.toByteArray());
		os.flush();
		//log.info("Stream: {}", os);
		try {
			
			ModelFactory.createDefaultModel().read(in, "");
		} catch (Exception e) {
			log.error("",e);
			Assert.assertTrue(false);
		}
	}
}
