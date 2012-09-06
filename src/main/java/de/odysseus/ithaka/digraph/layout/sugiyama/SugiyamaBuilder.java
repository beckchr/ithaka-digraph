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
package de.odysseus.ithaka.digraph.layout.sugiyama;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import de.odysseus.ithaka.digraph.Digraph;
import de.odysseus.ithaka.digraph.Digraphs;
import de.odysseus.ithaka.digraph.DoubledDigraph;
import de.odysseus.ithaka.digraph.EdgeWeights;
import de.odysseus.ithaka.digraph.layout.DigraphLayout;
import de.odysseus.ithaka.digraph.layout.DigraphLayoutArc;
import de.odysseus.ithaka.digraph.layout.DigrpahLayoutBuilder;
import de.odysseus.ithaka.digraph.layout.DigraphLayoutDimension;
import de.odysseus.ithaka.digraph.layout.DigraphLayoutDimensionProvider;
import de.odysseus.ithaka.digraph.layout.DigraphLayoutNode;
import de.odysseus.ithaka.digraph.layout.DigraphLayoutPoint;
import de.odysseus.ithaka.digraph.util.fas.FeedbackArcSet;
import de.odysseus.ithaka.digraph.util.fas.FeedbackArcSetPolicy;
import de.odysseus.ithaka.digraph.util.fas.SimpleFeedbackArcSetProvider;

/**
 * Sugiyama's algorithm. Work in progress...
 *
 * @author Christoph Beck
 *
 * @param <V> Vertex type
 * @param <E> Edge type
 */
public class SugiyamaBuilder<V, E> implements DigrpahLayoutBuilder<V, E> {
	protected final int horizontalSpacing;
	protected final int verticalSpacing;
	protected final boolean transpose;

	public SugiyamaBuilder(int horizontalSpacing, int verticalSpacing) {
		this(horizontalSpacing, verticalSpacing, false);
	}

	public SugiyamaBuilder(int horizontalSpacing, int verticalSpacing, boolean transpose) {
		this.horizontalSpacing = horizontalSpacing;
		this.verticalSpacing = verticalSpacing;
		this.transpose = transpose;
	}

	protected List<List<SugiyamaNode<V>>> createLayers(Digraph<SugiyamaNode<V>,?> graph) {
		List<List<SugiyamaNode<V>>> layers = new ArrayList<List<SugiyamaNode<V>>>();
		for (SugiyamaNode<V> node : graph.vertices()) {
			for (int i = layers.size(); i <= node.getLayer(); i++) {
				layers.add(new ArrayList<SugiyamaNode<V>>());
			}
			layers.get(node.getLayer()).add(node);
		}
		return layers;
	}

	protected DigraphLayoutDimension computeLayoutDimension(List<List<SugiyamaNode<V>>> layers) {
		int maxX = 0;
		for (List<SugiyamaNode<V>> layer : layers) {
			SugiyamaNode<V> last = layer.get(layer.size()-1);
			maxX = Math.max(maxX, last.getPoint().x + last.getDimension().w);
		}
		int maxY = 0;
		for (SugiyamaNode<V> node : layers.get(layers.size()-1)) {
			maxY = Math.max(maxY, node.getPoint().y + node.getDimension().h);
		}
		return new DigraphLayoutDimension(maxX, maxY);
	}

	private void computeNodePoints(Digraph<SugiyamaNode<V>,SugiyamaArc<V,E>> graph, List<List<SugiyamaNode<V>>> layers) {
		int minPosition = 0; // let the leftmost vertex(s) have position 0
		for (List<SugiyamaNode<V>> layer : layers) {
			minPosition = Math.min(minPosition, (int)layer.get(0).getPosition());
		}
		int levelY = -verticalSpacing;
		for (List<SugiyamaNode<V>> layer : layers) {
			int levelHeight = 0;
			levelY += verticalSpacing;
			for (SugiyamaNode<V> node : layer) {
				node.setPoint(new DigraphLayoutPoint((int)node.getPosition() - minPosition - node.getDimension().w / 2, levelY));
				levelHeight = Math.max(levelHeight, node.getDimension().h);
			}
			levelY += levelHeight;
			levelY += computeExtraVerticalSpacing(graph, layer);
		}
	}

