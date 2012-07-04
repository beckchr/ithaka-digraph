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
package de.odysseus.ithaka.digraph;

import java.util.Collection;
import java.util.Set;

/**
 * Digraph adapter.
 * A digraph adapter delegates to a digraph supplied at construction time.
 *
 * @param <V> vertex type
 * @param <E> edge type
 */
public abstract class DigraphAdapter<V,E> implements Digraph<V,E> {	
	private final Digraph<V,E> delegate;
	
	public DigraphAdapter(Digraph<V,E> delegate) {
		this.delegate = delegate;
	}

	@Override
	public boolean add(V vertex) {
		return delegate.add(vertex);
	}

	@Override
	public boolean contains(Object source, Object target) {
		return delegate.contains(source, target);
	}

	@Override
	public boolean contains(Object vertex) {
		return delegate.contains(vertex);
	}

	@Override
	public E get(Object source, Object target) {
		return delegate.get(source, target);
	}

	@Override
	public int getOutDegree(Object vertex) {
		return delegate.getOutDegree(vertex);
	}

	@Override
	public int getEdgeCount() {
		return delegate.getEdgeCount();
	}

	@Override
	public int getVertexCount() {
		return delegate.getVertexCount();
	}

	@Override
	public Iterable<V> vertices() {
		return delegate.vertices();
	}

	@Override
	public E put(V source, V target, E edge) {
		return delegate.put(source, target, edge);
	}

	@Override
	public E remove(V source, V target) {
		return delegate.remove(source, target);
	}

	@Override
	public boolean remove(V vertex) {
		return delegate.remove(vertex);
	}

	@Override
	public void removeAll(Collection<V> vertices) {
		delegate.removeAll(vertices);
	}
	
	@Override
	public Digraph<V,E> reverse() {
		return delegate.reverse();
	}
	
	@Override
	public Digraph<V, E> subgraph(Set<V> vertices) {
		return delegate.subgraph(vertices);
	}
	
	@Override
	public boolean isAcyclic() {
		return delegate.isAcyclic();
	}
	
	@Override
	public Iterable<V> targets(Object source) {
		return delegate.targets(source);
	}
	
	@Override
	public String toString() {
		return delegate.toString();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj.getClass() == getClass()) {
			return delegate.equals(((DigraphAdapter<?,?>)obj).delegate);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return delegate.hashCode();
	}
}
