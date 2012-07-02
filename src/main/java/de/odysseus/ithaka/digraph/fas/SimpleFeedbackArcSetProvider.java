/*
 * Copyright 2012 Odysseus Software GmbH, Frankfurt am Main/Germany.
 */
package de.odysseus.ithaka.digraph.fas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import de.odysseus.ithaka.digraph.Digraph;
import de.odysseus.ithaka.digraph.DigraphFactory;
import de.odysseus.ithaka.digraph.Digraphs;
import de.odysseus.ithaka.digraph.EdgeWeights;
import de.odysseus.ithaka.digraph.MapDigraph;

/**
 * Simple feedback arc set provider.
 */
public class SimpleFeedbackArcSetProvider extends AbstractFeedbackArcSetProvider {
	public SimpleFeedbackArcSetProvider() {
		this(1);
	}
	
	public SimpleFeedbackArcSetProvider(int numberOfThreads) {
		super(numberOfThreads);
	}

	/**
	 * create equivalent graphs whith different edge orderings.
	 * @param digraph digraph to copy
	 * @param weights edge weights
	 * @return list of copies
	 */
	private <V,E> List<Digraph<V,E>> copies(Digraph<V,E> digraph, int count) {
		List<Digraph<V,E>> copies = new ArrayList<Digraph<V,E>>();
		copies.add(digraph);

		final List<Integer> shuffle = new ArrayList<Integer>();
		final Map<V, Integer> order = new HashMap<V, Integer>();
		int index = 0;
		for (V source : digraph.vertices()) {
			order.put(source, index);
			shuffle.add(index++);
		}
		
		Random random = new Random(7);
		for (int i = 0; i < count; i++) {
			Collections.shuffle(shuffle, random);
			copies.add(Digraphs.copy(digraph, new DigraphFactory<Digraph<V,E>>() {
				List<Integer> index = new ArrayList<Integer>(shuffle);
				@Override
				public Digraph<V, E> create() {
					return new MapDigraph<V, E>(new Comparator<V>() {
						@Override
						public int compare(V v1, V v2) {
							int value1 = index.get(order.get(v1));
							int value2 = index.get(order.get(v2));
							return Integer.valueOf(value1).compareTo(value2);
						}
					});
				}
			}));
		}
		return copies;
	}
	
	/**
	 * Compute simple feedback arc set by performing |n| DFS traversals (each starting
	 * with a different vertex) on the tangle, taking non-forward edges as feedback.
	 * The minimum weight feedback arc set among those |n| results is returned.
	 * @param tangle strongly connected component
	 * @param weights edge weights
	 * @return feedback arc set
	 */
	@Override
	protected <V,E> Digraph<V,E> lfas(Digraph<V,E> tangle, EdgeWeights<? super V> weights) {
		/*
		 * store best results
		 */
		int minWeight = Integer.MAX_VALUE;
		int minSize = Integer.MAX_VALUE;
		List<V> minFinished = null;
		
		/*
		 * threshold on max. number of iterations (avoid running forever)
		 */
		int maxIterationsLeft = Math.max(1, 1000000 / (tangle.getVertexCount() + tangle.getEdgeCount()));

		/*
		 * perform DFS for each node, keep best result
		 */
		List<Digraph<V,E>> copies = copies(tangle, Math.min(10, tangle.getVertexCount()));		
		List<V> finished = new ArrayList<V>(tangle.getVertexCount());
		Set<V> discovered = new HashSet<V>(tangle.getVertexCount());
		for (V start : tangle.vertices()) {
			for (Digraph<V, E> copy : copies) {
				finished.clear();
				discovered.clear();
				Digraphs.dfs(copy, start, discovered, finished);
				assert finished.size() == tangle.getVertexCount();

				int weight = 0;
				int size = 0;
				discovered.clear();
				for (V source : finished) {
					discovered.add(source);
					for (V target : tangle.targets(source)) {
						if (!discovered.contains(target)) { // feedback edge
							weight += weights.get(source, target);
							size++;
						}
					}
					if (weight > minWeight) {
						break;
					}
				}
				if (weight < minWeight || weight == minWeight && size < minSize) {
					minFinished = new ArrayList<V>(finished);
					minWeight = weight;
					minSize = size;
				}
			}
			if (--maxIterationsLeft == 0) {
				break;
			}
		}

		/*
		 * create feedback graph
		 */
		Digraph<V, E> feedback = MapDigraph.<V, E>getDefaultDigraphFactory().create();
		discovered.clear();
		for (V source : minFinished) {
			discovered.add(source);
			for (V target : tangle.targets(source)) {
				if (!discovered.contains(target)) { // feedback edge
					feedback.put(source, target, tangle.get(source, target));
				}
			}
		}

		return feedback;
	}
}
