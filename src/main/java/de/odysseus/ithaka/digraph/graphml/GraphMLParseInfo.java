/*
 * Copyright 2012 Odysseus Software GmbH, Frankfurt am Main/Germany.
 */
package de.odysseus.ithaka.digraph.graphml;

public enum GraphMLParseInfo {
	MaxInDegree,
	MaxOutDegree,
	Order,
	Nodes,
	NodeIds,
	Edges,
	EdgeIds,
	InDegree,
	OutDegree;
	
	@Override
	public String toString() {
		return "parse." + name().toLowerCase();
	}
}
