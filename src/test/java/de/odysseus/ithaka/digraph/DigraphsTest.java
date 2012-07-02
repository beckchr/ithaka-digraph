/*
 * Copyright 2008 Odysseus Software GmbH, Frankfurt am Main/Germany.
 */
package de.odysseus.ithaka.digraph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

public class DigraphsTest {

	@Test
	public void testEmptyDigraph() {
		Digraph<?,?> empty = Digraphs.emptyDigraph();
		Assert.assertEquals(0, empty.getEdgeCount());
		Assert.assertEquals(0, empty.getVertexCount());
		Assert.assertTrue(empty.isAcyclic());
	}

	@Test(expected=UnsupportedOperationException.class)
	public void testUnmodifiableDigraph() {
		Digraph<Integer,Object> digraph = Digraphs.unmodifiableDigraph(new MapDigraph<Integer,Object>());
		digraph.put(1, 2, 3);
	}

	@Test
	public void testTopsort() {
		SimpleDigraph<Integer> g = new SimpleDigraphAdapter<Integer>();
		g.add(1, 2);
		g.add(2, 3);
		g.add(3, 4);
		g.add(1, 3);
		g.add(2, 4);
		assert g.isAcyclic();

		int n = 0;
		int v = 0;
		for (int w : Digraphs.topsort(g, false)) {
			Assert.assertTrue(v < w);
			v = w;
			n++;
		}
		Assert.assertEquals(g.getVertexCount(), n);
	}

	@Test
	public void testClosure() {
		SimpleDigraph<Integer> g = new SimpleDigraphAdapter<Integer>();
		g.add(1, 2);
		g.add(2, 3);
		g.add(3, 2);
		g.add(3, 4);

		Set<Integer> c;

		c = Digraphs.closure(g, 1);
		Assert.assertEquals(4, c.size());

		c = Digraphs.closure(g, 2);
		Assert.assertEquals(3, c.size());
		Assert.assertFalse(c.contains(1));

		c = Digraphs.closure(g, 3);
		Assert.assertEquals(3, c.size());
		Assert.assertFalse(c.contains(1));

		c = Digraphs.closure(g, 4);
		Assert.assertEquals(1, c.size());
		Assert.assertTrue(c.contains(4));
	}

	@Test
	public void testIsAcyclic() {
		SimpleDigraph<Integer> g;

		g = new SimpleDigraphAdapter<Integer>();
		g.add(1, 2);
		g.add(2, 3);
		g.add(3, 4);
		g.add(1, 3);
		g.add(2, 4);
		Assert.assertTrue(Digraphs.isAcyclic(g));

		g = new SimpleDigraphAdapter<Integer>();
		g.add(1, 2);
		g.add(2, 3);
		g.add(3, 2);
		g.add(3, 4);
		Assert.assertFalse(Digraphs.isAcyclic(g));
	}

	@Test
	public void testIsStronglyConnected() {
		SimpleDigraph<Integer> g;

		g = new SimpleDigraphAdapter<Integer>();
		g.add(1, 2);
		g.add(2, 3);
		g.add(3, 4);
		g.add(1, 3);
		g.add(2, 4);
		Assert.assertFalse(Digraphs.isStronglyConnected(g));

		g = new SimpleDigraphAdapter<Integer>();
		g.add(1, 2);
		g.add(2, 3);
		g.add(3, 4);
		g.add(4, 2);
		g.add(3, 1);
		Assert.assertTrue(Digraphs.isStronglyConnected(g));
	}