	private int computeExtraVerticalSpacing(Digraph<SugiyamaNode<V>,SugiyamaArc<V,E>> graph, List<SugiyamaNode<V>> layer) {
		double maxHorizontalDistance = 0;
		for (SugiyamaNode<V> source : layer) {
			for (SugiyamaNode<V> target : graph.targets(source)) {
				maxHorizontalDistance = Math.max(maxHorizontalDistance, Math.abs(target.getPosition() - source.getPosition()));
			}
		}
		int maxExtraSpace = verticalSpacing / 2;
		int maxAcceptableAngle = 82; // magic
		double maxAcceptableTangens = Math.tan((2 * Math.PI * maxAcceptableAngle) / 360);
		if (maxHorizontalDistance / verticalSpacing <= maxAcceptableTangens) {
			return 0;
		}
		int extraSpace = Math.min(maxExtraSpace, (int)(maxHorizontalDistance / maxAcceptableTangens) - verticalSpacing );
//		System.out.println("maxAngle = " + Math.atan(maxHorizontalDistance / verticalSpacing) * 360 / (2 * Math.PI) + ", extraSpace = " + extraSpace + ", newMaxAngle = " + Math.atan(maxHorizontalDistance / (verticalSpacing + extraSpace)) * 360 / (2 * Math.PI));
		return Math.min(maxExtraSpace, extraSpace);
	}

	/**
	 * Split long arc into segment with intermediate dummy nodes
	 * @param graph graph
	 * @param arc long arc
	 * @param zero dimension used for dummy nodes
	 */
	private void insertSegment(Digraph<SugiyamaNode<V>,SugiyamaArc<V,E>> graph, SugiyamaArc<V,E> arc, DigraphLayoutDimension zero) {
		assert arc.getTarget().getLayer() > arc.getSource().getLayer() + 1;

		SugiyamaNode<V> source = arc.getSource();
		SugiyamaNode<V> target = arc.getTarget();
		SugiyamaNode<V> lower = new SugiyamaNode<V>(zero);
		lower.setLayer(source.getLayer() + 1);
		lower.setUpper(source);
		graph.put(source, lower, new SugiyamaArc<V,E>(source, lower, arc.isFeedback(), arc.getEdge()));
		for (int layer = source.getLayer() + 2; layer < target.getLayer(); layer++) {
			SugiyamaNode<V> upper = lower;
			lower = new SugiyamaNode<V>(zero);
			lower.setLayer(layer);
			lower.setUpper(upper);
			upper.setLower(lower);
			graph.put(upper, lower, new SugiyamaArc<V,E>(upper, lower, arc.isFeedback(), arc.getEdge()));
		}
		lower.setLower(target);
		graph.put(lower, target, new SugiyamaArc<V,E>(lower, target, arc.isFeedback(), arc.getEdge()));
	}

	/**
	 * Split long arcs by inserting dummy nodes
	 */
	protected void insertDummyNodes(Digraph<SugiyamaNode<V>,SugiyamaArc<V,E>> graph, DigraphLayoutDimensionProvider<V> dimensions) {
		List<SugiyamaArc<V,E>> longArcs = new LinkedList<SugiyamaArc<V,E>>();
		for (SugiyamaNode<V> source : graph.vertices()) {
			for (SugiyamaNode<V> target : graph.targets(source)) {
				if (target.getLayer() - source.getLayer() > 1) {
					longArcs.add(graph.get(source, target));
				}
			}
		}
		DigraphLayoutDimension zero = dimensions.getDimension(null);
		for (SugiyamaArc<V,E> arc : longArcs) {
			insertSegment(graph, arc, zero);
			if (arc.getBackArc() != null) {
				insertSegment(graph, arc.getBackArc(), zero);
			}
			graph.remove(arc.getSource(), arc.getTarget());
		}
	}

