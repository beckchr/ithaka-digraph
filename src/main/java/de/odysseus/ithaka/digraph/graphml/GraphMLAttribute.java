/*
 * Copyright 2012 Odysseus Software GmbH, Frankfurt am Main/Germany.
 */
package de.odysseus.ithaka.digraph.graphml;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public abstract class GraphMLAttribute<T> extends AbstractGraphMLProperty<T> {
	private final GraphMLPropertyType type;
	private final String name;
	private final String defaultData;

	/**
	 * Create simple text property.
	 * @param domain
	 * @param type
	 * @param name
	 */
	public GraphMLAttribute(GraphMLPropertyDomain domain, String name, GraphMLPropertyType type) {
		this(domain, name, type, null);
	}

	/**
	 * Create simple text property.
	 * @param domain
	 * @param type
	 * @param name
	 * @param defaultData
	 */
	public GraphMLAttribute(GraphMLPropertyDomain domain, String name, GraphMLPropertyType type, String defaultData) {
		super(domain, null, null);
		this.name = name;
		this.type = type;
		this.defaultData = defaultData;
	}
	
	protected abstract String getData(T value);
	
	public GraphMLPropertyType getType() {
		return type;
	}
	
	public String getName() {
		return name;
	}
	
	/**
	 * Write key attributes (<code>attr.name</code> and <code>attr.type</code>).
	 * @param writer
	 * @throws XMLStreamException
	 */
	@Override
	public void writeKeyExtraAttributes(XMLStreamWriter writer) throws XMLStreamException {
		writer.writeAttribute("attr.name", name);
		writer.writeAttribute("attr.type", type.toString());
	}
	
	@Override
	public boolean hasDefault() {
		return defaultData != null;
	}
	
	@Override
	public void writeDefaultContent(XMLStreamWriter writer) throws XMLStreamException {
		writer.writeCharacters(defaultData);
	}

	@Override
	public boolean hasData(T value) {
		return getData(value) != null;
	}
	
	@Override
	public void writeDataContent(XMLStreamWriter writer, T value) throws XMLStreamException {
		writer.writeCharacters(getData(value));
	}
}
