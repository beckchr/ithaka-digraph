/*
 * Copyright 2008 Odysseus Software GmbH, Frankfurt am Main/Germany.
 */
package de.odysseus.ithaka.digraph.layout.sugiyama;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.odysseus.ithaka.digraph.Digraph;
import de.odysseus.ithaka.digraph.DigraphFactory;
import de.odysseus.ithaka.digraph.DoubledDigraph;
import de.odysseus.ithaka.digraph.DoubledDigraphAdapter;
import de.odysseus.ithaka.digraph.MapDigraph;

/**
 * Sugiyama 3: adjust node positions on layers.
 * Brandes/Koepf: "Fast and Simple Horizontal Coordinate Assignment" (Symposium on Graph Drawing, 2001)
 */
public class SugiyamaStep3<V> {
	static final int LEFTMOST_UPPER = 0;
	static final int RIGHTMOST_UPPER = 1;
	static final int LEFTMOST_LOWER = 2;
	static final int RIGHTMOST_LOWER = 3;

	static class BrandesKoepfNode {
		int id;
		Object data; // debug
		BrandesKoepfNode innerSegmentSource;
		BrandesKoepfNode root;
		BrandesKoepfNode align;
		BrandesKoepfNode sink, sink2;
		BrandesKoepfNode pred, succ;
		int shift;
		int pos;
		int delta;
		int x;
		int[] result = new int[4];
		@Override
		public String toString() {
			return data == null ? "<>" : data.toString();
		}
	}

	public static final Comparator<BrandesKoepfNode> CMP_ID = new Comparator<BrandesKoepfNode>() {
		@Override
		public int compare(BrandesKoepfNode o1, BrandesKoepfNode o2) {
			return o1.id < o2.id ? -1 : o1.id > o2.id ? 1 : 0;
		}
	};

	public static final Comparator<BrandesKoepfNode> CMP_POS = new Comparator<BrandesKoepfNode>() {
		@Override
		public int compare(BrandesKoepfNode o1, BrandesKoepfNode o2) {
			return o1.pos < o2.pos ? -1 : o1.pos > o2.pos ? 1 : 0;
		}
	};

	static DigraphFactory<Digraph<BrandesKoepfNode,Boolean>> FACTORY = new DigraphFactory<Digraph<BrandesKoepfNode,Boolean>>() {
		@Override
		public Digraph<BrandesKoepfNode,Boolean> create() {
			return new MapDigraph<BrandesKoepfNode,Boolean>(CMP_ID, CMP_POS);
		}
	};

	private int delta;

	public SugiyamaStep3(int delta) {
		this.delta = delta;
	}

	public void adjustNodePositions(Digraph<SugiyamaNode<V>,?> graph, List<List<SugiyamaNode<V>>> layers) {
		DoubledDigraph<BrandesKoepfNode,Boolean> brandesKoepfGraph =
			DoubledDigraphAdapter.getAdapterFactory(FACTORY).create();
		Map<SugiyamaNode<V>,BrandesKoepfNode> map = new HashMap<SugiyamaNode<V>,BrandesKoepfNode>();
		int id = 0;
		for (SugiyamaNode<V> node : graph.vertices()) {
			BrandesKoepfNode v = new BrandesKoepfNode();
			v.id = ++id;
			v.data = node.getVertex();
			v.pos = node.getIndex();
			v.delta = node.getDimension().w / 2;
			map.put(node, v);
			brandesKoepfGraph.add(v);
		}
		for (SugiyamaNode<V> source : graph.vertices()) {
			for (SugiyamaNode<V> target : graph.targets(source)) {
				BrandesKoepfNode v = map.get(source);
				BrandesKoepfNode w = map.get(target);
				if (source.isDummy() && target.isDummy()) {
					w.innerSegmentSource = v;
				}
				brandesKoepfGraph.put(v, w, Boolean.FALSE);
			}
		}
		List<List<BrandesKoepfNode>> brandesKoepfLayers = new ArrayList<List<BrandesKoepfNode>>(layers.size());
		for (List<SugiyamaNode<V>> layer : layers) {
			List<BrandesKoepfNode> brandesKoepfLayer = new ArrayList<BrandesKoepfNode>(layer.size());
			BrandesKoepfNode pred = null;
			for (SugiyamaNode<V> node : layer) {
				BrandesKoepfNode brandesKoepfNode = map.get(node);
				if (pred != null) {
					brandesKoepfNode.pred = pred;
					pred.succ = brandesKoepfNode;
				}
				brandesKoepfLayer.add(brandesKoepfNode);
				pred = brandesKoepfNode;
			}
			brandesKoepfLayers.add(brandesKoepfLayer);
		}
		runAlgorithm(brandesKoepfGraph, brandesKoepfLayers);
		int min = Integer.MAX_VALUE;
		for (BrandesKoepfNode v : brandesKoepfGraph.vertices()) {
			min = Math.min(min, v.x - v.delta);
		}
		for (SugiyamaNode<V> node : graph.vertices()) {
			node.setPosition(map.get(node).x - min);
		}
	}

