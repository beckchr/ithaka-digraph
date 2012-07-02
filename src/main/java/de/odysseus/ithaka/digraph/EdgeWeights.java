/*
 * Copyright 2008 Odysseus Software GmbH, Frankfurt am Main/Germany.
 */
package de.odysseus.ithaka.digraph;

public interface EdgeWeights<V> {
	public static final EdgeWeights<Object> UNIT_WEIGHTS = new EdgeWeights<Object>() {
		@Override
		public Integer get(Object source, Object target) { return Integer.valueOf(1); }
	};
	
	public Integer get(V source, V target);
}
