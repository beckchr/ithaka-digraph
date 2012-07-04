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

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import de.odysseus.ithaka.digraph.Digraph;
import de.odysseus.ithaka.digraph.DigraphProvider;
import de.odysseus.ithaka.digraph.graphml.GraphMLExporter;
import de.odysseus.ithaka.digraph.graphml.GraphMLProperty;
import de.odysseus.ithaka.digraph.graphml.GraphMLPropertyDomain;
import de.odysseus.ithaka.digraph.graphml.GraphMLProvider;
import de.odysseus.ithaka.digraph.graphml.SimpleGraphMLProvider;
import de.odysseus.ithaka.digraph.layout.LayoutArc;
import de.odysseus.ithaka.digraph.layout.LayoutBuilder;
import de.odysseus.ithaka.digraph.layout.LayoutNode;

public class YFilesGraphML<V, E> {
	private static final String SCHEMA_LOCATION = "http://www.yworks.com/xml/schema/graphml/1.1/ygraphml.xsd";
	
	private final LayoutTree<V, E> layoutTree;
	private final SimpleGraphMLProvider<LayoutNode<V>, LayoutArc<V,E>, Digraph<? extends LayoutNode<V>,? extends LayoutArc<V,E>>> provider;
	
	static abstract class GraphMLPropertyDelegate<S, T> implements GraphMLProperty<T> {
		final GraphMLProperty<S> property;

		public GraphMLPropertyDelegate(GraphMLProperty<S> property) {
			this.property = property;
		}
		@Override
		public GraphMLPropertyDomain getDomain() {
			return property.getDomain();
		}
		@Override
		public String getNamespaceURI() {
			return property.getNamespaceURI();
		}
		@Override
		public String getPrefix() {
			return property.getPrefix();
		}
		@Override
		public void writeKey(XMLStreamWriter writer, String id) throws XMLStreamException {
			property.writeKey(writer, id);
		}
	}

	public YFilesGraphML(
			Digraph<V, E> digraph,
			LayoutBuilder<V, E> builder,
			LabelResolver<? super V> labels,
			Font font) {
		this(digraph, builder, labels, null, font, font);
	}

	public YFilesGraphML(
			Digraph<V, E> digraph,
			LayoutBuilder<V, E> builder,
			LabelResolver<? super V> nodeLabels,
			LabelResolver<? super E> edgeLabels,
			Font nodeFont,
			Font edgeFont) {
			this(digraph, null, builder, nodeLabels, edgeLabels, nodeFont, edgeFont, false);
	}

	public YFilesGraphML(
			Digraph<V, E> digraph,
			DigraphProvider<V, Digraph<V,E>> subgraphs,
			LayoutBuilder<V, E> builder,
			LabelResolver<? super V> nodeLabels,
			Font nodeFont,
			boolean groupNodes) {
		this(digraph, subgraphs, builder, nodeLabels, null, nodeFont, null, groupNodes);
	}

	public YFilesGraphML(
			Digraph<V, E> digraph,
			DigraphProvider<V, Digraph<V,E>> subgraphs,
			LayoutBuilder<V, E> builder,
			LabelResolver<? super V> nodeLabels,
			LabelResolver<? super E> edgeLabels,
			Font nodeFont,
			Font edgeFont,
			boolean groupNodes) {
		this(digraph, subgraphs, new SimpleGraphMLProvider<V, E, Digraph<V,E>>(), builder, nodeLabels, edgeLabels, nodeFont, edgeFont, groupNodes);
	}

	public YFilesGraphML(
			Digraph<V, E> digraph,
			DigraphProvider<V, Digraph<V,E>> subgraphs,
			GraphMLProvider<V, E, Digraph<V,E>> delegate,
			LayoutBuilder<V, E> builder,
			LabelResolver<? super V> nodeLabels,
			LabelResolver<? super E> edgeLabels,
			Font nodeFont,
			Font edgeFont,
			boolean groupNodes) {
		this(
			delegate,
			new LayoutTree<V, E>(digraph, subgraphs, builder, nodeLabels, nodeFont),
			new NodeGraphicsProperty<V>(nodeLabels, nodeFont, subgraphs, groupNodes),
			new EdgeGraphicsProperty<V, E>(edgeLabels, edgeFont));
	}

