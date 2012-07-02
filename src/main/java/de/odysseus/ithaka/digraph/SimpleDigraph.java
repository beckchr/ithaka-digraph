/*
 * Copyright 2008 Odysseus Software GmbH, Frankfurt am Main/Germany.
 */
package de.odysseus.ithaka.digraph;

import java.util.Set;

public interface SimpleDigraph<V> extends Digraph<V, Boolean> {

	/**
	 * Add an edge.
	 * @return <code>true</code> if the edge has been inserted, <code>false</code> if it already existed.
	 */
	public boolean add(V source, V target);

	/**
	 * Remove an edge.
	 * @return <code>true</code> if the edge has been removed, <code>false</code> if it didn't exist
	 */
	@Override
	public Boolean remove(V source, V target);

	/**
	 * Put an edge.
	 * If invoked, <code>true</code> must be passed as edge.
	 * Better use {@link #add(Object, Object)}}
	 * @throws IllegalArgumentException if supplied edge is not equal to <code>Boolean.TRUE</code>
	 */
	@Override
	public Boolean put(V source, V target, Boolean edge);
	
	/**
	 * Restrict result type.
	 */
	@Override
	public SimpleDigraph<V> reverse();
	
	/**
	 * Restrict result type.
	 */
	@Override
	public SimpleDigraph<V> subgraph(Set<V> vertices);
}