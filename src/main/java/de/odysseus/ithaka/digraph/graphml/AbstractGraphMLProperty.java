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
package de.odysseus.ithaka.digraph.graphml;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public abstract class AbstractGraphMLProperty<T> implements GraphMLProperty<T> {
	private final GraphMLPropertyDomain domain;
	private final String namespaceURI;
	private final String prefix;

	public AbstractGraphMLProperty(GraphMLPropertyDomain domain, String namespaceURI, String prefix) {
		this.domain = domain;
		this.namespaceURI = namespaceURI;
		this.prefix = prefix;
	}
	
	/* (non-Javadoc)
	 * @see de.odysseus.ithaka.digraph.graphml.GraphMLProperty#getDomain()
	 */
	@Override
	public GraphMLPropertyDomain getDomain() {
		return domain;
	}

	/* (non-Javadoc)
	 * @see de.odysseus.ithaka.digraph.graphml.GraphMLProperty#getNamespaceURI()
	 */
	@Override
	public String getNamespaceURI() {
		return namespaceURI;
	}
	
	/* (non-Javadoc)
	 * @see de.odysseus.ithaka.digraph.graphml.GraphMLProperty#getPrefix()
	 */
	@Override
	public String getPrefix() {
		return prefix;
	}
	
	/* (non-Javadoc)
	 * @see de.odysseus.ithaka.digraph.graphml.GraphMLProperty#writeKey(javax.xml.stream.XMLStreamWriter, java.lang.String)
	 */
	@Override
	public void writeKey(XMLStreamWriter writer, String id) throws XMLStreamException {
		boolean hasDefault = hasDefault();
		if (hasDefault) {
			writer.writeStartElement(GraphMLExporter.GRAPHML_NAMESPACE_URI, "key");
		} else {
			writer.writeEmptyElement(GraphMLExporter.GRAPHML_NAMESPACE_URI, "key");
		}
		writer.writeAttribute("id", id);
		writer.writeAttribute("for", domain.toString());
		writeKeyExtraAttributes(writer);
		if (hasDefault) {
			writer.writeStartElement(GraphMLExporter.GRAPHML_NAMESPACE_URI, "default");
			writeDefaultContent(writer);
			writer.writeEndElement(); // default
			writer.writeEndElement(); // key
		}
	}

	/* (non-Javadoc)
	 * @see de.odysseus.ithaka.digraph.graphml.GraphMLProperty#writeData(javax.xml.stream.XMLStreamWriter, java.lang.String, T)
	 */
	@Override
	public void writeData(XMLStreamWriter writer, String id, T value) throws XMLStreamException {
		if (hasData(value)) {
			writer.writeStartElement(GraphMLExporter.GRAPHML_NAMESPACE_URI, "data");
			writer.writeAttribute("key", id);
			writeDataContent(writer, value);
			writer.writeEndElement();
		}		
	}

	/**
	 * Write key attributes (everything except <code>id</code> and <code>for</code>).
	 * @param writer
	 * @throws XMLStreamException
	 */
	protected abstract void writeKeyExtraAttributes(XMLStreamWriter writer) throws XMLStreamException;

	protected abstract boolean hasDefault();
	protected abstract void writeDefaultContent(XMLStreamWriter writer) throws XMLStreamException;

	protected abstract boolean hasData(T data);
	protected abstract void writeDataContent(XMLStreamWriter writer, T data) throws XMLStreamException;
}
