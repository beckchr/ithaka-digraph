/*
 * Copyright 2012 Odysseus Software GmbH, Frankfurt am Main/Germany.
 */
package de.odysseus.ithaka.digraph;

public interface DigraphProvider<T, G extends Digraph<?,?>> {
	public G get(T value);
}
