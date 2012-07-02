/*
 * Copyright 2012 Odysseus Software GmbH, Frankfurt am Main/Germany.
 */
package de.odysseus.ithaka.digraph.graphml.yfiles;

import java.awt.Font;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import de.odysseus.ithaka.digraph.graphml.AbstractGraphMLProperty;
import de.odysseus.ithaka.digraph.graphml.GraphMLPropertyDomain;

public abstract class AbstractGraphicsProperty<T> extends AbstractGraphMLProperty<T> {
	private static final String YFILES_NAMESPACE_URI = "http://www.yworks.com/xml/graphml";

	private final String yfilesType;
	private final Font font;
	
	public AbstractGraphicsProperty(GraphMLPropertyDomain domain, String yfilesType, Font font) {
		super(domain, YFILES_NAMESPACE_URI, "y");
		this.yfilesType = yfilesType;
		this.font = font;
	}

	@Override
	protected void writeKeyExtraAttributes(XMLStreamWriter writer) throws XMLStreamException {
		writer.writeAttribute("yfiles.type", yfilesType);
	}

	protected void writeLabelFontAttributes(XMLStreamWriter writer) throws XMLStreamException {
		if (font != null) {
			writer.writeAttribute("fontFamily", font.getFamily());
			writer.writeAttribute("fontSize", String.valueOf(font.getSize()));
			switch (font.getStyle()) {
			case Font.PLAIN:
				writer.writeAttribute("fontStyle", "plain");
				break;
			case Font.BOLD:
				writer.writeAttribute("fontStyle", "bold");
				break;
			case Font.ITALIC:
				writer.writeAttribute("fontStyle", "italic");
				break;
			case Font.BOLD + Font.ITALIC:
				writer.writeAttribute("fontStyle", "boldItalic");
			break;
			}
		}
	}	
	
	@Override
	protected boolean hasDefault() {
		return false;
	}

	@Override
	protected void writeDefaultContent(XMLStreamWriter writer) throws XMLStreamException {
	}

	@Override
	protected boolean hasData(T data) {
		return true;
	}
}
