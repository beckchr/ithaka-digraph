/*
 * Copyright 2008 Odysseus Software GmbH, Frankfurt am Main/Germany.
 */
package de.odysseus.ithaka.digraph.layout;

import de.odysseus.ithaka.digraph.Digraph;

public interface LayoutBuilder<V, E> {
	/**
	 * Layout graph...
	 */
	public Layout<V, E> layout(Digraph<V, E> digraph, LayoutDimensionProvider<V> dimensions);
}