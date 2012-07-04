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
