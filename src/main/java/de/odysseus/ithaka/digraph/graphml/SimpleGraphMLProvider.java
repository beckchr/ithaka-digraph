package de.odysseus.ithaka.digraph.graphml;

import java.util.ArrayList;

import de.odysseus.ithaka.digraph.Digraph;

public class SimpleGraphMLProvider<V, E, G extends Digraph<? extends V, ? extends E>> implements GraphMLProvider<V, E, G> {
	private final ArrayList<GraphMLProperty<G>> graphProperties = new ArrayList<GraphMLProperty<G>>();
	private final ArrayList<GraphMLProperty<V>> nodeProperties = new ArrayList<GraphMLProperty<V>>();
	private final ArrayList<GraphMLProperty<E>> edgeProperties = new ArrayList<GraphMLProperty<E>>();

	private final String schemaLocation;
	
	public SimpleGraphMLProvider() {
		this(null);
	}
	
	public SimpleGraphMLProvider(String schemaLocation) {
		this.schemaLocation = schemaLocation;
	}
	
	@Override
	public String getSchemaLocation() {
		return schemaLocation;
	}
	
	public void addGraphProperty(GraphMLProperty<G> graphProperty) {
		graphProperties.add(graphProperty);
	}
	
	@Override
	public Iterable<GraphMLProperty<G>> getGraphProperties() {
		return graphProperties;
	}
	
	public void addNodeProperty(GraphMLProperty<V> nodeProperty) {
		nodeProperties.add(nodeProperty);
	}
	
	@Override
	public Iterable<GraphMLProperty<V>> getNodeProperties() {
		return nodeProperties;
	}
	
	public void addEdgeProperty(GraphMLProperty<E> edgeProperty) {
		edgeProperties.add(edgeProperty);
	}
	
	@Override
	public Iterable<GraphMLProperty<E>> getEdgeProperties() {
		return edgeProperties;
	}
}
