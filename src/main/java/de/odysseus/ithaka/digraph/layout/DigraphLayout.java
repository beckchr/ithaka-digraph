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

public class DigraphLayout<V,E> {
	private final DoubledDigraph<? extends DigraphLayoutNode<V>,? extends DigraphLayoutArc<V,E>> digraph;
	private final DigraphLayoutDimension dimension;
	private final DigraphLayoutPoint point;

	public DigraphLayout(DoubledDigraph<? extends DigraphLayoutNode<V>,? extends DigraphLayoutArc<V,E>> digraph, DigraphLayoutDimension dimension) {
		this.digraph = digraph;
		this.dimension = dimension;
		this.point = new DigraphLayoutPoint(0, 0);
	}

	public DoubledDigraph<? extends DigraphLayoutNode<V>,? extends DigraphLayoutArc<V,E>> getLayoutGraph() {
		return digraph;
	}

	public DigraphLayoutDimension getDimension() {
		return dimension;
	}
	
	public DigraphLayoutPoint getPoint() {
		return point;
	}
	
	public void translate(int dx, int dy) {
		for (DigraphLayoutNode<V> source : digraph.vertices()) {
			source.getPoint().translate(dx, dy);
			for (DigraphLayoutNode<V> target : digraph.targets(source)) {
				DigraphLayoutArc<V, E> arc = digraph.get(source, target);
				arc.getStartPoint().translate(dx, dy);
				arc.getEndPoint().translate(dx, dy);
				for (DigraphLayoutPoint point : arc.getBendPoints()) {
					point.translate(dx, dy);
				}
			}
		}
		point.translate(dx, dy);
	}
}