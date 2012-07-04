/*
 * Copyright 2012 Odysseus Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
