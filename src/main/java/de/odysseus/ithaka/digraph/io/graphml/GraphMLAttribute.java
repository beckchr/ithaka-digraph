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
package de.odysseus.ithaka.digraph.io.graphml;

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
