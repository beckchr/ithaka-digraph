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
package de.odysseus.ithaka.digraph;

import java.util.List;
import java.util.Set;

public class WeightedDigraphAdapter<V> extends DigraphAdapter<V, Integer> implements WeightedDigraph<V> {
	public static <V> DigraphFactory<WeightedDigraphAdapter<V>> getAdapterFactory(final DigraphFactory<? extends Digraph<V,Integer>> factory) {
		return new DigraphFactory<WeightedDigraphAdapter<V>>() {
			@Override
			public WeightedDigraphAdapter<V> create() {
				return new WeightedDigraphAdapter<V>(factory);
			}
		};
	}
	
	private static final EdgeCumulator<Object,Integer,Integer> ADD_CUMULATOR = new EdgeCumulator<Object,Integer,Integer>() {
		@Override
		public Integer add(Object source, Object target, Integer edge, Integer operand) {
			return edge == null ? operand : Integer.valueOf(edge.intValue() + operand.intValue());
		}
	};

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
		return weight == null ? Integer.valueOf(0) : weight;
	}

	@Override
	public void add(V source, V target, int weight) {
		put(source, target, get(source, target) + weight);
	}

	protected DigraphFactory<? extends WeightedDigraph<V>> getDigraphFactory() {
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
