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
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Map-based directed graph implementation.
 *
 * @param <V> vertex type
 * @param <E> edge type
 */
public class MapDigraph<V, E> implements Digraph<V, E> {
	/**
	 * Factory creating default <code>MapDigraph</code>.
	 * @return map digraph factory
	 */
	public static <V, E> DigraphFactory<MapDigraph<V, E>> getDefaultDigraphFactory() {
		return getMapDigraphFactory(MapDigraph.<V,E>getDefaultVertexMapFactory(null), MapDigraph.<V,E>getDefaultEdgeMapFactory(null));
	}

	/**
	 * Factory creating <code>MapDigraph</code>.
	 * @param vertexMapFactory factory to create vertex --> edge-map maps
	 * @param edgeMapFactory factory to create edge-target --> edge-value maps
	 * @return map digraph factory
	 */
	public static <V, E> DigraphFactory<MapDigraph<V, E>> getMapDigraphFactory(
			final VertexMapFactory<V, E> vertexMapFactory,
			final EdgeMapFactory<V, E> edgeMapFactory) {
		return new DigraphFactory<MapDigraph<V, E>>() {
			@Override
			public MapDigraph<V, E> create() {
				return new MapDigraph<V, E>(vertexMapFactory, edgeMapFactory);
			}
		};
	}

	/**
	 * Vertex map factory (vertex to edge map).
	 */
	public static interface VertexMapFactory<V, E> {
		public Map<V, Map<V, E>> create();
	}

	/**
	 * Edge map factory (edge target to edge value).
	 */
	public static interface EdgeMapFactory<V, E> {
		public Map<V, E> create(V source);
	}

	private static final <V, E> VertexMapFactory<V, E> getDefaultVertexMapFactory(final Comparator<? super V> comparator) {
		return new VertexMapFactory<V, E>() {
			@Override
			public Map<V, Map<V, E>> create() {
				if (comparator == null) {
					return new LinkedHashMap<V, Map<V, E>>(16);
				} else {
					return new TreeMap<V, Map<V, E>>(comparator);
				}
			}
		};
	};

	private static final <V, E> EdgeMapFactory<V, E> getDefaultEdgeMapFactory(final Comparator<? super V> comparator) {
		return new EdgeMapFactory<V, E>() {
			@Override
			public Map<V, E> create(V ignore) {
				if (comparator == null) {
					return new LinkedHashMap<V, E>(16);
				} else {
					return new TreeMap<V, E>(comparator);
				}
			}
		};
	};

	private final VertexMapFactory<V, E> vertexMapFactory;
	private final EdgeMapFactory<V, E> edgeMapFactory;
	private final Map<V, Map<V, E>> vertexMap;

	private int edgeCount;

	/**
	 * Create digraph.
	 * {@link HashMap}s will be used as vertex/edge maps.
	 * Vertices and edge targets will be iterated in no particular order. 
	 */
	public MapDigraph() {
		this(null);
	}

	/**
	 * Create digraph.
	 * If a vertex comparator is given, {@link TreeMap}s will be used as vertex/edge maps.
	 * Vertices and edge targets will be iterated in the order given by the comparator. 
	 * @param comparator vertex comparator (may be <code>null</code>)
	 */
	public MapDigraph(final Comparator<? super V> comparator) {
		this(comparator, comparator);
	}

	/**
	 * Create digraph.
	 * If a vertex comparator is given, {@link TreeMap}s will be used as vertex maps
	 * and vertices will be iterated in the order given by the vertex comparator. 
	 * If an edge comparator is given, {@link TreeMap}s will be used as edge maps
	 * and edge targets will be iterated in the order given by the edge comparator. 
	 * @param vertexComparator
	 * @param edgeComparator
	 */
	public MapDigraph(final Comparator<? super V> vertexComparator, final Comparator<? super V> edgeComparator) {
		this(MapDigraph.<V, E> getDefaultVertexMapFactory(vertexComparator), MapDigraph.<V, E> getDefaultEdgeMapFactory(edgeComparator));
	}

	/**
	 * Create digraph.
	 * @param vertexMapFactory factory to create vertex --> edge-map maps
	 * @param edgeMapFactory factory to create edge-target --> edge-value maps
	 */
	public MapDigraph(VertexMapFactory<V, E> vertexMapFactory, EdgeMapFactory<V, E> edgeMapFactory) {
		this.vertexMapFactory = vertexMapFactory;
		this.edgeMapFactory = edgeMapFactory;

		vertexMap = vertexMapFactory.create();
	}

	@Override
	public boolean add(V vertex) {
		if (!vertexMap.containsKey(vertex)) {
			vertexMap.put(vertex, Collections.<V, E> emptyMap());
			return true;
		}
		return false;
	}

	@Override
	public E put(V source, V target, E edge) {
		Map<V, E> edgeMap = vertexMap.get(source);
		if (edgeMap == null || edgeMap.isEmpty()) {
			vertexMap.put(source, edgeMap = edgeMapFactory.create(source));
		}
		E result = edgeMap.put(target, edge);
		if (result == null) {
			add(target);
			edgeCount++;
		}
		return result;
	}

	@Override
	public E get(Object source, Object target) {
		Map<V, E> edgeMap = vertexMap.get(source);
		if (edgeMap == null) {
			return null;
		}
		return edgeMap.get(target);
	}

