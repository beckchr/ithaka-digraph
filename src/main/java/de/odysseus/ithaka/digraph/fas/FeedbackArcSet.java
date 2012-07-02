/*
 * Copyright 2008 Odysseus Software GmbH, Frankfurt am Main/Germany.
 */
package de.odysseus.ithaka.digraph.fas;

import de.odysseus.ithaka.digraph.Digraph;
import de.odysseus.ithaka.digraph.UnmodifiableDigraph;

public class FeedbackArcSet<V, E> extends UnmodifiableDigraph<V, E> {
	private final FeedbackArcSetPolicy policy;
	private final boolean exact;
	private final int weight;
	
	public FeedbackArcSet(Digraph<V, E> feedback, int weight, FeedbackArcSetPolicy policy, boolean exact) {
		super(feedback);
		this.weight = weight;
		this.policy = policy;
		this.exact = exact;
	}

	public boolean isExact() {
		return exact;
	}
	
	public int getWeight() {
		return weight;
	}
	
	public FeedbackArcSetPolicy getPolicy() {
		return policy;
	}
}
