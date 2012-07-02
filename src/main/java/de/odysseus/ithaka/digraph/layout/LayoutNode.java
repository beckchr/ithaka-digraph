/*
 * Copyright 2008 Odysseus Software GmbH, Frankfurt am Main/Germany.
 */
package de.odysseus.ithaka.digraph.layout;

public interface LayoutNode<V> {
	/**
	 * @return original vertex data
	 */
	public V getVertex();

	/**
	 * @return width and height
	 */
	public LayoutDimension getDimension();
	
	/**
	 * @return upper left point
	 */
	public LayoutPoint getPoint();
}
