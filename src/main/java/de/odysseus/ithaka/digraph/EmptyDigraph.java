/*
 * Copyright 2008 Odysseus Software GmbH, Frankfurt am Main/Germany.
 */
package de.odysseus.ithaka.digraph;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * Empty digraph.
 * Adding a vertex or edge will throw a <code>UnsupportedOperationException</code>.
 * @author beck
 *
 * @param <V> the vertex type
 * @param <E> the edge type
 */
class EmptyDigraph<V,E> implements DoubledDigraph<V,E> {
	@Override
	public boolean add(Object vertex) {
		throw new UnsupportedOperationException("Empty digraph cannot have vertices!");
	}

	@Override
	public boolean contains(Object source, Object target) {
		return false;
	}

	@Override
	public boolean contains(Object vertex) {
		return false;
	}

	@Override
	public E get(Object source, Object target) {
		return null;
	}

	@Override
	public int getInDegree(Object vertex) {
		return 0;
	}

	@Override
	public int getOutDegree(Object vertex) {
		return 0;
	}

	@Override
	public int getEdgeCount() {
		return 0;
	}
	
	@Override
	public int getVertexCount() {
		return 0;
	}

	@Override
	public Iterable<V> vertices() {
		return Collections.emptyList();
	}

	@Override
	public E put(Object source, Object target, Object edge) {
		throw new UnsupportedOperationException("Empty digraph cannot have edges!");
	}

	@Override
	public E remove(Object source, Object target) {
		return null;
	}

	@Override
	public boolean remove(Object vertex) {
		return false;
	}

	@Override
	public void removeAll(Collection<V> vertices) {
	}
	
	@Override
	public DoubledDigraph<V,E> reverse() {
		return this;
	}
	
	@Override
	public Digraph<V, E> subgraph(Set<V> vertices) {
		return this;
	}

	@Override
	public Iterable<V> sources(Object target) {
		return Collections.emptyList();
	}

	@Override
	public Iterable<V> targets(Object source) {
		return Collections.emptyList();
	}
	
	@Override
	public boolean isAcyclic() {
		return true;
	}
}
