/*
 * Copyright 2008 Odysseus Software GmbH, Frankfurt am Main/Germany.
 */
package de.odysseus.ithaka.digraph.layout.sugiyama;

import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import de.odysseus.ithaka.digraph.Digraph;
import de.odysseus.ithaka.digraph.layout.LayoutPoint;

/**
 * Route arcs
 */
public class SugiyamaStep4<V,E> {
	public void routeArcs(Digraph<SugiyamaNode<V>,SugiyamaArc<V,E>> graph, List<List<SugiyamaNode<V>>> layers) {
		computeArcSlots(graph);
		computeArcBends(graph, layers);
	}

	private void computeArcSlots(Digraph<SugiyamaNode<V>,SugiyamaArc<V,E>> graph) {
		List<SugiyamaArc<V,E>> arcs = new ArrayList<SugiyamaArc<V,E>>(graph.getEdgeCount());

		// collect arcs
		for (SugiyamaNode<V> source : graph.vertices()) {
			for (SugiyamaNode<V> target : graph.targets(source)) {
				arcs.add(graph.get(source, target));
			}
		}

		// sort arcs
		Collections.sort(arcs, new Comparator<SugiyamaArc<V,E>>() {
			@Override
			public int compare(SugiyamaArc<V,E> e1, SugiyamaArc<V,E> e2) {
				double key1 = e1.getSource().getIndex() + e1.getTarget().getIndex();
				double key2 = e2.getSource().getIndex() + e2.getTarget().getIndex();
				return key1 < key2 ? -1 : key1 > key2 ? 1 : 0;
			}
		});

		// assign slot numbers
		for (SugiyamaArc<V,E> arc : arcs) {
			arc.setSourceSlot(arc.getSource().nextLowerSlot());
			arc.setTargetSlot(arc.getTarget().nextUpperSlot());
			if ((int)arc.getSource().getPosition() == (int)arc.getTarget().getPosition()) { // assign center slot
				arc.getSource().setLowerCenterSlot(arc.getSourceSlot());
				arc.getTarget().setUpperCenterSlot(arc.getTargetSlot());
			}
			arc = arc.getBackArc();
			if (arc != null) {
				arc.setSourceSlot(arc.getSource().nextLowerSlot());
				arc.setTargetSlot(arc.getTarget().nextUpperSlot());
			}
		}
	}