	@Test
	public void testIsEquivalent() {
		WeightedDigraph<Integer> g1;
		WeightedDigraph<Integer> g2;

		g1 = new WeightedDigraphAdapter<Integer>();
		g1.add(0);
		g1.put(1, 2, 1);
		g1.put(2, 3, 2);
		g1.put(3, 4, 3);
		g1.put(1, 3, 4);
		g1.put(2, 4, 5);

		g2 = g1;

		Assert.assertTrue(Digraphs.isEquivalent(g1, g2, false));
		Assert.assertTrue(Digraphs.isEquivalent(g1, g2, true));

		g2 = new WeightedDigraphAdapter<Integer>();
		g2.add(0);
		g2.put(1, 2, 1);
		g2.put(2, 3, 2);
		g2.put(3, 4, 3);
		g2.put(1, 3, 4);
		g2.put(2, 4, 5);

		Assert.assertTrue(Digraphs.isEquivalent(g1, g2, false));
		Assert.assertTrue(Digraphs.isEquivalent(g2, g1, false));
		Assert.assertTrue(Digraphs.isEquivalent(g1, g2, true));
		Assert.assertTrue(Digraphs.isEquivalent(g2, g1, true));

		g2.remove(0);

		Assert.assertFalse(Digraphs.isEquivalent(g1, g2, false));
		Assert.assertFalse(Digraphs.isEquivalent(g2, g1, false));
		Assert.assertFalse(Digraphs.isEquivalent(g1, g2, true));
		Assert.assertFalse(Digraphs.isEquivalent(g2, g1, true));

		g2.add(0);

		Assert.assertTrue(Digraphs.isEquivalent(g1, g2, false));
		Assert.assertTrue(Digraphs.isEquivalent(g2, g1, false));
		Assert.assertTrue(Digraphs.isEquivalent(g1, g2, true));
		Assert.assertTrue(Digraphs.isEquivalent(g2, g1, true));

		g2.remove(2, 3);

		Assert.assertFalse(Digraphs.isEquivalent(g1, g2, false));
		Assert.assertFalse(Digraphs.isEquivalent(g2, g1, false));
		Assert.assertFalse(Digraphs.isEquivalent(g1, g2, true));
		Assert.assertFalse(Digraphs.isEquivalent(g2, g1, true));

		g2.put(2, 3, 2);

		Assert.assertTrue(Digraphs.isEquivalent(g1, g2, false));
		Assert.assertTrue(Digraphs.isEquivalent(g2, g1, false));
		Assert.assertTrue(Digraphs.isEquivalent(g1, g2, true));
		Assert.assertTrue(Digraphs.isEquivalent(g2, g1, true));

		g2.put(2, 3, 0);

		Assert.assertTrue(Digraphs.isEquivalent(g1, g2, false));
		Assert.assertTrue(Digraphs.isEquivalent(g2, g1, false));
		Assert.assertFalse(Digraphs.isEquivalent(g1, g2, true));
		Assert.assertFalse(Digraphs.isEquivalent(g2, g1, true));

		g2.put(2, 3, null);

		Assert.assertTrue(Digraphs.isEquivalent(g1, g2, false));
		Assert.assertTrue(Digraphs.isEquivalent(g2, g1, false));
		Assert.assertFalse(Digraphs.isEquivalent(g1, g2, true));
		Assert.assertFalse(Digraphs.isEquivalent(g2, g1, true));
	}

	@Test
	public void testIsReachable() {
		SimpleDigraph<Integer> g = new SimpleDigraphAdapter<Integer>();
		g.add(1, 2);
		g.add(2, 3);
		g.add(3, 2);
		g.add(3, 4);

		Assert.assertTrue(Digraphs.isReachable(g, 1, 1));
		Assert.assertTrue(Digraphs.isReachable(g, 1, 2));
		Assert.assertTrue(Digraphs.isReachable(g, 1, 3));
		Assert.assertTrue(Digraphs.isReachable(g, 1, 4));
		Assert.assertTrue(Digraphs.isReachable(g, 2, 2));
		Assert.assertTrue(Digraphs.isReachable(g, 2, 3));
		Assert.assertTrue(Digraphs.isReachable(g, 2, 4));
		Assert.assertTrue(Digraphs.isReachable(g, 3, 2));
		Assert.assertTrue(Digraphs.isReachable(g, 3, 3));
		Assert.assertTrue(Digraphs.isReachable(g, 3, 4));
		Assert.assertTrue(Digraphs.isReachable(g, 4, 4));

		Assert.assertFalse(Digraphs.isReachable(g, 2, 1));
		Assert.assertFalse(Digraphs.isReachable(g, 3, 1));
		Assert.assertFalse(Digraphs.isReachable(g, 4, 1));
		Assert.assertFalse(Digraphs.isReachable(g, 4, 2));
		Assert.assertFalse(Digraphs.isReachable(g, 4, 3));
	}

