package de.odysseus.ithaka.digraph.graphml;

/*
 * Copyright 2012 Odysseus Software GmbH, Frankfurt am Main/Germany.
 */
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import de.odysseus.ithaka.digraph.Digraph;
import de.odysseus.ithaka.digraph.DigraphProvider;

/**
 * Export digraph as GraphML.
 * @param <V> vertex type
 * @param <E> edge type
 */
public class GraphMLExporter {
	static class CanonicalIdGenerator {
		private int dataIndex = 0;
		private int graphIndex = 0;
		private int nodeIndex = 0;
		private int edgeIndex = 0;
		private String format(char prefix, int index) {
			return String.format("%c%d", prefix, index);
		}
		String nextDataId() {
			return format('d', dataIndex++);
		}
		String nextGraphId() {
			return format('g', graphIndex++);
		}
		String nextNodeId() {
			return format('n', nodeIndex++);
		}
		String nextEdgeId() {
			return format('e', edgeIndex++);
		}
	}
	
	static final String GRAPHML_NAMESPACE_URI = "http://graphml.graphdrawing.org/xmlns";
	
	private final Set<GraphMLParseInfo> parseInfos;

	public GraphMLExporter() {
		this(EnumSet.noneOf(GraphMLParseInfo.class));
	}

	public GraphMLExporter(EnumSet<GraphMLParseInfo> parseInfos) {
		this.parseInfos = parseInfos;
	}
	
	private <V, E, G extends Digraph<? extends V, ? extends E>> void write(
			GraphMLProvider<V, E, G> provider,
			DigraphProvider<? super V, G> subgraphs,
			XMLStreamWriter writer,
			G digraph,
			Map<GraphMLProperty<?>, String> propertyIds,
			CanonicalIdGenerator idGenerator) throws XMLStreamException {
		writer.writeStartElement("graph");
		writer.writeAttribute("id", idGenerator.nextGraphId());
		writer.writeAttribute("edgedefault", "directed");
		if (parseInfos.contains(GraphMLParseInfo.Order)) {
			writer.writeAttribute("parse.order", "nodesfirst");
		}
		if (parseInfos.contains(GraphMLParseInfo.Nodes)) {
			writer.writeAttribute("parse.nodes", String.valueOf(digraph.getVertexCount()));
		}
		if (parseInfos.contains(GraphMLParseInfo.NodeIds)) {
			writer.writeAttribute("parse.nodeids", "canonical");
		}
		if (parseInfos.contains(GraphMLParseInfo.Edges)) {
			writer.writeAttribute("parse.edges", String.valueOf(digraph.getEdgeCount()));
		}
		if (parseInfos.contains(GraphMLParseInfo.EdgeIds)) {
			writer.writeAttribute("parse.edgeids", "canonical");
		}
		Digraph<?, ?> reverse = null;
		if (parseInfos.contains(GraphMLParseInfo.InDegree) || parseInfos.contains(GraphMLParseInfo.MaxInDegree)) {
			reverse = digraph.reverse();
		}
		if (parseInfos.contains(GraphMLParseInfo.MaxInDegree)) {
			int maxInDegree = 0;
			for (V vertex : digraph.vertices()) {
				maxInDegree = Math.max(maxInDegree, reverse.getOutDegree(vertex));
			}
			writer.writeAttribute("parse.maxindegree", String.valueOf(maxInDegree));
		}
		if (parseInfos.contains(GraphMLParseInfo.MaxOutDegree)) {
			int maxOutDegree = 0;
			for (V vertex : digraph.vertices()) {
				maxOutDegree = Math.max(maxOutDegree, digraph.getOutDegree(vertex));
			}
			writer.writeAttribute("parse.maxoutdegree", String.valueOf(maxOutDegree));
		}
		for (GraphMLProperty<G> property : provider.getGraphProperties()) {
			property.writeData(writer, propertyIds.get(property), digraph);
		}
		Map<V, String> nodeIds = new HashMap<V, String>();
		for (V vertex : digraph.vertices()) {
			writer.writeStartElement(GRAPHML_NAMESPACE_URI, "node");
			String id = idGenerator.nextNodeId();
			writer.writeAttribute("id", id);
			if (parseInfos.contains(GraphMLParseInfo.InDegree)) {
				writer.writeAttribute("parse.indegree", String.valueOf(reverse.getOutDegree(vertex)));
			}
			if (parseInfos.contains(GraphMLParseInfo.OutDegree)) {
				writer.writeAttribute("parse.outdegree", String.valueOf(digraph.getOutDegree(vertex)));
			}
			nodeIds.put(vertex, id);
			for (GraphMLProperty<V> property : provider.getNodeProperties()) {
				property.writeData(writer, propertyIds.get(property), vertex);
			}
			G subgraph = subgraphs == null ? null : subgraphs.get(vertex);
			if (subgraph != null) {
				write(provider, subgraphs, writer, subgraph, propertyIds, idGenerator);
			}
			writer.writeEndElement(); // node
		}
		for (V source : digraph.vertices()) {
			for (V target : digraph.targets(source)) {
				writer.writeStartElement(GRAPHML_NAMESPACE_URI, "edge");
				E edge = digraph.get(source, target);
				if (parseInfos.contains(GraphMLParseInfo.EdgeIds)) {
					writer.writeAttribute("id",  idGenerator.nextEdgeId());
				}
				writer.writeAttribute("source", nodeIds.get(source));
				writer.writeAttribute("target", nodeIds.get(target));
				for (GraphMLProperty<E> property : provider.getEdgeProperties()) {
					property.writeData(writer, propertyIds.get(property), edge);
				}
				writer.writeEndElement(); // edge
			}
		}
		writer.writeEndElement(); // graph
	}

