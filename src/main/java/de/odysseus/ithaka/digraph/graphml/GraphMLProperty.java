package de.odysseus.ithaka.digraph.graphml;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public interface GraphMLProperty<T> {

	public abstract GraphMLPropertyDomain getDomain();

	public abstract String getNamespaceURI();

	public abstract String getPrefix();

	public abstract void writeKey(XMLStreamWriter writer, String id) throws XMLStreamException;

	public abstract void writeData(XMLStreamWriter writer, String id, T value) throws XMLStreamException;

}