	@Test
	public void testDfs() {
		SimpleDigraph<Integer> g = new SimpleDigraphAdapter<Integer>();
		g.add(1, 2);
		g.add(2, 3);
		g.add(3, 2);
		g.add(3, 4);

		Set<Integer> discovered = new HashSet<Integer>();
		List<Integer> finished = new ArrayList<Integer>();
		Digraphs.dfs(g, 1, discovered, finished);

		Assert.assertTrue(discovered.size() == 4);
		Assert.assertTrue(finished.size() == 4);
		Assert.assertEquals(4, finished.get(0).intValue());
		Assert.assertEquals(3, finished.get(1).intValue());
		Assert.assertEquals(2, finished.get(2).intValue());
		Assert.assertEquals(1, finished.get(3).intValue());
	}

	@Test
	public void testDfs2() {
		SimpleDigraph<Integer> g = new SimpleDigraphAdapter<Integer>();
		g.add(2, 1);
		g.add(2, 3);
		g.add(3, 2);
		g.add(3, 4);

		Set<Integer> discovered = new HashSet<Integer>();
		List<Integer> finished = new ArrayList<Integer>();
		Digraphs.dfs2(g, 1, discovered, finished);

		Assert.assertTrue(discovered.size() == 4);
		Assert.assertTrue(finished.size() == 4);
		Assert.assertEquals(4, finished.get(0).intValue());
		Assert.assertEquals(3, finished.get(1).intValue());
		Assert.assertEquals(2, finished.get(2).intValue());
		Assert.assertEquals(1, finished.get(3).intValue());
	}

	@Test
	public void testScc() {
		SimpleDigraph<Integer> g = new SimpleDigraphAdapter<Integer>();
		g.add(1, 2);
		g.add(2, 1);
		g.add(1, 3);
		g.add(3, 4);
		g.add(4, 2);
		g.add(3, 5);

		List<Set<Integer>> components = Digraphs.scc(g);
		Assert.assertEquals(2, components.size());
		if (components.get(0).size() == 1) {
			Set<Integer> tmp = components.get(0);
			components.set(0, components.get(1));
			components.set(1, tmp);
		}
		Assert.assertEquals(4, components.get(0).size());
		Assert.assertEquals(1, components.get(1).size());
		Assert.assertTrue(components.get(1).contains(5));
	}

	@Test
	public void testWcc() {
		SimpleDigraph<Integer> g = new SimpleDigraphAdapter<Integer>();
		g.add(1, 2);
		g.add(1, 3);
		g.add(4, 2);
		g.add(5, 6);

		List<Set<Integer>> components = Digraphs.wcc(g);
		Assert.assertEquals(2, components.size());
		if (components.get(0).size() == 1) {
			Set<Integer> tmp = components.get(0);
			components.set(0, components.get(1));
			components.set(1, tmp);
		}
		Assert.assertEquals(4, components.get(0).size());
		Assert.assertEquals(2, components.get(1).size());
		Assert.assertTrue(components.get(1).contains(5));
		Assert.assertTrue(components.get(1).contains(6));
	}

	@Test
	public void testReverse() {
		SimpleDigraph<Integer> g = new SimpleDigraphAdapter<Integer>();
		g.add(1, 2);
		g.add(1, 3);
		g.add(4, 2);
		g.add(5, 6);
		g.add(7);

		SimpleDigraph<Integer> r = g.reverse();
		Assert.assertEquals(7, r.getVertexCount());
		Assert.assertEquals(4, r.getEdgeCount());
		Assert.assertTrue(r.contains(2, 1));
		Assert.assertTrue(r.contains(3, 1));
		Assert.assertTrue(r.contains(2, 4));
		Assert.assertTrue(r.contains(6, 5));
	}