	protected void removeDummyNodes(Digraph<SugiyamaNode<V>,SugiyamaArc<V,E>> graph) {
		Set<SugiyamaNode<V>> dummies = new HashSet<SugiyamaNode<V>>();
		List<SugiyamaArc<V,E>> newArcs = new LinkedList<SugiyamaArc<V,E>>();
		List<SugiyamaArc<V,E>> oldArcs = new LinkedList<SugiyamaArc<V,E>>();
		for (SugiyamaNode<V> source : graph.vertices()) {
			if (source.isDummy()) {
				dummies.add(source);
			} else {
				for (SugiyamaNode<V> target : graph.targets(source)) {
					if (target.isDummy()) {
						List<DigraphLayoutPoint> points = graph.get(source, target).getBendPoints();
						SugiyamaNode<V> current = target;
						SugiyamaNode<V> previous = null;
						do {
							if (current.getUpper().getPosition() != current.getPosition() || current.getLower().getPosition() != current.getPosition()) {
								if (points.isEmpty()) {
									points = new LinkedList<DigraphLayoutPoint>();
								}
								points.add(current.getPoint());
								List<DigraphLayoutPoint> additionalBends = graph.get(current, current.getLower()).getBendPoints();
								if (additionalBends != null) {
									points.addAll(additionalBends);
								}
							}
							previous = current;
							current = current.getLower();
						} while (current.isDummy());
						SugiyamaArc<V,E> firstArc = graph.get(source, target);
						SugiyamaArc<V,E> lastArc = graph.get(previous, current);
						SugiyamaArc<V,E> newArc = null;
						if (firstArc.isFeedback()) {
							newArc = new SugiyamaArc<V,E>(current, source, true, firstArc.getEdge());
							newArc.setSourceSlot(lastArc.getTargetSlot());
							newArc.setTargetSlot(firstArc.getSourceSlot());
							if (!points.isEmpty()) {
								Collections.reverse(points);
							}
							newArc.setBendPoints(points);
						} else {
							newArc = new SugiyamaArc<V,E>(source, current, false, firstArc.getEdge());
							newArc.setSourceSlot(firstArc.getSourceSlot());
							newArc.setTargetSlot(lastArc.getTargetSlot());
							newArc.setBendPoints(points);
						}
						newArcs.add(newArc);
					} else {
						SugiyamaArc<V,E> arc = graph.get(source, target);
						if (arc.isFeedback()) {
							SugiyamaArc<V,E> newArc = new SugiyamaArc<V,E>(target, source, true, arc.getEdge());
							newArc.setSourceSlot(arc.getTargetSlot());
							newArc.setTargetSlot(arc.getSourceSlot());
							newArc.setBendPoints(arc.getBendPoints());
							newArcs.add(newArc);
							oldArcs.add(arc);
						}
						if (arc.getBackArc() != null) {
							newArcs.add(arc.getBackArc());
						}
					}
				}
			}
		}
		graph.removeAll(dummies);
		for (SugiyamaArc<V,E> arc : oldArcs) {
			graph.remove(arc.getSource(), arc.getTarget());
		}
		for (SugiyamaArc<V,E> arc : newArcs) {
			graph.put(arc.getSource(), arc.getTarget(), arc);
		}
	}

	private void fixEndPoints(Digraph<SugiyamaNode<V>,SugiyamaArc<V,E>> graph) {
		for (SugiyamaNode<V> source : graph.vertices()) {
			for (SugiyamaNode<V> target : graph.targets(source)) {
				graph.get(source, target).fixEndPoints();
			}
		}
	}

	/**
	 * add bend point to feedback arc (v,w) if (w,v) is also present.
	 */
	protected void finalizeLayout(Digraph<SugiyamaNode<V>, SugiyamaArc<V, E>> graph) {
		int minHorizontalDistance = Math.min(transpose ? 12 : 16, horizontalSpacing);
		int minVerticalDistance = 0; // vertical displacement currently not used
		for (SugiyamaNode<V> source : graph.vertices()) {
			for (SugiyamaNode<V> target : graph.targets(source)) {
				SugiyamaArc<V, E> arc1 = graph.get(source, target);
				if (arc1.isFeedback() && graph.contains(target, source) && arc1.getBendPoints().isEmpty()) {
					SugiyamaArc<V, E> arc2 = graph.get(target, source);
					if (arc2.getBendPoints().isEmpty()) {
						boolean bend = false;
						// horizontal displacement
						int x1 = (arc1.getStartPoint().x + arc1.getEndPoint().x) / 2;
						int x2 = (arc2.getStartPoint().x + arc2.getEndPoint().x) / 2;								
						int horizontalDistance = Math.abs(x2 - x1);
						if (horizontalDistance < minHorizontalDistance) {
							int delta = minHorizontalDistance - horizontalDistance + 1;
							x1 = arc1.getStartPoint().x <= arc2.getEndPoint().x ? x1 - delta/2 : x1 + delta/2;
							x2 = arc1.getStartPoint().x <= arc2.getEndPoint().x ? x2 + delta/2 : x2 - delta/2;
							bend = true;
						}
						// vertical displacement
						int y1 = (arc1.getStartPoint().y + arc1.getEndPoint().y) / 2;
						int y2 = (arc2.getStartPoint().y + arc2.getEndPoint().y) / 2;
						int verticalDistance = Math.abs(y2 - y1);
						if (verticalDistance < minVerticalDistance) {
							int delta = minVerticalDistance - verticalDistance + 1;
							y1 = arc1.getStartPoint().x <= arc1.getEndPoint().x ? y1 - delta/2 : y1 + delta/2;
							y2 = arc2.getEndPoint().x <= arc2.getStartPoint().x ? y2 + delta/2 : y2 - delta/2;
							bend = true;
						}
						if (bend) { // add bend points
							List<DigraphLayoutPoint> points = null;
							points = new LinkedList<DigraphLayoutPoint>();
							points.add(new DigraphLayoutPoint(x1, y1));
							arc1.setBendPoints(points);
							points = new LinkedList<DigraphLayoutPoint>();
							points.add(new DigraphLayoutPoint(x2, y2));
							arc2.setBendPoints(points);
						}
					}
				}
			}
		}
	}

