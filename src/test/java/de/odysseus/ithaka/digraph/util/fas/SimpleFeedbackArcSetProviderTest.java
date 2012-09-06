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
package de.odysseus.ithaka.digraph.util.fas;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Formatter;
import java.util.Random;

import junit.framework.Assert;

import org.junit.Test;

import de.odysseus.ithaka.digraph.Digraph;
import de.odysseus.ithaka.digraph.EdgeWeights;
import de.odysseus.ithaka.digraph.SimpleDigraph;
import de.odysseus.ithaka.digraph.SimpleDigraphAdapter;
import de.odysseus.ithaka.digraph.WeightedDigraph;
import de.odysseus.ithaka.digraph.WeightedDigraphAdapter;
import de.odysseus.ithaka.digraph.util.fas.FeedbackArcSet;
import de.odysseus.ithaka.digraph.util.fas.FeedbackArcSetPolicy;
import de.odysseus.ithaka.digraph.util.fas.FeedbackArcSetProvider;
import de.odysseus.ithaka.digraph.util.fas.SimpleFeedbackArcSetProvider;

public class SimpleFeedbackArcSetProviderTest {
	private SimpleDigraphAdapter<Integer> randomGraph(Random rng, int nodeCount, int arcCount) {
		if (arcCount > nodeCount * (nodeCount - 1)) {
			throw new IllegalArgumentException("Too many arcs!");
		}
		SimpleDigraphAdapter<Integer> graph = new SimpleDigraphAdapter<Integer>();
		while (graph.getEdgeCount() < arcCount) {
			int source = rng.nextInt(nodeCount);
			int target = rng.nextInt(nodeCount);
			if (source != target) {
				graph.add(source, target);
			}
		};
		return graph;
	}

	private EdgeWeights<Integer> randomWeights(final Digraph<Integer,?> graph, Random rng, int minWeight, int maxWeight) {
		int maxNode = 0;
		for (int node : graph.vertices()) {
			if (node > maxNode) {
				maxNode = node;
			}
		}
		final int size = maxNode + 1;

		class Weights implements EdgeWeights<Integer> {
			int[][] values = new int[size][size];
			@Override
			public Integer get(Integer source, Integer target) {
				return values[source][target];
			}
			@Override public String toString() {
				Formatter formatter = new Formatter(new StringBuilder());
				formatter.format("   0  1  2  3  4  5  6  7  8  9".substring(0, 1 + 3*graph.getVertexCount()));
				for (int source = 0; source < graph.getVertexCount(); source++) {
					formatter.format("%n%1d", source);
					for (int target = 0; target < graph.getVertexCount(); target++) {
						formatter.format(" %2d", values[source][target]);
					}
				}
				String result = formatter.toString();
				formatter.close();
				return result;
			}
		};
		Weights weights = new Weights();
		for (int source : graph.vertices()) {
			for (int target : graph.targets(source)) {
				weights.values[source][target] = minWeight + rng.nextInt(maxWeight + 1 - minWeight);
			}
		}
		return weights;
	}

	private boolean isFeedbackSet(SimpleDigraph<Integer> graph, Digraph<Integer,?> set) {
		for (int source : set.vertices()) {
			for (int target : set.targets(source)){
				if (!graph.remove(source, target)) {
					return false;
				}
			}
		}
		boolean result = graph.isAcyclic();
		for (int source : set.vertices()) {
			for (int target : set.targets(source)) {
				graph.add(source, target);
			}
		}
		return result;
	}

	private <V> int weight(Digraph<V,?> graph, EdgeWeights<V> weights) {
		int weight = 0;
		for (V source : graph.vertices()) {
			for (V target : graph.targets(source)) {
				weight += weights.get(source, target);
			}
		}
		return weight;
	}

