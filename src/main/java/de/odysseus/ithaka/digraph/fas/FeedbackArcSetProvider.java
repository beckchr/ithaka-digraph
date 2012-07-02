/*
 * Copyright 2008 Odysseus Software GmbH, Frankfurt am Main/Germany.
 */
package de.odysseus.ithaka.digraph.fas;

import de.odysseus.ithaka.digraph.Digraph;
import de.odysseus.ithaka.digraph.EdgeWeights;

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