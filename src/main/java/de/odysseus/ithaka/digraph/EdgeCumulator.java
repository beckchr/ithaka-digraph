/*
 * Copyright 2008 Odysseus Software GmbH, Frankfurt am Main/Germany.
 */
package de.odysseus.ithaka.digraph;

public interface EdgeCumulator<T,C,E> {
	/**
	 * Add <code>e</code> to arc <code>s --c--&gt; t</code>.
	 * @param s source subgraph
	 * @param t target subgraph
	 * @param c cumulated edge
	 * @param e operand
	 * @return <code>c + e</code>
	 */
	public C add(T s, T t, C c, E e);
}