	private void runAlgorithm(Digraph<BrandesKoepfNode,Boolean> graph, List<List<BrandesKoepfNode>> layers) {
		preprocessing(graph, layers);
		int[][] ranges = new int[4][];
		ranges[LEFTMOST_UPPER] = computeAssignment(graph, layers, LEFTMOST_UPPER);
		ranges[RIGHTMOST_UPPER] = computeAssignment(graph, layers, RIGHTMOST_UPPER);
		ranges[LEFTMOST_LOWER] = computeAssignment(graph, layers, LEFTMOST_LOWER);
		ranges[RIGHTMOST_LOWER] = computeAssignment(graph, layers, RIGHTMOST_LOWER);
		balance(graph, ranges);
	}

	private void shift(Digraph<BrandesKoepfNode,Boolean> graph, int assignment, int value) {
		if (value != 0) {
			for (BrandesKoepfNode v : graph.vertices()) {
				v.result[assignment] += value;
			}
		}
	}

	private void balance(Digraph<BrandesKoepfNode,Boolean> graph, int[][] ranges) {
		int minWidth = Integer.MAX_VALUE;
		int minWidthAssignment = -1;
		if (ranges[LEFTMOST_UPPER][1] - ranges[LEFTMOST_UPPER][0] < minWidth) {
			minWidth = ranges[LEFTMOST_UPPER][1] - ranges[LEFTMOST_UPPER][0];
			minWidthAssignment = LEFTMOST_UPPER;
		}
		if (ranges[RIGHTMOST_UPPER][1] - ranges[RIGHTMOST_UPPER][0] < minWidth) {
			minWidth = ranges[RIGHTMOST_UPPER][1] - ranges[RIGHTMOST_UPPER][0];
			minWidthAssignment = RIGHTMOST_UPPER;
		}
		if (ranges[LEFTMOST_LOWER][1] - ranges[LEFTMOST_LOWER][0] < minWidth) {
			minWidth = ranges[LEFTMOST_LOWER][1] - ranges[LEFTMOST_LOWER][0];
			minWidthAssignment = LEFTMOST_LOWER;
		}
		if (ranges[RIGHTMOST_LOWER][1] - ranges[RIGHTMOST_LOWER][0] < minWidth) {
			minWidth = ranges[RIGHTMOST_LOWER][1] - ranges[RIGHTMOST_LOWER][0];
			minWidthAssignment = RIGHTMOST_LOWER;
		}
		shift(graph, LEFTMOST_UPPER, ranges[minWidthAssignment][0] - ranges[LEFTMOST_UPPER][0]);
		shift(graph, LEFTMOST_LOWER, ranges[minWidthAssignment][0] - ranges[LEFTMOST_LOWER][0]);
		shift(graph, RIGHTMOST_UPPER, ranges[minWidthAssignment][1] - ranges[RIGHTMOST_UPPER][1]);
		shift(graph, RIGHTMOST_LOWER, ranges[minWidthAssignment][1] - ranges[RIGHTMOST_LOWER][1]);

		for (BrandesKoepfNode v : graph.vertices()) {
			Arrays.sort(v.result);
			v.x = (v.result[1] + v.result[2]) / 2;
		}
	}