	@Test
	public void testSubgraph() {
		SimpleDigraph<Integer> g = new SimpleDigraphAdapter<Integer>();
		g.add(1, 2);
		g.add(1, 3);
		g.add(4, 2);
		g.add(5, 6);
		g.add(7);

		Set<Integer> nodes = new HashSet<Integer>();
		nodes.add(1);
		nodes.add(2);
		nodes.add(3);
		nodes.add(7);
		SimpleDigraph<Integer> s = g.subgraph(nodes);

		Assert.assertEquals(4, s.getVertexCount());
		Assert.assertEquals(2, s.getEdgeCount());
		Assert.assertTrue(s.contains(1, 2));
		Assert.assertTrue(s.contains(1, 3));
	}

	@Test
	public void testPartition() {
		SimpleDigraph<Integer> g = new SimpleDigraphAdapter<Integer>();
		g.add(1, 2);
		g.add(2, 1);
		g.add(1, 3);
		g.add(1, 4);
		g.add(3, 4);
		g.add(4, 2);
		g.add(3, 5);

		List<Set<Integer>> sets = new ArrayList<Set<Integer>>();
		Set<Integer> set = new HashSet<Integer>();
		set.add(1);
		set.add(2);
		sets.add(set);
		set = new HashSet<Integer>();
		set.add(3);
		set.add(4);
		sets.add(set);
		set = new HashSet<Integer>();
		set.add(5);
		sets.add(set);

		DigraphFactory<WeightedDigraphAdapter<SimpleDigraph<Integer>>> f1 =
			WeightedDigraphAdapter.<SimpleDigraph<Integer>>getAdapterFactory(MapDigraph.<SimpleDigraph<Integer>,Integer>getDefaultDigraphFactory());
		DigraphFactory<SimpleDigraphAdapter<Integer>> f2 =
			SimpleDigraphAdapter.<Integer>getAdapterFactory(MapDigraph.<Integer,Boolean>getDefaultDigraphFactory());
		EdgeCumulator<SimpleDigraph<Integer>, Integer, Boolean> c =
			new EdgeCumulator<SimpleDigraph<Integer>, Integer, Boolean>() {
			@Override
			public Integer add(SimpleDigraph<Integer> s, SimpleDigraph<Integer> t, Integer c, Boolean e) {
				return c == null ? 1 : c + 1;
			}
		};
		WeightedDigraph<SimpleDigraph<Integer>> p =
			Digraphs.<Integer,Boolean,SimpleDigraph<Integer>,WeightedDigraph<SimpleDigraph<Integer>>,Integer>partition(g, sets, f1, f2, c);

		Assert.assertEquals(3, p.getVertexCount());
		Assert.assertEquals(3, p.getEdgeCount());
		SimpleDigraph<Integer> s1 = null, s2 = null, s3 = null;
		for (SimpleDigraph<Integer> s : p.vertices()) {
			if (s.contains(1) && s.contains(2)) {
				Assert.assertEquals(2, s.getVertexCount());
				Assert.assertEquals(2, s.getEdgeCount());
				Assert.assertTrue(s.contains(1, 2));
				Assert.assertTrue(s.contains(2, 1));
				s1 = s;
			}
			if (s.contains(3) && s.contains(4)) {
				Assert.assertEquals(2, s.getVertexCount());
				Assert.assertEquals(1, s.getEdgeCount());
				Assert.assertTrue(s.contains(3, 4));
				s2 = s;
			}
			if (s.contains(5)) {
				Assert.assertEquals(1, s.getVertexCount());
				Assert.assertEquals(0, s.getEdgeCount());
				s3 = s;
			}
		}
		Assert.assertTrue(s1 != s2 && s1 != s3 && s2 != s3 && s1 != null && s2 != null && s3 != null);
		Assert.assertEquals(2, p.get(s1, s2).intValue());
		Assert.assertEquals(1, p.get(s2, s1).intValue());
		Assert.assertEquals(1, p.get(s2, s3).intValue());
	}
}
