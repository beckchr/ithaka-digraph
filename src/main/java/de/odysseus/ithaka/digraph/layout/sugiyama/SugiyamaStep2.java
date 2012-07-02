/*
 * Copyright 2008 Odysseus Software GmbH, Frankfurt am Main/Germany.
 */
package de.odysseus.ithaka.digraph.layout.sugiyama;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import de.odysseus.ithaka.digraph.Digraph;
import de.odysseus.ithaka.digraph.Digraphs;

/**
 * Order nodes by minimizing arc crossings.
 */
public class SugiyamaStep2<V,E> {
	private static final Comparator<SugiyamaArc<?,?>> CMP_ARCS = new Comparator<SugiyamaArc<?,?>>() {
		@Override
		public int compare(SugiyamaArc<?,?> arc1, SugiyamaArc<?,?> arc2) {
			if (arc1.getSource().getPosition() < arc2.getSource().getPosition()) {
				return -1;
			}
			if (arc1.getSource().getPosition() > arc2.getSource().getPosition()) {
				return 1;
			}
			if (arc1.getTarget().getPosition() < arc2.getTarget().getPosition()) {
				return -1;
			}
			if (arc1.getTarget().getPosition() > arc2.getTarget().getPosition()) {
				return 1;
			}
			return 0;
		}
	};

	private static final Comparator<SugiyamaNode<?>> CMP_CENTER = new Comparator<SugiyamaNode<?>>() {
		@Override
		public int compare(SugiyamaNode<?> node1, SugiyamaNode<?> node2) {
			if (node1.getTemporary() < node2.getTemporary()) {
				return -1;
			}
			if (node1.getTemporary() > node2.getTemporary()) {
				return 1;
			}
			return Double.compare(node1.getPosition(), node2.getPosition());
		}
	};

	private static final Comparator<SugiyamaNode<?>> CMP_TEMPORARY = new Comparator<SugiyamaNode<?>>() {
		@Override
		public int compare(SugiyamaNode<?> node1, SugiyamaNode<?> node2) {
			return node1.getTemporary() < node2.getTemporary() ? -1 : node1.getTemporary() > node2.getTemporary() ? 1 : 0;
		}
	};

	private final int FORGIVENESS = 2;
	private final int MAX_ROUNDS = 16;

	/**
	 * Minimize crossings.
	 */
	public void minimizeCrossings(Digraph<SugiyamaNode<V>,SugiyamaArc<V,E>> graph, List<List<SugiyamaNode<V>>> layers) {
//		long startTime = System.currentTimeMillis();
		List<List<SugiyamaArc<V,E>>> arcs = createArcLayers(graph, layers);
		computeTemporary(graph, layers);
		int score = Integer.MAX_VALUE;
		Random random = new Random(7);
//		long currentTime;
//		System.out.println("preprocessed in " + ((currentTime = System.currentTimeMillis()) - startTime) + " ms");
//		startTime = currentTime;
		for (int i = 0; i < MAX_ROUNDS && score > 0; i++) {
			for (List<SugiyamaNode<V>> layer : layers) {
				Collections.shuffle(layer, random);
				Collections.sort(layer, CMP_TEMPORARY);
			}
			int crossings = Integer.MAX_VALUE;
			sweepDown(graph, layers);
			for (int forgiveness = FORGIVENESS; forgiveness >= 0;) {
				sweepUp(graph, layers);
				sweepDown(graph, layers);
				int newCrossings = countCrossings(layers, arcs);
				if (newCrossings >= crossings) {
					forgiveness--;
				} else {
					crossings = newCrossings;
					if (crossings < score) {
						for (List<SugiyamaNode<V>> layer : layers) {
							for (int j = 0; j < layer.size(); j++) {
								layer.get(j).setIndex(j);
							}
						}
						score = crossings;
					}
				}
//				System.out.print(" " + crossings);
			}
//			System.out.println();
		}
//		System.out.println("sweeped (-> " + score + ") in " + ((currentTime = System.currentTimeMillis()) - startTime) + " ms");
//		startTime = currentTime;
		for (List<SugiyamaNode<V>> layer : layers) {
			Collections.sort(layer, SugiyamaNode.CMP_INDEX);
		}
		for (List<SugiyamaNode<V>> layer : layers) {
			for (int j = 0; j < layer.size(); j++) {
				layer.get(j).setPosition(j);
			}
		}
		boolean changedByFirstStep, changedBySecondStep = false;
		do {
			changedByFirstStep = swapNeighbors(layers, arcs);
//			score = countCrossings(layers, arcs);
//			System.out.println("swapped (-> " + score + ") in " + ((currentTime = System.currentTimeMillis()) - startTime) + " ms");
//			startTime = currentTime;

			if (changedByFirstStep || !changedBySecondStep) {
				changedBySecondStep = combArcs(graph, true);
				changedBySecondStep |= combArcs(graph.reverse(), false);
				for (List<SugiyamaNode<V>> layer : layers) {
					Collections.sort(layer, SugiyamaNode.CMP_INDEX);
				}
				for (List<SugiyamaNode<V>> layer : layers) {
					for (int j = 0; j < layer.size(); j++) {
						layer.get(j).setPosition(j);
					}
				}
//				score = countCrossings(layers, arcs);
//				System.out.println("combed (-> " + score + ") in " + ((currentTime = System.currentTimeMillis()) - startTime) + " ms");
//				startTime = currentTime;
			} else {
				changedBySecondStep = false;
			}
		} while (changedBySecondStep);
	}

