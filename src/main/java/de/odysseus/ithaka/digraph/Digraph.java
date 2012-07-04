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