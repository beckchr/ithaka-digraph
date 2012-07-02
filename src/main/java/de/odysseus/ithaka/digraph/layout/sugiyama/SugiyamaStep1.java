/*
 * Copyright 2008 Odysseus Software GmbH, Frankfurt am Main/Germany.
 */
package de.odysseus.ithaka.digraph.layout.sugiyama;

import java.util.HashMap;
import java.util.Map;

import de.odysseus.ithaka.digraph.Digraph;
import de.odysseus.ithaka.digraph.DigraphFactory;
import de.odysseus.ithaka.digraph.Digraphs;
import de.odysseus.ithaka.digraph.DoubledDigraph;
import de.odysseus.ithaka.digraph.DoubledDigraphAdapter;
import de.odysseus.ithaka.digraph.MapDigraph;
import de.odysseus.ithaka.digraph.layout.LayoutDimensionProvider;

/**
 * Create layout graph and assign layer numbers
 */
public class SugiyamaStep1<V,E> {
	private DigraphFactory<Digraph<SugiyamaNode<V>,SugiyamaArc<V,E>>> factory = new DigraphFactory<Digraph<SugiyamaNode<V>,SugiyamaArc<V,E>>>() {
		@Override
		public Digraph<SugiyamaNode<V>,SugiyamaArc<V,E>> create() {
			return new MapDigraph<SugiyamaNode<V>, SugiyamaArc<V,E>>(SugiyamaNode.CMP_ID);
		}
	};

	public DoubledDigraph<SugiyamaNode<V>,SugiyamaArc<V,E>> createLayoutGraph(Digraph<V,E> graph, LayoutDimensionProvider<V> dimensions, Digraph<V,?> feedback, int horizontalSpacing) {
		DoubledDigraph<SugiyamaNode<V>,SugiyamaArc<V,E>> result =
			DoubledDigraphAdapter.getAdapterFactory(factory).create();

		Map<V,SugiyamaNode<V>> map = new HashMap<V, SugiyamaNode<V>>();

		// create nodes
		for (V vertex : graph.vertices()) {
			SugiyamaNode<V> node = new SugiyamaNode<V>(vertex, dimensions.getDimension(vertex), horizontalSpacing);
			map.put(vertex, node);
			result.add(node);
		}

		// create arcs
		for (V source : graph.vertices()) {
			for (V target : graph.targets(source)) {
				SugiyamaNode<V> s = map.get(source);
				SugiyamaNode<V> t = map.get(target);
				E e = graph.get(source, target);
				if (feedback.contains(source, target)) {
					if (graph.contains(target, source)) {
						result.put(t, s, new SugiyamaArc<V,E>(t, s, e, graph.get(target, source)));
					} else {
						result.put(t, s, new SugiyamaArc<V,E>(t, s, true, e));
					}
				} else if (!graph.contains(target, source)) {
					result.put(s, t, new SugiyamaArc<V,E>(s, t, false, e));
				}
			}
		}
		assert Digraphs.isAcyclic(result);

		computeNodeLayers(result);

		return result;
	}

	protected void computeNodeLayers(DoubledDigraph<SugiyamaNode<V>,SugiyamaArc<V,E>> graph) {
		int maxLayer = minLayers(graph);
		shiftDown(graph, 0, maxLayer);
	}

	private void shiftDown(DoubledDigraph<SugiyamaNode<V>,SugiyamaArc<V,E>> graph, int minLayer, int maxLayer) {
		boolean changed;
		do {
			changed = false;
			for (SugiyamaNode<V> node : graph.vertices()) {
				int minTargetLayer = maxLayer + 1;
				for (SugiyamaNode<V> target : graph.targets(node)) {
					minTargetLayer = Math.min(minTargetLayer, target.getLayer());
				}
				assert node.getLayer() < minTargetLayer;
				if (minTargetLayer <= maxLayer && minTargetLayer > node.getLayer() + 1) {
					changed = true;
					node.setLayer(minTargetLayer - 1);
				}
			}
		} while (changed);
	}

	@SuppressWarnings("unused")
	private void shiftUp(DoubledDigraph<SugiyamaNode<V>,SugiyamaArc<V,E>> graph, int minLayer, int maxLayer) {
		boolean changed;
		do {
			changed = false;
			for (SugiyamaNode<V> node : graph.vertices()) {
				int maxSourceLayer = minLayer - 1;
				for (SugiyamaNode<V> source : graph.sources(node)) {
					maxSourceLayer = Math.max(maxSourceLayer, source.getLayer());
				}
				assert maxSourceLayer < node.getLayer();
				if (maxSourceLayer >= minLayer && maxSourceLayer < node.getLayer() - 1) {
					changed = true;
					node.setLayer(maxSourceLayer + 1);
				}
			}
		} while (changed);
	}


	@SuppressWarnings("unused")
	private void shiftMiddle(DoubledDigraph<SugiyamaNode<V>,SugiyamaArc<V,E>> graph, int minLayer, int maxLayer) {
		boolean changed;
		do {
			changed = false;
			for (SugiyamaNode<V> node : graph.vertices()) {
				int minTargetLayer = maxLayer + 1;
				for (SugiyamaNode<V> target : graph.targets(node)) {
					minTargetLayer = Math.min(minTargetLayer, target.getLayer());
				}
				int maxSourceLayer = minLayer - 1;
				for (SugiyamaNode<V> source : graph.sources(node)) {
					maxSourceLayer = Math.max(maxSourceLayer, source.getLayer());
				}
				assert maxSourceLayer < node.getLayer() && node.getLayer() < minTargetLayer;
				if (minTargetLayer <= maxLayer && maxSourceLayer >= minLayer) { // middle
					int nodeLayer = (maxSourceLayer + minTargetLayer) / 2;
					if (node.getLayer() != nodeLayer) {
						changed = true;
						node.setLayer(nodeLayer);
					}
				} else if (minTargetLayer <= maxLayer && minTargetLayer > node.getLayer() + 1) { // down
					changed = true;
					node.setLayer(minTargetLayer - 1);
				} else if (maxSourceLayer >= minLayer && maxSourceLayer < node.getLayer() - 1) { // up
					changed = true;
					node.setLayer(maxSourceLayer + 1);
				}
			}
		} while (changed);
	}

	/**
	 * Compute minimum layers for nodes
	 * @param graph
	 * @return the maximum layer number
	 */
	private int minLayers(Digraph<SugiyamaNode<V>,SugiyamaArc<V,E>> graph) {
		int maxLayer = 0;
		for (SugiyamaNode<V> node : graph.vertices()) {
			if (graph.reverse().getOutDegree(node) == 0) {
				maxLayer = Math.max(maxLayer, minLayers(graph, node));
			}
		}
		return maxLayer;
	}

	/**
	 * Compute minimum layers for nodes reachable from specified source
	 * @return the maximum layer number
	 */
	private int minLayers(Digraph<SugiyamaNode<V>,SugiyamaArc<V,E>> graph, SugiyamaNode<V> source) {
		int maxLayer = source.getLayer();
		for (SugiyamaNode<V> target : graph.targets(source)) {
			if (target.getLayer() <= source.getLayer()) {
				target.setLayer(source.getLayer() + 1);
				maxLayer = Math.max(maxLayer, minLayers(graph, target));
			}
		}
		return maxLayer;
	}
}
