/*
 * Copyright 2012 Odysseus Software GmbH, Frankfurt am Main/Germany.
 */
package de.odysseus.ithaka.digraph.graphml;

import de.odysseus.ithaka.digraph.Digraph;

public interface GraphMLProvider<V, E, G extends Digraph<? extends V, ? extends E>> {
	public Iterable<GraphMLProperty<G>> getGraphProperties();
	public Iterable<GraphMLProperty<V>> getNodeProperties();
	public Iterable<GraphMLProperty<E>> getEdgeProperties();

	public String getSchemaLocation();
}
