package uk.ac.open.kmi.basil.rendering;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.core.Quad;

public class SimpleQuadAdapter implements Function<QuerySolution,Iterator<Quad>> {
	private int rowIndex = 0;
	private String graphName;
	private String instanceNS;
	private String instanceNamePrefix;
	private String propertyNamePrefix;
	
	public SimpleQuadAdapter(String graph) {
		this(graph, graph, "row");
	}	
	public SimpleQuadAdapter(String graph, String instanceNS) {
		this(graph, instanceNS, "row");
	}

	public SimpleQuadAdapter(String graph, String instanceNS, String instanceNamePrefix) {
		this(graph, instanceNS, instanceNamePrefix, "col");
	}

	public SimpleQuadAdapter(String graph, String instanceNS, String instanceNamePrefix, String propertyNamePrefix) {
		this.graphName = graph;
		this.instanceNamePrefix = instanceNamePrefix;
		this.instanceNS = instanceNS;
		this.propertyNamePrefix = propertyNamePrefix;
	}

	@Override
	public Iterator<Quad> apply(QuerySolution qs) {
		rowIndex++;
		Node graph = ResourceFactory.createResource(graphName).asNode();
		Resource subject = ResourceFactory.createResource(new StringBuilder().append(instanceNS).append(instanceNamePrefix).append(rowIndex).toString());
		Property property;
		List<Quad> list = new ArrayList<Quad>();
		Iterator<String> cn = qs.varNames();
		while (cn.hasNext()) {
			String c = cn.next();
			property = ResourceFactory.createProperty(new StringBuilder().append(instanceNS).append(propertyNamePrefix).append(c).toString());
			list.add(new Quad(graph, new Triple( subject.asNode(), property.asNode(), qs.get(c).asNode())));
		}
		return list.iterator();
	}
}
