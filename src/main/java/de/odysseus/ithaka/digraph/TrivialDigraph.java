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
import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Convenience class representing a digraph with zero or one vertex and an optional loop edge.
 * Vertex as well as edge <code>null</code> is forbidden.
 * @author beck
 *
 * @param <V> the vertex type
 * @param <E> the edge type
 */
public class TrivialDigraph<V,E> implements DoubledDigraph<V,E> {
	/**
	 * Answer a factory which creates empty trivial digraphs.
	 * @param <V> vertex type
	 * @param <E> edge type
	 * @return trivial digraph factory
	 */
	public static <V,E> DigraphFactory<TrivialDigraph<V,E>> getDigraphFactory() {
		return new DigraphFactory<TrivialDigraph<V,E>>() {
			@Override
			public TrivialDigraph<V,E> create() {
				return new TrivialDigraph<V,E>();
			}
		};
	}

	private V vertex;
	private E loop;

	public TrivialDigraph() {
	}

	public TrivialDigraph(V vertex) {
		this(vertex, null);
	}
	
	public TrivialDigraph(V vertex, E loop) {
		this.vertex = vertex;
		this.loop = loop;
	}
	
	/**
	 * @throws UnsupportedOperationException if adding the vertex would result in having 2 vertices in the graph 
	 * @throws IllegalArgumentException if <code>vertex == null</code>
	 */
	@Override
	public boolean add(V vertex) {
		if (vertex == null) {
			throw new IllegalArgumentException("Cannot add vertex null!");
		}
		if (this.vertex == null) {
			this.vertex = vertex;
			return true;
		}
		if (this.vertex.equals(vertex)) {
			return false;
		}
		throw new UnsupportedOperationException("TrivialDigraph must contain at most one vertex!");
	}

	@Override
	public boolean contains(Object source, Object target) {
		return vertex != null && loop != null && vertex.equals(source) && vertex.equals(target);
	}

	@Override
	public boolean contains(Object vertex) {
		return this.vertex != null && this.vertex.equals(vertex);
	}

	@Override
	public E get(Object source, Object target) {
		return contains(source, target) ? loop : null;
	}

	@Override
	public int getInDegree(Object vertex) {
		return loop == null ? 0 : 1;
	}

	@Override
	public int getOutDegree(Object vertex) {
		return loop == null ? 0 : 1;
	}

	@Override
	public int getEdgeCount() {
		return loop == null ? 0 : 1;
	}
	
	@Override
	public int getVertexCount() {
		return vertex == null ? 0 : 1;
	}

	@Override
	public Iterable<V> vertices() {
		if (vertex == null) {
			return Collections.emptyList();
		}
		return new Iterable<V>() {
			@Override
			public Iterator<V> iterator() {
				return new Iterator<V>() {
					boolean hasNext = true;
					@Override
					public boolean hasNext() {
						return hasNext;
					}
					@Override
					public V next() {
						if (hasNext) {
							hasNext = false;
							return vertex;
						}
						throw new NoSuchElementException("No more vertices");
					}
					@Override
					public void remove() {
						if (hasNext) {
							throw new IllegalStateException();
						}
						TrivialDigraph.this.remove(vertex);
					}
				};
			}
			@Override
			public String toString() {
				return "[" + vertex + "]";
			}
		};
	}

	@Override
	public E put(V source, V target, E edge) {
		if (edge == null) {
			throw new IllegalArgumentException("Cannot add edge null!");
		}
		if (source != target) {
			throw new UnsupportedOperationException("TrivialDigraph must not contain no-loop edges!");
		}
		E result = loop;
		add(source);
		loop = edge;
		return result;
	}

	@Override
	public E remove(V source, V target) {
		if (contains(source, target)) {
			E result = loop;
			loop = null;
			return result;
		}
		return null;
	}

	@Override
	public boolean remove(V vertex) {
		if (this.vertex != null && this.vertex.equals(vertex)) {
			this.vertex = null;
			loop = null;
			return true;
		}
		return false;
	}

	@Override
	public void removeAll(Collection<V> vertices) {
		if (vertices.contains(vertex)) {
			remove(vertex);
		}
	}

	@Override
	public DoubledDigraph<V,E> reverse() {
		return this;
	}
	
	@Override
	public Digraph<V, E> subgraph(Set<V> vertices) {
		return vertex != null && vertices.contains(vertex) ? this : Digraphs.<V,E>emptyDigraph();
	}

	@Override
	public Iterable<V> sources(Object target) {
		return targets(target);
	}

	@Override
	public Iterable<V> targets(Object source) {
		if (loop == null || vertex == null || !vertex.equals(source)) {
			return Collections.emptyList();
		}
		return new Iterable<V>() {
			@Override
			public Iterator<V> iterator() {
				return new Iterator<V>() {
					boolean hasNext = true;
					@Override
					public boolean hasNext() {
						return hasNext;
					}
					@Override
					public V next() {
						if (hasNext) {
							hasNext = false;
							return vertex;
						}
						throw new NoSuchElementException("No more vertices");
					}
					@Override
					public void remove() {
						if (hasNext) {
							throw new IllegalStateException();
						}
						TrivialDigraph.this.remove(vertex, vertex);
					}
				};
			}
			@Override
			public String toString() {
				return "[" + vertex + "]";
			}
		};
	}
	
	@Override
	public boolean isAcyclic() {
		return loop == null;
	}
}
