/*
 * Copyright 2008 Odysseus Software GmbH, Frankfurt am Main/Germany.
 */
package de.odysseus.ithaka.digraph;

import java.util.List;
import java.util.Set;

public class WeightedDigraphAdapter<V> extends DigraphAdapter<V, Integer> implements WeightedDigraph<V> {
	private static final Integer ZERO = Integer.valueOf(0);

	private static final EdgeCumulator<Object,Integer,Integer> ADD_CUMULATOR = new EdgeCumulator<Object,Integer,Integer>() {
		@Override
		public Integer add(Object s, Object t, Integer c, Integer e) {
			return c == null ? e : Integer.valueOf(c.intValue() + e.intValue());
		}
	};

	public static <V> DigraphFactory<WeightedDigraphAdapter<V>> getAdapterFactory(final DigraphFactory<? extends Digraph<V,Integer>> factory) {
		return new DigraphFactory<WeightedDigraphAdapter<V>>() {
			@Override
			public WeightedDigraphAdapter<V> create() {
				return new WeightedDigraphAdapter<V>(factory);
			}
		};
	}
	
	private final DigraphFactory<? extends Digraph<V,Integer>> factory;
	
	public WeightedDigraphAdapter() {
		this(MapDigraph.<V,Integer>getDefaultDigraphFactory());
	}

	public WeightedDigraphAdapter(DigraphFactory<? extends Digraph<V,Integer>> factory) {
		this(factory, factory.create());
	}
	
	protected WeightedDigraphAdapter(DigraphFactory<? extends Digraph<V,Integer>> factory, Digraph<V,Integer> delegate) {
		super(delegate);
		this.factory = factory;
	}
	
	@Override
	public Integer get(Object source, Object target) {
		Integer weight = super.get(source, target);
		return weight == null ? ZERO : weight;
	}

	@Override
	public void add(V source, V target, int weight) {
		put(source, target, get(source, target) + weight);
	}

	public DigraphFactory<? extends WeightedDigraph<V>> getDigraphFactory() {
		return getAdapterFactory(factory);
	}
	
	protected DigraphFactory<? extends Digraph<V,Integer>> getDelegateFactory() {
		return factory;
	}

	@Override
	public WeightedDigraph<V> reverse() {
		return new WeightedDigraphAdapter<V>(getDigraphFactory(), super.reverse());
	}
	
	@Override
	public WeightedDigraph<V> subgraph(Set<V> vertices) {
		return new WeightedDigraphAdapter<V>(getDigraphFactory(), super.subgraph(vertices));
	}
	
	@Override
	public int totalWeight() {
		int weight = 0;
		for (V source : vertices()) {
			for (V target : targets(source)) {
				weight += get(source, target);
			}
		}
		return weight;
	}

	/**
	 * Compute the component graph. The component are <code>WeightedDigraph<V></code>s.
	 * The outer graph is a {@link WeightedDigraph} whose edges are labelled with integers
	 * giving the sum of edge weights between components in the original (this) graph.
	 */
	public WeightedDigraph<WeightedDigraph<V>> partition(boolean weak) {
		// compute component sets
		List<Set<V>> components = weak ? Digraphs.<V>wcc(this) : Digraphs.<V>scc(this);

		// factory to create a digraph with weighted V digraph vertices and Integer edges
		DigraphFactory<? extends Digraph<WeightedDigraph<V>,Integer>> rawFactory =
			MapDigraph.getDefaultDigraphFactory();

		// adapter factory to create a weighted digraph from the above
		DigraphFactory<? extends WeightedDigraph<WeightedDigraph<V>>> outerFactory =
			WeightedDigraphAdapter.getAdapterFactory(rawFactory);

		// answer partition graph
		return Digraphs.partition(this, components, outerFactory, getDigraphFactory(), ADD_CUMULATOR);
	}
}