	public YFilesGraphML(
			GraphMLProvider<V, E, Digraph<V,E>> delegate,
			LayoutTree<V, E> layoutTree,
			NodeGraphicsProperty<V> nodeGraphics,
			EdgeGraphicsProperty<V, E> edgeGraphics) {
		this.layoutTree = layoutTree;
		this.provider = new SimpleGraphMLProvider<LayoutNode<V>, LayoutArc<V,E>, Digraph<? extends LayoutNode<V>,? extends LayoutArc<V,E>>>(SCHEMA_LOCATION);

		for (final GraphMLProperty<Digraph<V,E>> graphProperty : delegate.getGraphProperties()) {
			provider.addGraphProperty(new GraphMLPropertyDelegate<Digraph<V,E>, Digraph<? extends LayoutNode<V>,? extends LayoutArc<V,E>>>(graphProperty) {
				@Override
				public void writeData(XMLStreamWriter writer, String id, Digraph<? extends LayoutNode<V>, ? extends LayoutArc<V, E>> value) throws XMLStreamException {
					property.writeData(writer, id, findDigraph(YFilesGraphML.this.layoutTree, value));
				}
				Digraph<V,E> findDigraph(LayoutTree<V, E> layoutTree, Digraph<? extends LayoutNode<V>, ? extends LayoutArc<V, E>> layoutGraph) {
					if (layoutTree.getLayout().getLayoutGraph() == layoutGraph) {
						return layoutTree.getDigraph();
					}
					for (V subtreeVertex : layoutTree.subtreeVertices()) {
						Digraph<V,E> result = findDigraph(layoutTree.getSubtree(subtreeVertex), layoutGraph);
						if (result != null) {
							return result;
						}
					}
					return null;
				}
			});
		}

		for (final GraphMLProperty<V> nodeProperty : delegate.getNodeProperties()) {
			provider.addNodeProperty(new GraphMLPropertyDelegate<V, LayoutNode<V>>(nodeProperty) {
				@Override
				public void writeData(XMLStreamWriter writer, String id, LayoutNode<V> value) throws XMLStreamException {
					property.writeData(writer, id, value.getVertex());
				}
			});
		}

		for (final GraphMLProperty<E> edgeProperty : delegate.getEdgeProperties()) {
			provider.addEdgeProperty(new GraphMLPropertyDelegate<E, LayoutArc<V,E>>(edgeProperty) {
				@Override
				public void writeData(XMLStreamWriter writer, String id, LayoutArc<V,E> value) throws XMLStreamException {
					property.writeData(writer, id, value.getEdge());
				}
			});
		}

		provider.addNodeProperty(nodeGraphics);
		provider.addEdgeProperty(edgeGraphics);
	}
	
	private DigraphProvider<? super LayoutNode<V>, Digraph<? extends LayoutNode<V>, ? extends LayoutArc<V, E>>> getSubgraphProvider() {
		return new DigraphProvider<LayoutNode<V>, Digraph<? extends LayoutNode<V>,? extends LayoutArc<V,E>>>() {
			@Override
			public Digraph<? extends LayoutNode<V>, ? extends LayoutArc<V, E>> get(LayoutNode<V> node) {
				LayoutTree<V, E> subtree = layoutTree.find(node.getVertex());
				return subtree == null ? null : subtree.getLayout().getLayoutGraph();
			}
		};
	}
	
	public void export(XMLStreamWriter writer) throws XMLStreamException {
		new GraphMLExporter().export(provider, layoutTree.getLayout().getLayoutGraph(), getSubgraphProvider(), writer);
	}
}
