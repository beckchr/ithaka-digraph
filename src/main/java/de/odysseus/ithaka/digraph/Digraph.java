/*
 * Copyright 2008 Odysseus Software GmbH, Frankfurt am Main/Germany.
 */
package de.odysseus.ithaka.digraph;

import java.util.Collection;
import java.util.Set;

public interface Digraph<V, E> {

	public E get(Object source, Object target);

	public boolean contains(Object source, Object target);

	public boolean contains(Object vertex);

	public boolean add(V vertex);

	public E put(V source, V target, E edge);

	public E remove(V source, V target);
	
	public boolean remove(V vertex);

	public void removeAll(Collection<V> vertices);

	public Iterable<V> vertices();

	public Iterable<V> targets(Object source);

	public int getVertexCount();

	public int getOutDegree(Object vertex);

	public int getEdgeCount();
	
	public boolean isAcyclic();
	
	public Digraph<V,E> reverse();
	
	public Digraph<V,E> subgraph(Set<V> vertices);
}