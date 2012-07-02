/*
 * Copyright 2008 Odysseus Software GmbH, Frankfurt am Main/Germany.
 */
package de.odysseus.ithaka.digraph;

/**
 * Digraph holding its reverse graph and granting access to incoming edges
 */
public interface DoubledDigraph<V,E> extends Digraph<V,E> {
	public int getInDegree(Object vertex);
	
	public Iterable<V> sources(Object target);

	@Override
	public DoubledDigraph<V, E> reverse();
}
