package uk.ac.open.kmi.basil.rendering;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.jena.atlas.iterator.Transform;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

public class SimpleTripleAdapter implements Transform<QuerySolution,Iterator<Triple>> {
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
	public Iterator<Triple> convert(QuerySolution qs) {
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
