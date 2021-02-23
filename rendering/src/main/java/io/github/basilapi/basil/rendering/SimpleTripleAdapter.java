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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import org.apache.jena.graph.Triple;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

public class SimpleTripleAdapter implements Function<QuerySolution,Iterator<Triple>> {
	private int rowIndex = 0;
	private String instanceNS;
	private String instanceNamePrefix;
	private String propertyNamePrefix;
	public SimpleTripleAdapter(String instanceNS) {
		this(instanceNS, "row");
	}

	public SimpleTripleAdapter(String instanceNS, String instanceNamePrefix) {
		this(instanceNS, instanceNamePrefix, "col");
	}

	public SimpleTripleAdapter(String instanceNS, String instanceNamePrefix, String propertyNamePrefix) {
		this.instanceNamePrefix = instanceNamePrefix;
		this.instanceNS = instanceNS;
		this.propertyNamePrefix = propertyNamePrefix;
	}

	@Override
	public Iterator<Triple> apply(QuerySolution qs) {
		rowIndex++;
		Resource subject = ResourceFactory.createResource(new StringBuilder().append(instanceNS).append(instanceNamePrefix).append(rowIndex).toString());
		Property property;
		List<Triple> list = new ArrayList<Triple>();
		Iterator<String> cn = qs.varNames();
		while (cn.hasNext()) {
			String c = cn.next();
			property = ResourceFactory.createProperty(new StringBuilder().append(instanceNS).append(propertyNamePrefix).append(c).toString());
			list.add(new Triple(subject.asNode(), property.asNode(), qs.get(c).asNode()));
		}
		return list.iterator();
	}
}
