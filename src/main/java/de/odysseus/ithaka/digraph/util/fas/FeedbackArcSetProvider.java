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
package de.odysseus.ithaka.digraph.util.fas;

import de.odysseus.ithaka.digraph.Digraph;
import de.odysseus.ithaka.digraph.EdgeWeights;

/**
 * Feedback arc set provider interface.
 */
public interface FeedbackArcSetProvider {
	/**
	 * Calculate feedback arc set.
	 * @param digraph
	 * @param weights
	 * @param policy
	 * @return feedback arc set
	 */
	public <V, E> FeedbackArcSet<V, E> getFeedbackArcSet(
			Digraph<V, E> digraph,
			EdgeWeights<? super V> weights,
			FeedbackArcSetPolicy policy);
}