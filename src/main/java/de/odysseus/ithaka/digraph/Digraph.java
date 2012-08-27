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
 * Directed graph interface.
 *
 * @param <V> vertex type
 * @param <E> edge type
 */
public interface Digraph<V, E> {
	/**
	 * Get an edge.
	 * @param source source vertex
	 * @param target target vertex
	 * @return edge value (<code>null</code> if there is no edge from <code>source</code> to <code>target</code>)
	 */
	public E get(Object source, Object target);

	/**
	 * Edge test. 
	 * @param source source vertex
	 * @param target target vertex
	 * @return <code>true</code> iff this digraph contains an edge from <code>source</code> to <code>target</code>
	 */
	public boolean contains(Object source, Object target);

	/**
	 * Vertex test
	 * @param vertex vertex
	 * @return <code>true</code> iff this digraph contains <code>vertex</code>
	 */
	public boolean contains(Object vertex);

	/**
	 * Add vertex.
	 * @param vertex
	 * @return <code>true</code> iff <code>vertex</code> has been added
	 */
	public boolean add(V vertex);

	/**
	 * Put an edge.
	 * Vertices are added automatically if they appear in an edge.
	 * @param source source vertex
	 * @param target target vertex
	 * @param edge edge value
	 * @return edge value that has been previously set (<code>null</code> if there was no edge from <code>source</code>
	 * to <code>target</code>)
	 */
	public E put(V source, V target, E edge);

	/**
	 * Remove an edge.
	 * @param source source vertex
	 * @param target target vertex
	 * @return edge value that has been previously set (<code>null</code> if there was no edge from <code>source</code>
	 * to <code>target</code>)
	 */
	public E remove(V source, V target);
	
	/**
	 * Remove a vertex.
	 * @param vertex vertex
	 * @return <code>true</code> iff this digraph contained <code>vertex</code>
	 */
	public boolean remove(V vertex);

	/**
	 * Remove all vertices.
	 * @param vertices vertices
	 */
	public void removeAll(Collection<V> vertices);

	/**
	 * Iterate over vertices.
	 * @return vertices
	 */
	public Iterable<V> vertices();

	/**
	 * Iterate over edge targets for given source vertex. 
	 * @param source source vertex
	 * @return edge targets of edges starting at <code>source</code>
	 */
	public Iterable<V> targets(Object source);

	/**
	 * @return number of vertices in this digraph
	 */
	public int getVertexCount();

	/**
	 * @return number of edges starting at <code>vertex</code>
	 */
	public int getOutDegree(Object vertex);

	/**
	 * @return number of edges in this digraph
	 */
	public int getEdgeCount();
	
	/**
	 * @return <code>true</code> iff this digraph is acyclic (i.e. it is a DAG)
	 */
	public boolean isAcyclic();
	
	/**
	 * Get reverse digraph (same vertices, with edges reversed).
	 * @return reverse digraph
	 */
	public Digraph<V,E> reverse();
	
	/**
	 * Get induced subgraph (with vertices in this digraph and the given vertex set and edges that appear in this digraph over the given vertex set).
	 * @param vertices
	 * @return subgraph
	 */
	public Digraph<V,E> subgraph(Set<V> vertices);
}