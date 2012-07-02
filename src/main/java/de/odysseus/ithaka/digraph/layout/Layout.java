/*
 * Copyright 2008 Odysseus Software GmbH, Frankfurt am Main/Germany.
 */
package de.odysseus.ithaka.digraph.layout;

import de.odysseus.ithaka.digraph.DoubledDigraph;

public class Layout<V,E> {
	private final DoubledDigraph<? extends LayoutNode<V>,? extends LayoutArc<V,E>> digraph;
	private final LayoutDimension dimension;
	private final LayoutPoint point;

	public Layout(DoubledDigraph<? extends LayoutNode<V>,? extends LayoutArc<V,E>> digraph, LayoutDimension dimension) {
		this.digraph = digraph;
		this.dimension = dimension;
		this.point = new LayoutPoint(0, 0);
	}

	public DoubledDigraph<? extends LayoutNode<V>,? extends LayoutArc<V,E>> getLayoutGraph() {
		return digraph;
	}

	public LayoutDimension getDimension() {
		return dimension;
	}
	
	public LayoutPoint getPoint() {
		return point;
	}
	
	public void translate(int dx, int dy) {
		for (LayoutNode<V> source : digraph.vertices()) {
			source.getPoint().translate(dx, dy);
			for (LayoutNode<V> target : digraph.targets(source)) {
				LayoutArc<V, E> arc = digraph.get(source, target);
				arc.getStartPoint().translate(dx, dy);
				arc.getEndPoint().translate(dx, dy);
				for (LayoutPoint point : arc.getBendPoints()) {
					point.translate(dx, dy);
				}
			}
		}
		point.translate(dx, dy);
	}
}