	private boolean swapNeighbors(List<List<SugiyamaNode<V>>> nodes, List<List<SugiyamaArc<V,E>>> arcs) {
		// TODO changed to go through layers only once (cbe); is this ok?
//		if (true) return false;
		boolean changed = false;
//		boolean swapped;
//		do {
//			swapped = false;
			Iterator<List<SugiyamaNode<V>>> nodeLayers = nodes.iterator();
			List<SugiyamaNode<V>> lowerNodes = nodeLayers.next();
			Iterator<List<SugiyamaArc<V, E>>> arcLayers = arcs.iterator();
			List<SugiyamaArc<V,E>> lowerArcs = null;
			while (lowerNodes != null) {
				List<SugiyamaNode<V>> upperNodes = lowerNodes;
				lowerNodes = nodeLayers.hasNext() ? nodeLayers.next() : null;
				List<SugiyamaArc<V,E>> upperArcs = lowerArcs;
				lowerArcs = arcLayers.hasNext() ? arcLayers.next() : null;
				int before = countCrossings(upperArcs, upperNodes, lowerArcs, lowerNodes);
				for (int left = 0, right = 1; right < upperNodes.size(); left++, right++) {
					SugiyamaNode<V> leftNode = upperNodes.get(left);
					SugiyamaNode<V> rightNode = upperNodes.get(right);
					double leftPosition = leftNode.getPosition();
					double rightPosition = rightNode.getPosition();
					leftNode.setPosition(rightPosition);
					rightNode.setPosition(leftPosition);
					int after = countCrossings(upperArcs, upperNodes, lowerArcs, lowerNodes);
					if (before <= after) {
						leftNode.setPosition(leftPosition);
						rightNode.setPosition(rightPosition);
					} else {
						int leftIndex = leftNode.getIndex();
						leftNode.setIndex(rightNode.getIndex());
						rightNode.setIndex(leftIndex);
						upperNodes.set(left, rightNode);
						upperNodes.set(right, leftNode);
						changed = true;
//						swapped = true;
						before = after;
					}
				}
			}
//		} while (swapped);
		return changed;
	}

	private int countCrossings(List<SugiyamaArc<V, E>> upperArcs, List<SugiyamaNode<V>> upperNodes, List<SugiyamaArc<V, E>> lowerArcs, List<SugiyamaNode<V>> lowerNodes) {
		int crossings = 0;
		if (upperArcs != null) {
			crossings += countCrossings(upperArcs, upperNodes.size());
		}
		if (lowerArcs != null) {
			crossings += countCrossings(lowerArcs, lowerNodes.size());
		}
		return crossings;
	}

