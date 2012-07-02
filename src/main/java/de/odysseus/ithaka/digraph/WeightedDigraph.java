/*
 * Copyright 2008 Odysseus Software GmbH, Frankfurt am Main/Germany.
 */
package de.odysseus.ithaka.digraph;

import java.util.Set;

public interface WeightedDigraph<V> extends Digraph<V, Integer>, EdgeWeights<V> {

	/**
	 * Adds the given weight to the edge specified by its source and target vertices.
	 * Automatically inserts the edge if necessary.
	 * @param source source vertex
	 * @param target target vertex
	 * @param weight weight to be added
	 */
	public void add(V source, V target, int weight);

	/**
	 * @return sum of edge weights
	 */
	public int totalWeight();
	
	/**
	 * Restrict result type.
	 */
	@Override
	public WeightedDigraph<V> reverse();
	
	/**
	 * Restrict result type.
	 */
	@Override
	public WeightedDigraph<V> subgraph(Set<V> vertices);
}