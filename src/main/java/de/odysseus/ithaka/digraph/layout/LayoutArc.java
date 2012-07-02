/*
 * Copyright 2008 Odysseus Software GmbH, Frankfurt am Main/Germany.
 */
package de.odysseus.ithaka.digraph.layout;

public interface LayoutArc<V,E> {
	/**
	 * @return original edge data
	 */
	public E getEdge();

	/**
	 * @return start point
	 */
	public LayoutPoint getStartPoint();

	/**
	 * @return source node
	 */
	public LayoutNode<V> getSource();

	/**
	 * @return end point
	 */
	public LayoutPoint getEndPoint();

	/**
	 * @return target node
	 */
	public LayoutNode<V> getTarget();

	/**
	 * @return bend points
	 */
	public Iterable<LayoutPoint> getBendPoints();


	public boolean isFeedback();
}
