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

import de.odysseus.ithaka.digraph.DigraphProvider;
import de.odysseus.ithaka.digraph.graphml.GraphMLPropertyDomain;
import de.odysseus.ithaka.digraph.layout.LayoutNode;

public class NodeGraphicsProperty<V> extends AbstractGraphicsProperty<LayoutNode<V>> {
	private final LabelResolver<? super V> labelProvider;
	private final DigraphProvider<V, ?> subgraphs;
	private final boolean groupNodes;

	public NodeGraphicsProperty(LabelResolver<? super V> labelProvider, Font font, DigraphProvider<V, ?> subgraphs, boolean groupNodes) {
		super(GraphMLPropertyDomain.Node, "nodegraphics", font);
		this.labelProvider = labelProvider;
		this.subgraphs = subgraphs;
		this.groupNodes = groupNodes;
	}

	@Override
	protected void writeDataContent(XMLStreamWriter writer, LayoutNode<V> node) throws XMLStreamException {
		boolean subgraphNode = subgraphs != null && subgraphs.get(node.getVertex()) != null;
		if (subgraphNode && groupNodes) {
			writeGroupNode(writer, node);
		} else {
			writeShapeNode(writer, node, subgraphNode);
		}
	}
	
	private void writeShapeNode(XMLStreamWriter writer, LayoutNode<V> vertex, boolean subgraph) throws XMLStreamException {
		// <y:ShapeNode>  
		writer.writeStartElement(getPrefix(), "ShapeNode", getNamespaceURI());

		//   <y:Geometry x="170.5" y="-15.0" width="59.0" height="30.0"/>  
		writer.writeEmptyElement(getPrefix(), "Geometry", getNamespaceURI());
		writer.writeAttribute("x", String.valueOf(vertex.getPoint().x));
		writer.writeAttribute("y", String.valueOf(vertex.getPoint().y));
		writer.writeAttribute("width", String.valueOf(vertex.getDimension().w));
		writer.writeAttribute("height", String.valueOf(vertex.getDimension().h));

		//   <y:Fill color="#CCCCFF" transparent="false"/>  
		writer.writeEmptyElement(getPrefix(), "Fill", getNamespaceURI());
		writer.writeAttribute("hasColor", "false");
//		writer.writeAttribute("color", "#CCCCFF");
		writer.writeAttribute("transparent", "false");
		
		//   <y:BorderStyle type="line" width="1.0" color="#000000"/>  
		writer.writeEmptyElement(getPrefix(), "BorderStyle", getNamespaceURI());
		writer.writeAttribute("type", "line");
		writer.writeAttribute("width", "1.0");
		writer.writeAttribute("color", "#AAAAAA");
      
		//   <y:NodeLabel>January</y:NodeLabel>  
		writer.writeStartElement(getPrefix(), "NodeLabel", getNamespaceURI());
		writeLabelFontAttributes(writer);
		if (subgraph) {
			writer.writeAttribute("modelName", "internal");
			writer.writeAttribute("modelPosition", "tl");
			writer.writeAttribute("borderDistance", "2");
		}
		writer.writeCharacters(labelProvider.getLabel(vertex.getVertex()));
		writer.writeEndElement();

		//   <y:Shape type="rectangle"/>  
		writer.writeEmptyElement(getPrefix(), "Shape", getNamespaceURI());
		writer.writeAttribute("type", "roundrectangle");

		// </y:ShapeNode>
		writer.writeEndElement();
	}
	
	private void writeGroupNode(XMLStreamWriter writer, LayoutNode<V> vertex) throws XMLStreamException {
		// <y:GroupNode>  
		writer.writeStartElement(getPrefix(), "GroupNode", getNamespaceURI());

		//   <y:Geometry x="170.5" y="-15.0" width="59.0" height="30.0"/>  
		writer.writeEmptyElement(getPrefix(), "Geometry", getNamespaceURI());
		writer.writeAttribute("x", String.valueOf(vertex.getPoint().x));
		writer.writeAttribute("y", String.valueOf(vertex.getPoint().y));
		writer.writeAttribute("width", String.valueOf(vertex.getDimension().w));
		writer.writeAttribute("height", String.valueOf(vertex.getDimension().h));

		//   <y:Fill color="#CCCCFF" transparent="false"/>  
		writer.writeEmptyElement(getPrefix(), "Fill", getNamespaceURI());
		writer.writeAttribute("hasColor", "false");
//		writer.writeAttribute("color", "#CCCCFF");
		writer.writeAttribute("transparent", "false");
		
		//   <y:BorderStyle type="line" width="1.0" color="#000000"/>  
		writer.writeEmptyElement(getPrefix(), "BorderStyle", getNamespaceURI());
		writer.writeAttribute("type", "line");
		writer.writeAttribute("width", "1.0");
		writer.writeAttribute("color", "#AAAAAA");
      
		//   <y:NodeLabel>January</y:NodeLabel>  
		writer.writeStartElement(getPrefix(), "NodeLabel", getNamespaceURI());
		writeLabelFontAttributes(writer);
		writer.writeAttribute("alignment", "right");
		writer.writeAttribute("modelName", "internal");
		writer.writeAttribute("modelPosition", "t");
		writer.writeAttribute("borderDistance", "1.0");
		writer.writeAttribute("backgroundColor", "#EEEEEE");
		writer.writeAttribute("autoSizePolicy", "node_width");
		writer.writeCharacters(labelProvider.getLabel(vertex.getVertex()));
		writer.writeEndElement();

		//   <y:Shape type="rectangle"/>  
		writer.writeEmptyElement(getPrefix(), "Shape", getNamespaceURI());
		writer.writeAttribute("type", "roundrectangle");

		//	 <y:State closed="false" closedHeight="40.0" closedWidth="100.0" innerGraphDisplayEnabled="false"/>
		writer.writeEmptyElement(getPrefix(), "State", getNamespaceURI());
		writer.writeAttribute("closed", "false");
		writer.writeAttribute("closedHeight", "40");
		writer.writeAttribute("closedwidth", String.valueOf(vertex.getDimension().w));
		writer.writeAttribute("innerGraphDisplayEnabled", "false");

		//	 <y:Insets bottom="15" bottomF="15.0" left="15" leftF="15.0" right="15" rightF="15.0" top="15" topF="15.0"/>
		writer.writeEmptyElement(getPrefix(), "Insets", getNamespaceURI());
		writer.writeAttribute("bottom", "10");
		writer.writeAttribute("bottomF", "10");
		writer.writeAttribute("left", "10");
		writer.writeAttribute("leftF", "10");
		writer.writeAttribute("right", "10");
		writer.writeAttribute("rightF", "10");
		writer.writeAttribute("top", "10");
		writer.writeAttribute("topF", "10");

		//  <y:BorderInsets bottom="0" bottomF="0.0" left="0" leftF="0.0" right="0" rightF="0.0" top="0" topF="0.0"/>
		writer.writeEmptyElement(getPrefix(), "BorderInsets", getNamespaceURI());
		writer.writeAttribute("bottom", "0");
		writer.writeAttribute("bottomF", "0");
		writer.writeAttribute("left", "0");
		writer.writeAttribute("leftF", "0");
		writer.writeAttribute("right", "0");
		writer.writeAttribute("rightF", "0");
		writer.writeAttribute("top", "0");
		writer.writeAttribute("topF", "0");

		// </y:GroupNode>
		writer.writeEndElement();
	}
}
