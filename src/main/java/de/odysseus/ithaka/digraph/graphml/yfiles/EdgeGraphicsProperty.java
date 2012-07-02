/*
 * Copyright 2012 Odysseus Software GmbH, Frankfurt am Main/Germany.
 */
package de.odysseus.ithaka.digraph.graphml.yfiles;

import java.awt.Font;
import java.util.Iterator;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import de.odysseus.ithaka.digraph.graphml.GraphMLPropertyDomain;
import de.odysseus.ithaka.digraph.layout.LayoutArc;
import de.odysseus.ithaka.digraph.layout.LayoutPoint;

public class EdgeGraphicsProperty<V, E> extends AbstractGraphicsProperty<LayoutArc<V, E>> {
	private final LabelResolver<? super E> labels;

	public EdgeGraphicsProperty(LabelResolver<? super E> labels, Font font) {
		super(GraphMLPropertyDomain.Edge, "edgegraphics", font);
		this.labels = labels;
	}

	@Override
	protected void writeDataContent(XMLStreamWriter writer, LayoutArc<V, E> edge) throws XMLStreamException {
		// <y:PolyLineEdge>  
		writer.writeStartElement(getPrefix(), "PolyLineEdge", getNamespaceURI());
		Iterator<LayoutPoint> bendPoints = edge.getBendPoints().iterator();
		double sdx = edge.getSource().getPoint().x + edge.getSource().getDimension().w / 2.0;
		double sdy = edge.getSource().getPoint().y + edge.getSource().getDimension().h / 2.0;
		double tdx = edge.getTarget().getPoint().x + edge.getTarget().getDimension().w / 2.0;
		double tdy = edge.getTarget().getPoint().y + edge.getTarget().getDimension().h / 2.0;
		if (bendPoints.hasNext()) {
			//   <y:Path sx="0.0" sy="-15.0" tx="29.5" ty="0.0">  
			writer.writeStartElement(getPrefix(), "Path", getNamespaceURI());
			writer.writeAttribute("sx", String.valueOf(edge.getStartPoint().x - sdx));
			writer.writeAttribute("sy", String.valueOf(edge.getStartPoint().y - sdy));
			writer.writeAttribute("tx", String.valueOf(edge.getEndPoint().x - tdx));
			writer.writeAttribute("ty", String.valueOf(edge.getEndPoint().y - tdy));
			do {
				//     <y:Point x="425.0" y="0.0"/>  
				LayoutPoint bendPoint = bendPoints.next();
				writer.writeEmptyElement(getPrefix(), "Point", getNamespaceURI());
				writer.writeAttribute("x", String.valueOf(bendPoint.x));
				writer.writeAttribute("y", String.valueOf(bendPoint.y));
			} while (bendPoints.hasNext());
			//   </y:Path>  
			writer.writeEndElement();
		} else {
			//   <y:Path sx="0.0" sy="-15.0" tx="29.5" ty="0.0"/>  
			writer.writeEmptyElement(getPrefix(), "Path", getNamespaceURI());
			writer.writeAttribute("sx", String.valueOf(edge.getStartPoint().x - sdx));
			writer.writeAttribute("sy", String.valueOf(edge.getStartPoint().y - sdy));
			writer.writeAttribute("tx", String.valueOf(edge.getEndPoint().x - tdx));
			writer.writeAttribute("ty", String.valueOf(edge.getEndPoint().y - tdy));
		}
		//   <y:LineStyle type="line" width="1.0" color="#000000"/>  
		writer.writeEmptyElement(getPrefix(), "LineStyle", getNamespaceURI());
		writer.writeAttribute("type", "line");
		writer.writeAttribute("width", "1");
		writer.writeAttribute("color", "#333333");
		
		//   <y:Arrows source="none" target="standard"/>  
		writer.writeEmptyElement(getPrefix(), "Arrows", getNamespaceURI());
		writer.writeAttribute("source", "none");
		writer.writeAttribute("target", "standard");
		
		//   <y:EdgeLabel>Happy New Year!</y:EdgeLabel>
		if (labels != null) {
			String label = labels.getLabel(edge.getEdge());
			if (label != null && label.trim().length() > 0) {
				writer.writeStartElement(getPrefix(), "EdgeLabel", getNamespaceURI());
				writeLabelFontAttributes(writer);
				writer.writeCharacters(label);
				writer.writeEndElement();
			}
		}
		
		//   <y:BendStyle smoothed="false"/>  
		writer.writeEmptyElement(getPrefix(), "BendStyle", getNamespaceURI());
		writer.writeAttribute("smoothed", "false");

		// </y:PolyLineEdge>
		writer.writeEndElement();
	}
}
