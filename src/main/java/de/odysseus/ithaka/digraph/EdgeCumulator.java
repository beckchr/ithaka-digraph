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

/**
 * Edge cumulator interface.
 * 
 * @param <V> vertex type
 * @param <E> edge type
 * @param <T> operand type
 */
public interface EdgeCumulator<V,E,T> {
	/**
	 * Add <code>operand</code> to edge <code>source --edge--&gt; target</code>.
	 * @param source source vertex
	 * @param target target target
	 * @param edge edge value
	 * @param operand edge operand
	 * @return <code>edge + operand</code>
	 */
	public E add(V source, V target, E edge, T operand);
}