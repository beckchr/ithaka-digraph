/*
 * Copyright 2008 Odysseus Software GmbH, Frankfurt am Main/Germany.
 */
package de.odysseus.ithaka.digraph;

import java.util.List;
import java.util.Set;

public class SimpleDigraphAdapter<V> extends DigraphAdapter<V, Boolean> implements SimpleDigraph<V> {
	private static final EdgeCumulator<Object,Integer,Boolean> COUNT_CUMULATOR = new EdgeCumulator<Object,Integer,Boolean>() {
		@Override
		public Integer add(Object s, Object t, Integer c, Boolean e) {
			return Integer.valueOf(c == null ? 1 : c.intValue() + 1);
		}
	};

	public static <V> DigraphFactory<SimpleDigraphAdapter<V>> getAdapterFactory(final DigraphFactory<? extends Digraph<V,Boolean>> factory) {
		return new DigraphFactory<SimpleDigraphAdapter<V>>() {
			@Override
			public SimpleDigraphAdapter<V> create() {
				return new SimpleDigraphAdapter<V>(factory);
			}
		};
	}
	
	private final DigraphFactory<? extends Digraph<V,Boolean>> factory;

	public SimpleDigraphAdapter() {
		this(MapDigraph.<V,Boolean>getDefaultDigraphFactory());
	}

	public SimpleDigraphAdapter(DigraphFactory<? extends Digraph<V,Boolean>> factory) {
		this(factory, factory.create());
	}

	protected SimpleDigraphAdapter(DigraphFactory<? extends Digraph<V,Boolean>> factory, Digraph<V,Boolean> delegate) {
		super(delegate);
		this.factory = factory;
	}
	
	public DigraphFactory<? extends SimpleDigraph<V>> getDigraphFactory() {
		return getAdapterFactory(factory);
	}
	
	protected DigraphFactory<? extends Digraph<V,Boolean>> getDelegateFactory() {
		return factory;
	}

	@Override
	public boolean add(V source, V target) {
		return super.put(source, target, Boolean.TRUE) == null ? Boolean.TRUE : Boolean.FALSE;
	}

	@Override
	public Boolean remove(V source, V target) {
		return super.remove(source, target) == null ? Boolean.FALSE : Boolean.TRUE;
	}

	@Override
	public Boolean put(V source, V target, Boolean edge) {
		if (!Boolean.TRUE.equals(edge)) {
			// or should we simply ignore the supplied edge value?
			throw new IllegalArgumentException("illegal: edge must be TRUE; use add(source, target) instead!");
		}
		return add(source, target);
	}

	@Override
	public SimpleDigraph<V> reverse() {
		return new SimpleDigraphAdapter<V>(getDigraphFactory(), super.reverse());
	}

	@Override
	public SimpleDigraph<V> subgraph(Set<V> vertices) {
		return new SimpleDigraphAdapter<V>(getDigraphFactory(), super.subgraph(vertices));
	}
	
	/**
	 * Compute the component graph. The component are <code>SimpleDigraph<V></code> created by
	 * {@link #getDigraphFactory()}.
	 * The outer graph is a {@link WeightedDigraph} whose edges are labelled with integers
	 * giving the number of edges between components in the original (this) graph.
	 */
	public WeightedDigraph<SimpleDigraph<V>> partition(boolean weak) {
		// compute component sets
		List<Set<V>> components = weak ? Digraphs.<V>wcc(this) : Digraphs.<V>scc(this);

		// factory to create a digraph with simple V digraph vertices and Integer edges
		DigraphFactory<? extends Digraph<SimpleDigraph<V>,Integer>> rawFactory =
			MapDigraph.getDefaultDigraphFactory();

		// adapter factory to create a weighted digraph from the above
		DigraphFactory<? extends WeightedDigraph<SimpleDigraph<V>>> outerFactory =
			WeightedDigraphAdapter.getAdapterFactory(rawFactory);

		// answer partition graph
		return Digraphs.partition(this, components, outerFactory, getDigraphFactory(), COUNT_CUMULATOR);
	}
}