	protected DoubledDigraph<SugiyamaNode<V>,SugiyamaArc<V,E>> createLayoutGraph(Digraph<V,E> graph, DigraphLayoutDimensionProvider<V> dimensions, Digraph<V,?> feedback) {
		return new SugiyamaStep1<V,E>().createLayoutGraph(graph, dimensions, feedback, horizontalSpacing);
	}

	protected void minimizeCrossings(Digraph<SugiyamaNode<V>,SugiyamaArc<V,E>> graph, List<List<SugiyamaNode<V>>> layers) {
		new SugiyamaStep2<V,E>().minimizeCrossings(graph, layers);
	}

	protected void adjustNodePositions(Digraph<SugiyamaNode<V>,SugiyamaArc<V,E>> graph, List<List<SugiyamaNode<V>>> layers) {
		new SugiyamaStep3<V>(horizontalSpacing).adjustNodePositions(graph, layers);
	}

	protected void routeArcs(Digraph<SugiyamaNode<V>,SugiyamaArc<V,E>> graph, List<List<SugiyamaNode<V>>> layers) {
		new SugiyamaStep4<V,E>().routeArcs(graph, layers);
	}

	@Override
	public DigraphLayout<V,E> build(Digraph<V,E> digraph, DigraphLayoutDimensionProvider<V> dimensions) {
		FeedbackArcSet<V,E> feedback =
			new SimpleFeedbackArcSetProvider().getFeedbackArcSet(digraph, EdgeWeights.UNIT_WEIGHTS, FeedbackArcSetPolicy.MIN_WEIGHT);
		return layout(digraph, dimensions, feedback);
	}

	public DigraphLayout<V,E> layout(Digraph<V,E> digraph, DigraphLayoutDimensionProvider<V> dimensions, Digraph<V,?> feedback) {
		if (digraph.getVertexCount() == 0) {
			return new DigraphLayout<V,E>(Digraphs.<DigraphLayoutNode<V>,DigraphLayoutArc<V,E>>emptyDigraph(), new DigraphLayoutDimension(0, 0));
		}
		DoubledDigraph<SugiyamaNode<V>,SugiyamaArc<V,E>> graph = createLayoutGraph(digraph, dimensions, feedback);
		insertDummyNodes(graph, dimensions);
		if (transpose) {
			for (SugiyamaNode<V> source : graph.vertices()) {
				source.getDimension().transpose();
			}
		}
		List<List<SugiyamaNode<V>>> layers = createLayers(graph);
		minimizeCrossings(graph, layers);
		adjustNodePositions(graph, layers);
		computeNodePoints(graph, layers);
		DigraphLayoutDimension dimension = computeLayoutDimension(layers);
		routeArcs(graph, layers);
		removeDummyNodes(graph);
		fixEndPoints(graph);
		finalizeLayout(graph);
		if (transpose) {
			for (SugiyamaNode<V> source : graph.vertices()) {
				source.getPoint().transpose();
				source.getDimension().transpose();
				for (SugiyamaNode<V> target : graph.targets(source)) {
					SugiyamaArc<V, E> arc = graph.get(source, target);
					arc.getStartPoint().transpose();
					arc.getEndPoint().transpose();
					for (DigraphLayoutPoint point : arc.getBendPoints()) {
						point.transpose();
					}
				}
			}
			dimension.transpose();
		}
		return new DigraphLayout<V,E>(graph, dimension);
	}
}