	private int[] computeAssignment(Digraph<BrandesKoepfNode,Boolean> graph, List<List<BrandesKoepfNode>> layers, int mode) {
		verticalAlignment(graph, layers, mode);
		horizontalCompaction(graph, mode);
		int min = Integer.MAX_VALUE;
		int max = Integer.MIN_VALUE;
		for (BrandesKoepfNode v : graph.vertices()) {
			v.result[mode] = v.x;
			if (v.x > max) {
				max = v.x;
			}
			if (v.x < min) {
				min = v.x;
			}
		}
		return new int[]{min, max};
	}

	private void preprocessing(Digraph<BrandesKoepfNode,Boolean> graph, List<List<BrandesKoepfNode>> layers) {
		Digraph<BrandesKoepfNode,Boolean> reverse = graph.reverse();
		for (int i = 1; i < layers.size() - 2; i++) {
			List<BrandesKoepfNode> layer = layers.get(i+1);
			int k0 = 0;
			int l = 0;
			for (BrandesKoepfNode v : layer) {
				int l1 = v.pos;
				if (l1 == layer.size() - 1 || v.innerSegmentSource != null) {
					int k1 = layers.get(i).size() - 1;
					if (v.innerSegmentSource != null) {
						k1 = v.innerSegmentSource.pos;
					}
					while (l <= l1) {
						for (BrandesKoepfNode u : reverse.targets(layer.get(l))) {
							int k = u.pos;
							if (k < k0 || k > k1) {
								reverse.put(layer.get(l), u, Boolean.TRUE);
							}
						}
						l++;
					}
					k0 = k1;
				}
			}
		}
	}

	private void verticalAlignment(Digraph<BrandesKoepfNode,Boolean> graph, List<List<BrandesKoepfNode>> layers, int mode) {
		for (BrandesKoepfNode v : graph.vertices()) {
			v.root = v;
			v.align = v;
		}
		switch (mode) {
		case LEFTMOST_UPPER:
			for (int i = 1; i < layers.size(); i++) {
				verticalAlignmentLeft(graph.reverse(), layers.get(i));
			}
			break;
		case RIGHTMOST_UPPER:
			for (int i = 1; i < layers.size(); i++) {
				verticalAlignmentRight(graph.reverse(), layers.get(i));
			}
			break;
		case LEFTMOST_LOWER:
			for (int i = layers.size() - 2; i >= 0; i--) {
				verticalAlignmentLeft(graph, layers.get(i));
			}
			break;
		case RIGHTMOST_LOWER:
			for (int i = layers.size() - 2; i >= 0; i--) {
				verticalAlignmentRight(graph, layers.get(i));
			}
			break;
		}
	}

	private void verticalAlignmentLeft(Digraph<BrandesKoepfNode,Boolean> graph, List<BrandesKoepfNode> layer) {
		int r = Integer.MIN_VALUE;
		for (BrandesKoepfNode v : layer) {
			int d = graph.getOutDegree(v);
			if (d > 0) {
				int m = 0;
				BrandesKoepfNode u1 = null;
				for (BrandesKoepfNode u : graph.targets(v)) { // sorted
					if (m == (d-1)/2) {
						u1 = u;
					}
					if (m == d/2) {
						if (!graph.get(v, u1) && u1.pos > r) {
							u1.align = v;
							v.root = u1.root;
							v.align = v.root;
							r = u1.pos;
						} else if (u != u1 && !graph.get(v, u) && u.pos > r) {
							u.align = v;
							v.root = u.root;
							v.align = v.root;
							r = u.pos;
						}
						break;
					}
					m++;
				}
			}
		}
	}

	private void verticalAlignmentRight(Digraph<BrandesKoepfNode,Boolean> graph, List<BrandesKoepfNode> layer) {
		int r = Integer.MAX_VALUE;
		for (int k = layer.size() - 1; k >= 0; k--) {
			BrandesKoepfNode v = layer.get(k);
			int d = graph.getOutDegree(v);
			if (d > 0) {
				int m = 0;
				BrandesKoepfNode u1 = null;
				for (BrandesKoepfNode u : graph.targets(v)) { // sorted
					if (m == (d-1)/2) {
						u1 = u;
					}
					if (m == d/2) {
						if (!graph.get(v, u) && u.pos < r) {
							u.align = v;
							v.root = u.root;
							v.align = v.root;
							r = u.pos;
						} else if (u1 != u && !graph.get(v, u1) && u1.pos < r) {
							u1.align = v;
							v.root = u1.root;
							v.align = v.root;
							r = u1.pos;
						}
						break;
					}
					m++;
				}
			}
		}
	}

