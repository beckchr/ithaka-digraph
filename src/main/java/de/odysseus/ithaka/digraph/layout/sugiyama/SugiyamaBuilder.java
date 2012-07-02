/*
 * Copyright 2008 Odysseus Software GmbH, Frankfurt am Main/Germany.
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
import de.odysseus.ithaka.digraph.fas.FeedbackArcSet;
import de.odysseus.ithaka.digraph.fas.FeedbackArcSetPolicy;
import de.odysseus.ithaka.digraph.fas.SimpleFeedbackArcSetProvider;
import de.odysseus.ithaka.digraph.layout.Layout;
import de.odysseus.ithaka.digraph.layout.LayoutArc;
import de.odysseus.ithaka.digraph.layout.LayoutBuilder;
import de.odysseus.ithaka.digraph.layout.LayoutDimension;
import de.odysseus.ithaka.digraph.layout.LayoutDimensionProvider;
import de.odysseus.ithaka.digraph.layout.LayoutNode;
import de.odysseus.ithaka.digraph.layout.LayoutPoint;

/**
 * Sugiyama's algorithm. Work in progress...
 *
 * @author Christoph Beck
 *
 * @param <V> Vertex type
 * @param <E> Edge type
 */
public class SugiyamaBuilder<V, E> implements LayoutBuilder<V, E> {
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

	protected LayoutDimension computeLayoutDimension(List<List<SugiyamaNode<V>>> layers) {
		int maxX = 0;
		for (List<SugiyamaNode<V>> layer : layers) {
			SugiyamaNode<V> last = layer.get(layer.size()-1);
			maxX = Math.max(maxX, last.getPoint().x + last.getDimension().w);
		}
		int maxY = 0;
		for (SugiyamaNode<V> node : layers.get(layers.size()-1)) {
			maxY = Math.max(maxY, node.getPoint().y + node.getDimension().h);
		}
		return new LayoutDimension(maxX, maxY);
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
				node.setPoint(new LayoutPoint((int)node.getPosition() - minPosition - node.getDimension().w / 2, levelY));
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
	private void insertSegment(Digraph<SugiyamaNode<V>,SugiyamaArc<V,E>> graph, SugiyamaArc<V,E> arc, LayoutDimension zero) {
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
	protected void insertDummyNodes(Digraph<SugiyamaNode<V>,SugiyamaArc<V,E>> graph, LayoutDimensionProvider<V> dimensions) {
		List<SugiyamaArc<V,E>> longArcs = new LinkedList<SugiyamaArc<V,E>>();
		for (SugiyamaNode<V> source : graph.vertices()) {
			for (SugiyamaNode<V> target : graph.targets(source)) {
				if (target.getLayer() - source.getLayer() > 1) {
					longArcs.add(graph.get(source, target));
				}
			}
		}
		LayoutDimension zero = dimensions.getDimension(null);
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
						List<LayoutPoint> points = graph.get(source, target).getBendPoints();
						SugiyamaNode<V> current = target;
						SugiyamaNode<V> previous = null;
						do {
							if (current.getUpper().getPosition() != current.getPosition() || current.getLower().getPosition() != current.getPosition()) {
								if (points.isEmpty()) {
									points = new LinkedList<LayoutPoint>();
								}
								points.add(current.getPoint());
								List<LayoutPoint> additionalBends = graph.get(current, current.getLower()).getBendPoints();
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

	protected void finalizeLayout(Digraph<SugiyamaNode<V>,SugiyamaArc<V,E>> graph) {
		// do nothing
	}

	protected DoubledDigraph<SugiyamaNode<V>,SugiyamaArc<V,E>> createLayoutGraph(Digraph<V,E> graph, LayoutDimensionProvider<V> dimensions, Digraph<V,?> feedback) {
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
	public Layout<V,E> layout(Digraph<V,E> digraph, LayoutDimensionProvider<V> dimensions) {
		FeedbackArcSet<V,E> feedback =
			new SimpleFeedbackArcSetProvider().getFeedbackArcSet(digraph, EdgeWeights.UNIT_WEIGHTS, FeedbackArcSetPolicy.MIN_WEIGHT);
		return layout(digraph, dimensions, feedback);
	}

	public Layout<V,E> layout(Digraph<V,E> digraph, LayoutDimensionProvider<V> dimensions, Digraph<V,?> feedback) {
		if (digraph.getVertexCount() == 0) {
			return new Layout<V,E>(Digraphs.<LayoutNode<V>,LayoutArc<V,E>>emptyDigraph(), new LayoutDimension(0, 0));
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
		LayoutDimension dimension = computeLayoutDimension(layers);
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
					for (LayoutPoint point : arc.getBendPoints()) {
						point.transpose();
					}
				}
			}
			dimension.transpose();
		}
		return new Layout<V,E>(graph, dimension);
	}
}