	public <V, E, G extends Digraph<? extends V, ? extends E>> void export(
			GraphMLProvider<V, E, G> provider,
			G digraph,
			DigraphProvider<? super V, G> subgraphs,
			XMLStreamWriter writer) throws XMLStreamException {
		writer.writeStartDocument("UTF-8", "1.0");
		writer.setDefaultNamespace(GRAPHML_NAMESPACE_URI);
		writer.writeStartElement(GRAPHML_NAMESPACE_URI, "graphml");
		Set<QName> nsdecls = new LinkedHashSet<QName>();
		nsdecls.add(new QName(GRAPHML_NAMESPACE_URI, XMLConstants.DEFAULT_NS_PREFIX));
		for (GraphMLProperty<?> property : provider.getGraphProperties()) {
			if (property.getNamespaceURI() != null) {
				nsdecls.add(new QName(property.getNamespaceURI(), property.getPrefix()));
			}
		}
		for (GraphMLProperty<?> property : provider.getNodeProperties()) {
			if (property.getNamespaceURI() != null) {
				nsdecls.add(new QName(property.getNamespaceURI(), property.getPrefix()));
			}
		}
		for (GraphMLProperty<?> property : provider.getEdgeProperties()) {
			if (property.getNamespaceURI() != null) {
				nsdecls.add(new QName(property.getNamespaceURI(), property.getPrefix()));
			}
		}
		for (QName extension : nsdecls) {
			writer.writeNamespace(extension.getLocalPart(), extension.getNamespaceURI());
		}
		if (provider.getSchemaLocation() != null) {
			writer.writeNamespace("xsi", XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
			writer.writeAttribute(
					XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "schemaLocation",
					GRAPHML_NAMESPACE_URI + " " + provider.getSchemaLocation());
		}
		CanonicalIdGenerator idGenerator = new CanonicalIdGenerator();
		Map<GraphMLProperty<?>, String> propertyIds = new HashMap<GraphMLProperty<?>, String>();
		for (GraphMLProperty<?> property : provider.getGraphProperties()) {
			String id = idGenerator.nextDataId();
			propertyIds.put(property, id);
			property.writeKey(writer, id);
		}
		for (GraphMLProperty<?> property : provider.getNodeProperties()) {
			String id = idGenerator.nextDataId();
			propertyIds.put(property, id);
			property.writeKey(writer, id);
		}
		for (GraphMLProperty<?> property : provider.getEdgeProperties()) {
			String id = idGenerator.nextDataId();
			propertyIds.put(property, id);
			property.writeKey(writer, id);
		}

		write(provider, subgraphs, writer, digraph, propertyIds, idGenerator);

		writer.writeEndElement(); // graphml
		writer.writeEndDocument();
		writer.close();
	}
}