	private boolean combArcs(Digraph<SugiyamaNode<V>,SugiyamaArc<V, E>> graph, boolean downwards) {
//		if (true) return false;
		boolean changed = false;
		for(SugiyamaNode<V> source : graph.vertices()) {
			if (graph.getOutDegree(source) > 1) {
				List<List<SugiyamaNode<V>>> targetLayers = new ArrayList<List<SugiyamaNode<V>>>();
				List<SugiyamaNode<V>> targetLayer = new ArrayList<SugiyamaNode<V>>();
				for (SugiyamaNode<V> target : graph.targets(source)) {
					targetLayer.add(target);
				}
				List<SugiyamaNode<V>> targetList = new ArrayList<SugiyamaNode<V>>();
				while (!targetLayer.isEmpty()) {
					Collections.sort(targetLayer, SugiyamaNode.CMP_INDEX);
					targetLayers.add(targetLayer);
					Iterator<SugiyamaNode<V>> targets = targetLayer.iterator();
					targetLayer = new ArrayList<SugiyamaNode<V>>();
					for (int position = 0, insertIndex = 0; targets.hasNext(); position++) {
						SugiyamaNode<V> node = targets.next();
						if (node.isDummy()) {
							node.setPosition(-1);
							SugiyamaNode<V> next = downwards ? node.getLower() : node.getUpper();
							targetLayer.add(next);
							if (!next.isDummy()) {
								assert (downwards ? next.getLower() : next.getUpper()) == null;
								if ((downwards ? next.getUpper() : next.getLower()) == null) {
									if (downwards) next.setUpper(node); else next.setLower(node);
								} else {
									if (downwards) next.setLower(node); else next.setUpper(node);
								}
							}
						} else {
							node.setPosition((downwards ? node.getLower() : node.getUpper()) == null ? position : position - 1);
							targetList.add(insertIndex++, node);
						}
					}
				};
				int[] maxDummyPositions = new int[targetLayers.size()];
				Arrays.fill(maxDummyPositions, -1);
				for (SugiyamaNode<V> target : targetList) {
					setPositions(target, targetLayers, maxDummyPositions, source, downwards);
				}
				for (List<SugiyamaNode<V>> layer : targetLayers) {
					List<Integer> indices = new ArrayList<Integer>(layer.size());
					for (SugiyamaNode<V> node : layer) {
						indices.add(node.getIndex());
					}
					for (SugiyamaNode<V> node : layer) {
						int position = (int)Math.round(node.getPosition());
//						node.setIndex(indices.get(position));
						int index = indices.get(position);
						if (node.getIndex() != index) {
							node.setIndex(index);
							changed = true;
						}
					}
				}
			}
		}
		return changed;
	}

	private void setPositions(SugiyamaNode<V> node, List<List<SugiyamaNode<V>>> targetLayers, int[] maxDummyPositions, SugiyamaNode<V> source, boolean downwards) {
		int firstLayer = source.getLayer() + (downwards ? 1 : -1);
		int position = (int)Math.round(node.getPosition());
		while (downwards ? node.getUpper() != null && node.getUpper() != source : node.getLower() != null && node.getLower() != source) {
			SugiyamaNode<V> next = downwards ? node.getUpper() : node.getLower();
			assert next.isDummy();
			if (!node.isDummy()) {
				if ((downwards ? node.getLower() : node.getUpper()) == null) {
					if (downwards) node.setUpper(null); else node.setLower(null);
				} else {
					if (downwards) {
						node.setUpper(node.getLower());
						node.setLower(null);
					} else {
						node.setLower(node.getUpper());
						node.setUpper(null);
					}
					node.setPosition(node.getPosition() + 1);
				}
			}
			node = next;
			int layerIndex = Math.abs(node.getLayer() - firstLayer);
			List<SugiyamaNode<V>> layer = targetLayers.get(layerIndex);
			while (!layer.get(position).isDummy() || position <= maxDummyPositions[layerIndex]) {
				if(!layer.get(position).isDummy()) {
					setPositions(layer.get(position), targetLayers, maxDummyPositions, source, downwards);
				}
				position++;
			}
			node.setPosition(position);
			maxDummyPositions[layerIndex] = position;
		}
	}

	private void computeTemporary(Digraph<SugiyamaNode<V>,SugiyamaArc<V,E>> graph, List<List<SugiyamaNode<V>>> layers) {
		int group = 0;
		for (Set<SugiyamaNode<V>> component : Digraphs.wcc(graph)) {
			group++;
			for (SugiyamaNode<V> node : component) {
				node.setTemporary(group);
			}
		}
	}

	private void sweepUp(Digraph<SugiyamaNode<V>,SugiyamaArc<V,E>> graph, List<List<SugiyamaNode<V>>> layers) {
		// sweep up: sort layers size-2, ..., 0 by down-barycenter (average target position)
		for (int i = layers.size() - 1; i > 0; i--) {
			List<SugiyamaNode<V>> lower = layers.get(i);
			List<SugiyamaNode<V>> upper = layers.get(i-1);
			for (int j = 0; j < lower.size(); j++) {
				lower.get(j).setPosition(j);
			}
			reorder(graph, upper);
		}
		List<SugiyamaNode<V>> top = layers.get(0);
		for (int j = 0; j < top.size(); j++) {
			top.get(j).setPosition(j);
		}
	}

