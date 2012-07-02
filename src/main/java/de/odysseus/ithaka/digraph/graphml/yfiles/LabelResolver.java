/*
 * Copyright 2012 Odysseus Software GmbH, Frankfurt am Main/Germany.
 */
package de.odysseus.ithaka.digraph.graphml.yfiles;

public interface LabelResolver<T> {
	public String getLabel(T value);
}
