/*
 * Copyright 2012 Odysseus Software GmbH, Frankfurt am Main/Germany.
 */
package de.odysseus.ithaka.digraph.graphml;

public enum GraphMLPropertyDomain {
	Graph,
	Node,
	Edge;

	@Override
	public String toString() {
		return name().toLowerCase();
	}
}