	private void sweepDown(Digraph<SugiyamaNode<V>,SugiyamaArc<V,E>> graph, List<List<SugiyamaNode<V>>> layers) {
		// sweep down: sort layers 1, ..., size-1 by up-barycenter (average source position)
		for (int i = 0; i < layers.size() - 1; i++) {
			List<SugiyamaNode<V>> upper = layers.get(i);
			List<SugiyamaNode<V>> lower = layers.get(i+1);
			for (int j = 0; j < upper.size(); j++) {
				upper.get(j).setPosition(j);
			}
			reorder(graph.reverse(), lower);
		}
		List<SugiyamaNode<V>> bottom = layers.get(layers.size() - 1);
		for (int j = 0; j < bottom.size(); j++) {
			bottom.get(j).setPosition(j);
		}
	}

	private List<List<SugiyamaArc<V,E>>> createArcLayers(Digraph<SugiyamaNode<V>,SugiyamaArc<V,E>> graph, List<List<SugiyamaNode<V>>> layers) {
		List<List<SugiyamaArc<V,E>>> result = new ArrayList<List<SugiyamaArc<V,E>>>(layers.size() - 1);
		for (int i = 0; i < layers.size() - 1; i++) {
			List<SugiyamaArc<V,E>> list = new ArrayList<SugiyamaArc<V,E>>();
			for (SugiyamaNode<V> source : layers.get(i)) {
				for (SugiyamaNode<V> target : graph.targets(source)) {
					list.add(graph.get(source, target));
				}
			}
			result.add(list);
		}
		return result;
	}

	private int countCrossings(List<List<SugiyamaNode<V>>> nodes, List<List<SugiyamaArc<V,E>>> arcs) {
		int crossings = 0;
		Iterator<List<SugiyamaNode<V>>> south = nodes.iterator();
		south.next();
		for (List<SugiyamaArc<V,E>> list : arcs) {
			crossings += countCrossings(list, south.next().size());
		}
		return crossings;
	}

	/**
	 * W. Barth et al., Bilayer Cross Counting, JGAA, 8(2) 179-194 (2004)
	 * @param q number of southern nodes (max. target position)
	 */
	private int countCrossings(List<SugiyamaArc<V,E>> arcs, int q) {
		Collections.sort(arcs, CMP_ARCS);
		int crossings = 0;
		int firstLeafIndex = 1;
		while (firstLeafIndex < q) {
			firstLeafIndex *= 2;
		}
		firstLeafIndex -= 1;
		int[] tree = new int[firstLeafIndex + q + 2];
		for (SugiyamaArc<V,E> arc : arcs) {
			int index = firstLeafIndex + (int)arc.getTarget().getPosition();
			tree[index]++;
			while (index > 0) {
				if (index % 2 != 0) {
					crossings += tree[index + 1];
				}
				index = (index - 1) / 2;
				tree[index]++;
			}
		}
		return crossings;
	}

	/**
	 * Compute center of specified layer node.
	 * This implementation computes the baricenter.
	 * May be overridden to use another method, e.g. median.
	 * @param graph
	 * @param source
	 * @return center of specified layer node
	 */
	private double center(Digraph<SugiyamaNode<V>,?> graph, SugiyamaNode<V> source) {
		double weight = 0;
		for (SugiyamaNode<V> target : graph.targets(source)) {
			weight += target.getPosition();
		}
		return weight / graph.getOutDegree(source);
	}

	/**
	 * Reorder a level by centric method by setting positions.
	 * @param layer the level to be reordered by barycenter
	 */
	private void reorder(Digraph<SugiyamaNode<V>,?> graph, List<SugiyamaNode<V>> layer) {
		double maxCenter = 0.0;
		for (SugiyamaNode<V> source : layer) {
			double center = graph.getOutDegree(source) > 0 ? center(graph, source) : maxCenter;
			if (center > maxCenter) {
				maxCenter = center;
			}
			source.setPosition(center);
		}
		Collections.sort(layer, CMP_CENTER);
	}
}
