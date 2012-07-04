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