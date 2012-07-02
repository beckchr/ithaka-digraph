/*
 * Copyright 2012 Odysseus Software GmbH, Frankfurt am Main/Germany.
 */
package de.odysseus.ithaka.digraph.graphml;

public enum GraphMLPropertyType {
	Boolean,
	Int,
	Long,
	Float,
	Double,
	String;
	
	@Override
	public String toString() {
		return name().toLowerCase();
	}
}