	private long calculateFeedbackArcSets(Random rng, int nodeCount, int arcCount, int minWeight, int maxWeight) {
		FeedbackArcSetProvider simpleProvider = new SimpleFeedbackArcSetProvider();
		SimpleDigraph<Integer> graph = randomGraph(rng, nodeCount, arcCount);
		EdgeWeights<Integer> weights = randomWeights(graph, rng, minWeight, maxWeight);

		long time = System.currentTimeMillis();
		Digraph<Integer,?> swfas = simpleProvider.getFeedbackArcSet(graph, weights, FeedbackArcSetPolicy.MIN_WEIGHT);
		Digraph<Integer,?> ssfas = simpleProvider.getFeedbackArcSet(graph, weights, FeedbackArcSetPolicy.MIN_SIZE);
		time = System.currentTimeMillis() - time;

		if (graph.isAcyclic()) {
			assertTrue(ssfas.getEdgeCount() == 0);
			assertTrue(swfas.getEdgeCount() == 0);
		}
		assertTrue(isFeedbackSet(graph, swfas));
		assertTrue(isFeedbackSet(graph, ssfas));

		assertTrue(weight(swfas, weights) <= weight(ssfas, weights));
		assertTrue(ssfas.getEdgeCount() <= swfas.getEdgeCount());

//		System.out.println("G = " + graph);
//		System.out.println(weights);
//		System.out.println("swfas: size=" + swfas.getEdgeCount() + ", weight=" + weight(swfas, weights) + ", " + swfas);
//		System.out.println("ssfas: size=" + ssfas.getEdgeCount() + ", weight=" + weight(ssfas, weights) + ", " + ssfas);
//		System.out.println();
		return time;
	}

	@Test
//	@Ignore
	public void testThreads() {
		SimpleDigraphAdapter<Integer> graph = new SimpleDigraphAdapter<Integer>();
		int tangleSize = 10;
		int tangleCount = 4;
		for (int nodeOffset = 0; nodeOffset < tangleCount * tangleSize; nodeOffset += tangleSize) {
			for (int source = 0; source < tangleSize; source++) {				
				for (int target = 0; target < tangleSize; target++) {
					if (source != target) {
						graph.add(nodeOffset + source, nodeOffset + target);
					}
				}
			}
		}
		Assert.assertEquals(tangleSize * (tangleSize - 1) * tangleCount, graph.getEdgeCount());
		Assert.assertEquals(tangleSize * tangleCount, graph.getVertexCount());
		EdgeWeights<Object> weights = EdgeWeights.UNIT_WEIGHTS;
		for (int threads = tangleCount; threads >= 0; threads--) {
			SimpleFeedbackArcSetProvider simpleProvider = new SimpleFeedbackArcSetProvider(threads);
			Digraph<Integer,?> fas = simpleProvider.getFeedbackArcSet(graph, weights, FeedbackArcSetPolicy.MIN_WEIGHT);
			Assert.assertEquals(graph.getVertexCount(), fas.getVertexCount());
			Assert.assertEquals(graph.getEdgeCount() / 2, fas.getEdgeCount());
			assertTrue(isFeedbackSet(graph, fas));
		}
	}

	@Test
	public void testRandomDigraphs() {
		Random rng = new Random(7);

		for (int nodeCount = 6; nodeCount <= 10; nodeCount++) {
			for (int edgeCount = 22; edgeCount <= 25; edgeCount++) {
				calculateFeedbackArcSets(rng, nodeCount, edgeCount, 1, 99);
				calculateFeedbackArcSets(rng, nodeCount, edgeCount, 1, 10);
				calculateFeedbackArcSets(rng, nodeCount, edgeCount, 1, 1);
				calculateFeedbackArcSets(rng, nodeCount, edgeCount, 90, 99);
			}
		}
	}

	@Test
	public void testPolicy() {
		FeedbackArcSetProvider provider = new SimpleFeedbackArcSetProvider();
		WeightedDigraph<Integer> graph = new WeightedDigraphAdapter<Integer>();
		graph.put(1, 2, 3);
		graph.put(2, 3, 1);
		graph.put(3, 1, 2);
		graph.put(2, 1, 1);

		FeedbackArcSet<Integer,?> fas;
		
		// minimum weight fas contains 2->3, 2->1
		fas = provider.getFeedbackArcSet(graph, graph, FeedbackArcSetPolicy.MIN_WEIGHT);
		assertFalse(fas.isExact());
		assertEquals(2, fas.getEdgeCount());
		assertEquals(2, fas.getWeight());
		assertTrue(fas.contains(2, 3));
		assertSame(graph.get(2, 3), fas.get(2, 3));
		assertTrue(fas.contains(2, 1));
		assertSame(graph.get(2, 1), fas.get(2, 1));
		
		// minimum size fas contains 1->2
		fas = provider.getFeedbackArcSet(graph, graph, FeedbackArcSetPolicy.MIN_SIZE);
		assertFalse(fas.isExact());
		assertEquals(1, fas.getEdgeCount());
		assertEquals(3, fas.getWeight());
		assertTrue(fas.contains(1, 2));
		assertSame(graph.get(1, 2), fas.get(1, 2));
	}
}
