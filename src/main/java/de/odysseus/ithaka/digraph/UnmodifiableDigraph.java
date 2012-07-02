/*
 * Copyright 2008 Odysseus Software GmbH, Frankfurt am Main/Germany.
 */
package de.odysseus.ithaka.digraph;

public class UnmodifiableDigraph<V, E> extends DigraphAdapter<V, E> {
	public UnmodifiableDigraph(Digraph<V, E> digraph) {
		super(digraph);
	}
	
	@Override
	public final boolean add(V vertex) {
		throw new UnsupportedOperationException("This Digraph is readonly!");
	}

	@Override
	public final E put(V source, V target, E edge) {
		throw new UnsupportedOperationException("This Digraph is readonly!");
	}
	
	@Override
	public final boolean remove(V vertex) {
		throw new UnsupportedOperationException("This Digraph is readonly!");
	}
	
	@Override
	public final E remove(V source, V target) {
		throw new UnsupportedOperationException("This Digraph is readonly!");
	}
}
