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
package de.odysseus.ithaka.digraph.graphml.yfiles;

import java.awt.Font;
import java.awt.Insets;
import java.util.HashMap;
import java.util.Map;

import de.odysseus.ithaka.digraph.Digraph;
import de.odysseus.ithaka.digraph.DigraphProvider;
import de.odysseus.ithaka.digraph.layout.Layout;
import de.odysseus.ithaka.digraph.layout.LayoutBuilder;
import de.odysseus.ithaka.digraph.layout.LayoutDimension;
import de.odysseus.ithaka.digraph.layout.LayoutDimensionProvider;
import de.odysseus.ithaka.digraph.layout.LayoutNode;

public class LayoutTree<V, E> {
	private final Digraph<V,E> digraph;
	private final Layout<V, E> layout;
	private final Map<V, LayoutTree<V,E>> children;
	
	public LayoutTree(
			Digraph<V,E> digraph,
			DigraphProvider<V, Digraph<V,E>> subgraphProvider,
			LayoutBuilder<V, E> builder,
			LabelResolver<? super V> labels,
			Font font) {
		this(digraph, subgraphProvider, builder, new LabelDimensionProvider<V>(labels, font));
	}

	public LayoutTree(
			Digraph<V,E> digraph,
			DigraphProvider<V, Digraph<V,E>> subgraphProvider,
			LayoutBuilder<V, E> builder,
			LayoutDimensionProvider<? super V> labelProvider) {
		this(digraph, subgraphProvider, builder, labelProvider, new Insets(15 + 10, 10, 10, 10));
	}

	public LayoutTree(
			Digraph<V,E> digraph,
			final DigraphProvider<V, Digraph<V,E>> subgraphProvider,
			final LayoutBuilder<V, E> builder,
			final LayoutDimensionProvider<? super V> labelProvider,
			final Insets insets) {
		this.digraph = digraph;
		this.children = new HashMap<V, LayoutTree<V,E>>();
		this.layout = builder.layout(digraph, new LayoutDimensionProvider<V>() {
			@Override
			public LayoutDimension getDimension(V vertex) {
				LayoutDimension dimension = labelProvider.getDimension(vertex);
				if (vertex != null && subgraphProvider != null) {
					Digraph<V,E> subgraph = subgraphProvider.get(vertex);
					if (subgraph != null) {
						LayoutTree<V,E> subtree = new LayoutTree<V,E>(subgraph, subgraphProvider, builder, labelProvider, insets);
						children.put(vertex, subtree);
						LayoutDimension subtreeDimension = subtree.layout.getDimension();
						int width = Math.max(dimension.w, subtreeDimension.w + insets.left + insets.right);
						int height = subtreeDimension.h + insets.top + insets.bottom;
						return new LayoutDimension(width, height);
					}
				}
				return dimension;
			}
		});
		for (LayoutNode<V> layoutNode : layout.getLayoutGraph().vertices()) {
			LayoutTree<V,E> child = children.get(layoutNode.getVertex());
			if (child != null) {
				child.layout.translate(layoutNode.getPoint().x + insets.left, layoutNode.getPoint().y + insets.top);
			}
		}
	}
	
	public Layout<V, E> getLayout() {
		return layout;
	}
	
	public Digraph<V, E> getDigraph() {
		return digraph;
	}
	
	public LayoutTree<V, E> getSubtree(V vertex) {
		return children.get(vertex);
	}
	
	public Iterable<V> subtreeVertices() {
		return children.keySet();
	}

	public LayoutTree<V, E> find(V vertex) {
		if (children.containsKey(vertex)) {
			return children.get(vertex);
		}
		for (LayoutTree<V,E> child : children.values()) {
			LayoutTree<V, E> result = child.find(vertex);
			if (result != null) {
				return result;
			}
		}
		return null;
	}
}
