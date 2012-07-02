/*
 * Copyright 2008 Odysseus Software GmbH, Frankfurt am Main/Germany.
 */
package de.odysseus.ithaka.digraph.layout;

public interface LayoutDimensionProvider<V> {
	/**
	 * @param vertex
	 * @return vertex dimensions
	 */
	public LayoutDimension getDimension(V vertex);
}
