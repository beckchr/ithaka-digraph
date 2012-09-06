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

public interface GraphMLProperty<T> {

	public abstract GraphMLPropertyDomain getDomain();

	public abstract String getNamespaceURI();

	public abstract String getPrefix();

	public abstract void writeKey(XMLStreamWriter writer, String id) throws XMLStreamException;

	public abstract void writeData(XMLStreamWriter writer, String id, T value) throws XMLStreamException;

}