	private void computeArcBends(final Digraph<SugiyamaNode<V>,SugiyamaArc<V,E>> graph, final List<List<SugiyamaNode<V>>> layers) {
		List<SugiyamaArc<V,E>> arcs = new ArrayList<SugiyamaArc<V,E>>(graph.getEdgeCount());

		// collect arcs
		for (SugiyamaNode<V> source : graph.vertices()) {
			for (SugiyamaNode<V> target : graph.targets(source)) {
				SugiyamaArc<V,E> arc = graph.get(source, target);
				arcs.add(arc);
				if (arc.getBackArc() != null) {
					arcs.add(arc.getBackArc());
				}
			}
		}

		// sort arcs: layers top down, left to right
		Collections.sort(arcs, new Comparator<SugiyamaArc<V,E>>() {
			@Override
			public int compare(SugiyamaArc<V,E> e1, SugiyamaArc<V,E> e2) {
				int i1 = e1.getSource().getLayer();
				int i2 = e2.getSource().getLayer();
				if (i1 < i2) {
					return -1;
				} else if (i1 > i2) {
					return 1;
				} else {
					int j1 = e1.getSource().getIndex();
					int j2 = e2.getSource().getIndex();
					if (j1 < j2) {
						return -1;
					} else if (j1 > j2) {
						return 1;
					} else {
						int k1 = e1.getSourceSlot();
						int k2 = e2.getSourceSlot();
						if (k1 < k2) {
							return -1;
						} else if (k1 > k2) {
							return 1;
						}
						return 0;
					}
				}
			}
		});

		List<SugiyamaNode<V>> layer = null;
		SugiyamaNode<V> last = null;
		Border border = null;

		for (boolean leftToRight : new boolean[] {true, false}) {
//			System.out.println("left to right: " + leftToRight);
			if (!leftToRight) {
				Collections.reverse(arcs);
				last = null;
			}
			for (SugiyamaArc<V,E> arc : arcs) {
				SugiyamaNode<V> source = arc.getSource();
				SugiyamaNode<V> target = arc.getTarget();
				LayoutPoint sourceSlotPoint = source.getLowerSlotPoint(arc.getSourceSlot());	// segment start
				LayoutPoint targetSlotPoint = target.getUpperSlotPoint(arc.getTargetSlot());	// segment end
				if (source != last) {													// first/last arc of source
					if (last == null || last.getLayer() != source.getLayer()) {			// first/last arc on layer
						layer = layers.get(source.getLayer());
						last = null;
						border = new Border(leftToRight);
					}
					if (leftToRight) {	// add nodes from last (incl.) to source (excl.) to border
						for (int index = last == null ? 0 : last.getIndex(); index < source.getIndex(); index++) {
							SugiyamaNode<V> node = layer.get(index);
							int borderPointX = node.getPoint().x + node.getDimension().w - 1;
							int borderPointY = node.getPoint().y + node.getDimension().h - 1;
							border.add(new LayoutPoint(borderPointX, borderPointY)); //, sourceSlotPoint, targetSlotPoint);
						}
					} else {
						for (int index = last == null ? layer.size() - 1 : last.getIndex(); index > source.getIndex(); index--) {
							SugiyamaNode<V> node = layer.get(index);
							int borderPointX = node.getPoint().x;
							int borderPointY = node.getPoint().y + node.getDimension().h - 1;
							border.add(new LayoutPoint(borderPointX, borderPointY)); //, sourceSlotPoint, targetSlotPoint);
						}
					}
				}
//				System.out.println("border=" + border + ", from " + sourceSlotPoint + " to " + targetSlotPoint);
				int bendY = border.getBendY(sourceSlotPoint, targetSlotPoint);
				if (bendY > sourceSlotPoint.y) {
//					System.out.println("bad: " + arc);
					LayoutPoint bend = new LayoutPoint(sourceSlotPoint.x, bendY);
					LinkedList<LayoutPoint> bends = new LinkedList<LayoutPoint>();
					bends.add(bend);
					arc.setBendPoints(bends);
					border.add(bend);
				}
				last = source;
			}
		}
	}

	private class Border {
		int aura = 4, sign;
		List<LayoutPoint> borderPoints = new ArrayList<LayoutPoint>();	// list of points with decreasing y values and decreasing slope (i.e. convex)

		Border(boolean leftToRight) {
			sign = leftToRight ? 1 : -1;
		}
		void add(LayoutPoint borderPoint) {
			int index = borderPoints.size();
			if (!borderPoints.isEmpty()) {
				LayoutPoint last = borderPoints.get(index - 1);
				if (sign * borderPoint.x < sign * last.x) {
					return;									// ignore (occurs when last was a bend)
				}
			}
			while (--index >= 0 && borderPoints.get(index).y <= borderPoint.y) {
				borderPoints.remove(index);					// remove trailing points with y values too small
			}
			while (--index > 0) {
				LayoutPoint current = borderPoints.get(index);
				LayoutPoint previous = borderPoints.get(index - 1);
				if (sign * Line2D.relativeCCW(previous.x, previous.y, current.x, current.y, borderPoint.x, borderPoint.y) <= 0) {
					borderPoints.remove(index);				// remove trailing points with slope values too small
				} else {
					break;
				}
			}
			borderPoints.add(borderPoint);
		}
		int getBendY(LayoutPoint sourceSlotPoint, LayoutPoint targetSlotPoint) {
			int bendY = sourceSlotPoint.y;
			if (sign * sourceSlotPoint.x > sign * targetSlotPoint.x) {
				for (LayoutPoint borderPoint : borderPoints) {
					if (borderPoint.y > bendY &&
							sign * borderPoint.x > sign * targetSlotPoint.x &&
							sign * Line2D.relativeCCW(sourceSlotPoint.x, sourceSlotPoint.y, targetSlotPoint.x, targetSlotPoint.y, borderPoint.x + sign * aura, borderPoint.y + aura) >= 0) {
						bendY = borderPoint.y;
					}
				}
			}
			return bendY;
		}
		@Override
		public String toString() {
			String result = "[ ";
			for (LayoutPoint borderPoint : borderPoints) {
				result += borderPoint + " ";
			}
			return result + "]";
		}
	}
}
