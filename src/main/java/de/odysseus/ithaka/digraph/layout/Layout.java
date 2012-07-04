/*
 * Copyright 2012 Odysseus Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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