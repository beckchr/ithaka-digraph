/*
 * Copyright 2008 Odysseus Software GmbH, Frankfurt am Main/Germany.
 */
package de.odysseus.ithaka.digraph;

public interface DigraphFactory<G extends Digraph<?,?>> {
	public G create();
}