	@Override
	public E remove(V source, V target) {
		Map<V, E> edgeMap = vertexMap.get(source);
		if (edgeMap == null || !edgeMap.containsKey(target)) {
			return null;
		}
		E result = edgeMap.remove(target);
		edgeCount--;
		if (edgeMap.isEmpty()) {
			vertexMap.put(source, Collections.<V, E> emptyMap());
		}
		return result;
	}

	@Override
	public boolean remove(V vertex) {
		Map<V, E> edgeMap = vertexMap.get(vertex);
		if (edgeMap == null) {
			return false;
		}
		edgeCount -= edgeMap.size();
		vertexMap.remove(vertex);
		for (V source : vertexMap.keySet()) {
			remove(source, vertex);
		}
		return true;
	}

	@Override
	public void removeAll(Collection<V> vertices) {
		for (V vertex : vertices) {
			Map<V, E> edgeMap = vertexMap.get(vertex);
			if (edgeMap != null) {
				edgeCount -= edgeMap.size();
				vertexMap.remove(vertex);
			}
		}
		for (V source : vertexMap.keySet()) {
			Map<V, E> edgeMap = vertexMap.get(source);
			Iterator<V> iter = edgeMap.keySet().iterator();
			while (iter.hasNext()) {
				if (vertices.contains(iter.next())) {
					iter.remove();
					edgeCount--;
				}
			}
			if (edgeMap.isEmpty()) {
				vertexMap.put(source, Collections.<V, E> emptyMap());
			}
		}
	}

	@Override
	public boolean contains(Object source, Object target) {
		Map<V, E> edgeMap = vertexMap.get(source);
		if (edgeMap == null) {
			return false;
		}
		return edgeMap.containsKey(target);
	}

	@Override
	public boolean contains(Object vertex) {
		return vertexMap.containsKey(vertex);
	}

	@Override
	public Iterable<V> vertices() {
		if (vertexMap.isEmpty()) {
			return Collections.emptySet();
		}
		return new Iterable<V>() {
			@Override
			public Iterator<V> iterator() {
				return new Iterator<V>() {
					Iterator<V> delegate = vertexMap.keySet().iterator();
					V vertex = null;

					@Override
					public boolean hasNext() {
						return delegate.hasNext();
					}

					@Override
					public V next() {
						return vertex = delegate.next();
					}

					@Override
					public void remove() {
						Map<V, E> edgeMap = vertexMap.get(vertex);
						delegate.remove();
						edgeCount -= edgeMap.size();
						for (V source : vertexMap.keySet()) {
							MapDigraph.this.remove(source, vertex);
						}
					}
				};
			}

			@Override
			public String toString() {
				return vertexMap.keySet().toString();
			}
		};
	}

	@Override
	public Iterable<V> targets(final Object source) {
		final Map<V, E> edgeMap = vertexMap.get(source);
		if (edgeMap == null || edgeMap.isEmpty()) {
			return Collections.emptySet();
		}
		return new Iterable<V>() {
			@Override
			public Iterator<V> iterator() {
				return new Iterator<V>() {
					Iterator<V> delegate = edgeMap.keySet().iterator();

					@Override
					public boolean hasNext() {
						return delegate.hasNext();
					}

					@Override
					public V next() {
						return delegate.next();
					}

					@Override
					public void remove() {
						delegate.remove();
						edgeCount--;
						if (edgeMap.isEmpty()) {
							@SuppressWarnings("unchecked")
							V v = (V) source;
							vertexMap.put(v, Collections.<V, E> emptyMap());
						}
					}
				};
			}

			@Override
			public String toString() {
				return edgeMap.keySet().toString();
			}
		};
	}

	@Override
	public int getVertexCount() {
		return vertexMap.size();
	}

	@Override
	public int getOutDegree(Object vertex) {
		Map<V, E> edgeMap = vertexMap.get(vertex);
		if (edgeMap == null) {
			return 0;
		}
		return edgeMap.size();
	}

	@Override
	public int getEdgeCount() {
		return edgeCount;
	}

	public DigraphFactory<? extends MapDigraph<V, E>> getDigraphFactory() {
		return new DigraphFactory<MapDigraph<V, E>>() {
			@Override
			public MapDigraph<V, E> create() {
				return new MapDigraph<V, E>(vertexMapFactory, edgeMapFactory);
			}
		};
	}

	@Override
	public MapDigraph<V, E> reverse() {
		return Digraphs.<V, E, MapDigraph<V, E>> reverse(this, getDigraphFactory());
	}

	@Override
	public MapDigraph<V, E> subgraph(Set<V> vertices) {
		return Digraphs.<V, E, MapDigraph<V, E>> subgraph(this, vertices, getDigraphFactory());
	}

	@Override
	public boolean isAcyclic() {
		return Digraphs.isAcyclic(this);
	}

	@Override
	public String toString() {
		StringBuffer b = new StringBuffer();
		b.append(getClass().getName().substring(getClass().getName().lastIndexOf('.') + 1));
		b.append("(");
		Iterator<V> vertices = vertices().iterator();
		while (vertices.hasNext()) {
			V v = vertices.next();
			b.append(v);
			b.append(targets(v));
			if (vertices.hasNext()) {
				b.append(", ");
				if (b.length() > 1000) {
					b.append("...");
					break;
				}
			}
		}
		b.append(")");
		return b.toString();
	}
}
