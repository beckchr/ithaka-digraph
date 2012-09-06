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
package de.odysseus.ithaka.digraph.io.graphml.yfiles;

import java.awt.Font;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import de.odysseus.ithaka.digraph.Digraph;
import de.odysseus.ithaka.digraph.DigraphProvider;
import de.odysseus.ithaka.digraph.io.graphml.GraphMLExporter;
import de.odysseus.ithaka.digraph.io.graphml.GraphMLProperty;
import de.odysseus.ithaka.digraph.io.graphml.GraphMLPropertyDomain;
import de.odysseus.ithaka.digraph.io.graphml.GraphMLProvider;
import de.odysseus.ithaka.digraph.io.graphml.SimpleGraphMLProvider;
import de.odysseus.ithaka.digraph.layout.DigraphLayoutArc;
import de.odysseus.ithaka.digraph.layout.DigrpahLayoutBuilder;
import de.odysseus.ithaka.digraph.layout.DigraphLayoutNode;

public class YFilesGraphML<V, E> {
	private static final String SCHEMA_LOCATION = "http://www.yworks.com/xml/schema/graphml/1.1/ygraphml.xsd";
	
	private final LayoutTree<V, E> layoutTree;
	private final SimpleGraphMLProvider<DigraphLayoutNode<V>, DigraphLayoutArc<V,E>, Digraph<? extends DigraphLayoutNode<V>,? extends DigraphLayoutArc<V,E>>> provider;
	
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
			DigrpahLayoutBuilder<V, E> builder,
			LabelResolver<? super V> labels,
			Font font) {
		this(digraph, builder, labels, null, font, font);
	}

	public YFilesGraphML(
			Digraph<V, E> digraph,
			DigrpahLayoutBuilder<V, E> builder,
			LabelResolver<? super V> nodeLabels,
			LabelResolver<? super E> edgeLabels,
			Font nodeFont,
			Font edgeFont) {
			this(digraph, null, builder, nodeLabels, edgeLabels, nodeFont, edgeFont, false);
	}

	public YFilesGraphML(
			Digraph<V, E> digraph,
			DigraphProvider<V, Digraph<V,E>> subgraphs,
			DigrpahLayoutBuilder<V, E> builder,
			LabelResolver<? super V> nodeLabels,
			Font nodeFont,
			boolean groupNodes) {
		this(digraph, subgraphs, builder, nodeLabels, null, nodeFont, null, groupNodes);
	}

	public YFilesGraphML(
			Digraph<V, E> digraph,
			DigraphProvider<V, Digraph<V,E>> subgraphs,
			DigrpahLayoutBuilder<V, E> builder,
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
			DigrpahLayoutBuilder<V, E> builder,
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
		this.provider = new SimpleGraphMLProvider<DigraphLayoutNode<V>, DigraphLayoutArc<V,E>, Digraph<? extends DigraphLayoutNode<V>,? extends DigraphLayoutArc<V,E>>>(SCHEMA_LOCATION);

		for (final GraphMLProperty<Digraph<V,E>> graphProperty : delegate.getGraphProperties()) {
			provider.addGraphProperty(new GraphMLPropertyDelegate<Digraph<V,E>, Digraph<? extends DigraphLayoutNode<V>,? extends DigraphLayoutArc<V,E>>>(graphProperty) {
				@Override
				public void writeData(XMLStreamWriter writer, String id, Digraph<? extends DigraphLayoutNode<V>, ? extends DigraphLayoutArc<V, E>> value) throws XMLStreamException {
					property.writeData(writer, id, findDigraph(YFilesGraphML.this.layoutTree, value));
				}
				Digraph<V,E> findDigraph(LayoutTree<V, E> layoutTree, Digraph<? extends DigraphLayoutNode<V>, ? extends DigraphLayoutArc<V, E>> layoutGraph) {
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
			provider.addNodeProperty(new GraphMLPropertyDelegate<V, DigraphLayoutNode<V>>(nodeProperty) {
				@Override
				public void writeData(XMLStreamWriter writer, String id, DigraphLayoutNode<V> value) throws XMLStreamException {
					property.writeData(writer, id, value.getVertex());
				}
			});
		}

		for (final GraphMLProperty<E> edgeProperty : delegate.getEdgeProperties()) {
			provider.addEdgeProperty(new GraphMLPropertyDelegate<E, DigraphLayoutArc<V,E>>(edgeProperty) {
				@Override
				public void writeData(XMLStreamWriter writer, String id, DigraphLayoutArc<V,E> value) throws XMLStreamException {
					property.writeData(writer, id, value.getEdge());
				}
			});
		}

		provider.addNodeProperty(nodeGraphics);
		provider.addEdgeProperty(edgeGraphics);
	}
	
	private DigraphProvider<? super DigraphLayoutNode<V>, Digraph<? extends DigraphLayoutNode<V>, ? extends DigraphLayoutArc<V, E>>> getSubgraphProvider() {
		return new DigraphProvider<DigraphLayoutNode<V>, Digraph<? extends DigraphLayoutNode<V>,? extends DigraphLayoutArc<V,E>>>() {
			@Override
			public Digraph<? extends DigraphLayoutNode<V>, ? extends DigraphLayoutArc<V, E>> get(DigraphLayoutNode<V> node) {
				LayoutTree<V, E> subtree = layoutTree.find(node.getVertex());
				return subtree == null ? null : subtree.getLayout().getLayoutGraph();
			}
		};
	}
	
	public void export(XMLStreamWriter writer) throws XMLStreamException {
		new GraphMLExporter().export(provider, layoutTree.getLayout().getLayoutGraph(), getSubgraphProvider(), writer);
	}
}