	private void horizontalCompaction(Digraph<BrandesKoepfNode,Boolean> graph, int mode) {
		switch (mode) {
		case LEFTMOST_LOWER:
		case LEFTMOST_UPPER:
			horizontalCompactionLeft(graph);
			break;
		case RIGHTMOST_LOWER:
		case RIGHTMOST_UPPER:
			horizontalCompactionRight(graph);
			break;
		}
	}

	private void placeBlockLeft(BrandesKoepfNode v) {
		if (v.x == Integer.MIN_VALUE) {
			v.x = 0;
			BrandesKoepfNode w = v;
			do {
				if (w.pred != null) {
					BrandesKoepfNode u = w.pred.root;
					placeBlockLeft(u);
					if (v.sink == v) {
						v.sink = u.sink;
					}
					int delta = this.delta + w.pred.delta + w.delta;
					if (v.sink != u.sink) {
						u.sink.sink2 = v.sink; // TODO Why was this line commented out in PREVIEW < 4?
						u.sink.shift = Math.min(u.sink.shift, v.x - u.x - delta);
					} else {
						v.x = Math.max(v.x, u.x + delta);
					}
				}
				w = w.align;
			} while (w != v);
		}
	}

	private void horizontalCompactionLeft(Digraph<BrandesKoepfNode,Boolean> graph) {
		for (BrandesKoepfNode v : graph.vertices()) {
			v.sink = v;
			v.sink2 = v;
			v.shift = Integer.MAX_VALUE;
			v.x = Integer.MIN_VALUE; // undefined
		}
		for (BrandesKoepfNode v : graph.vertices()) {
			if (v.root == v) {
				placeBlockLeft(v);
			}
		}
		for (BrandesKoepfNode v : graph.vertices()) {
			if (v.root == v) {
				BrandesKoepfNode sink = v.sink;
				do {
					if (sink.shift < Integer.MAX_VALUE) {
						v.x += sink.shift;
					}
					sink = sink.sink2;
				} while (sink.sink2 != sink);
			}
		}
		for (BrandesKoepfNode v : graph.vertices()) {
			v.x = v.root.x;
		}
	}

	private void placeBlockRight(BrandesKoepfNode v) {
		if (v.x == Integer.MIN_VALUE) {
			v.x = 0;
			BrandesKoepfNode w = v;
			do {
				if (w.succ != null) {
					BrandesKoepfNode u = w.succ.root;
					placeBlockRight(u);
					if (v.sink == v) {
						v.sink = u.sink;
					}
					int delta = this.delta + w.succ.delta + w.delta;
					if (v.sink != u.sink) {
						u.sink.sink2 = v.sink;
						u.sink.shift = Math.min(u.sink.shift, u.x - v.x - delta);
					} else {
						v.x = Math.min(v.x, u.x - delta);
					}
				}
				w = w.align;
			} while (w != v);
		}
	}

	private void horizontalCompactionRight(Digraph<BrandesKoepfNode,Boolean> graph) {
		for (BrandesKoepfNode v : graph.vertices()) {
			v.sink = v;
			v.sink2 = v;
			v.shift = Integer.MAX_VALUE;
			v.x = Integer.MIN_VALUE; // undefined
		}
		for (BrandesKoepfNode v : graph.vertices()) {
			if (v.root == v) {
				placeBlockRight(v);
			}
		}
		for (BrandesKoepfNode v : graph.vertices()) {
			if (v.root == v) {
				BrandesKoepfNode sink = v.sink;
				do {
					if (sink.shift < Integer.MAX_VALUE) {
						v.x -= sink.shift;
					}
					sink = sink.sink2;
				} while (sink.sink2 != sink);
			}
		}
		for (BrandesKoepfNode v : graph.vertices()) {
			v.x = v.root.x;
		}
	}
}
