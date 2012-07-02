/*
 * Copyright 2008 Odysseus Software GmbH, Frankfurt am Main/Germany.
 */
package de.odysseus.ithaka.digraph;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

public class MapDigraphTest {
	private Comparator<String> CMP = new Comparator<String>() {
		@Override
		public int compare(String o1, String o2) {
			return o1.compareTo(o2);
		}
	};

	private Comparator<String> CMP2 = new Comparator<String>() {
		@Override
		public int compare(String o1, String o2) {
			return o2.compareTo(o1);
		}
	};

	@Test
	public void testGetDefaultDigraphFactory() {
		Assert.assertNotNull(MapDigraph.<String,Integer>getDefaultDigraphFactory().create());
	}

	@Test
	public void testMapDigraphComparatorOfQsuperV() {
		MapDigraph<String,Integer> digraph = new MapDigraph<String,Integer>(CMP);

		digraph.add("c");
		digraph.add("b");
		digraph.add("a");
		digraph.put("a", "c", 1);
		digraph.put("a", "b", 2);
		digraph.put("a", "a", 3);

		Iterator<String> nodes = digraph.vertices().iterator();
		Assert.assertEquals("a", nodes.next());
		Assert.assertEquals("b", nodes.next());
		Assert.assertEquals("c", nodes.next());

		Iterator<String> targets = digraph.targets("a").iterator();
		Assert.assertEquals("a", targets.next());
		Assert.assertEquals("b", targets.next());
		Assert.assertEquals("c", targets.next());
	}

	@Test
	public void testMapDigraphComparatorOfQsuperVComparatorOfQsuperV() {
		MapDigraph<String,Integer> digraph = new MapDigraph<String,Integer>(CMP, CMP2);

		digraph.add("c");
		digraph.add("b");
		digraph.add("a");
		digraph.put("a", "c", 1);
		digraph.put("a", "b", 2);
		digraph.put("a", "a", 3);

		Iterator<String> nodes = digraph.vertices().iterator();
		Assert.assertEquals("a", nodes.next());
		Assert.assertEquals("b", nodes.next());
		Assert.assertEquals("c", nodes.next());

		Iterator<String> targets = digraph.targets("a").iterator();
		Assert.assertEquals("c", targets.next());
		Assert.assertEquals("b", targets.next());
		Assert.assertEquals("a", targets.next());
	}

	@Test
	public void testMapDigraph() {
		MapDigraph<String,Integer> digraph = new MapDigraph<String,Integer>();

		digraph.add("c");
		digraph.add("b");
		digraph.add("a");
		digraph.put("a", "c", 1);
		digraph.put("a", "b", 2);
		digraph.put("a", "a", 3);

		Iterator<String> nodes = digraph.vertices().iterator();
		Assert.assertEquals("c", nodes.next());
		Assert.assertEquals("b", nodes.next());
		Assert.assertEquals("a", nodes.next());

		Iterator<String> targets = digraph.targets("a").iterator();
		Assert.assertEquals("c", targets.next());
		Assert.assertEquals("b", targets.next());
		Assert.assertEquals("a", targets.next());
	}

	@Test
	public void testAdd() {
		MapDigraph<String,Integer> digraph = new MapDigraph<String,Integer>();

		Assert.assertTrue(digraph.add("foo"));
		Assert.assertEquals(1, digraph.getVertexCount());
		Assert.assertTrue(digraph.contains("foo"));

		Assert.assertFalse(digraph.add("foo"));
		Assert.assertEquals(1, digraph.getVertexCount());
		Assert.assertTrue(digraph.contains("foo"));

		Assert.assertTrue(digraph.add(null));
		Assert.assertEquals(2, digraph.getVertexCount());
		Assert.assertTrue(digraph.contains(null));
	}

	@Test
	public void testPut() {
		MapDigraph<String,Integer> digraph = new MapDigraph<String,Integer>();

		Assert.assertNull(digraph.put("foo", "bar", 1));
		Assert.assertTrue(digraph.contains("foo", "bar"));
		Assert.assertTrue(digraph.contains("foo"));
		Assert.assertTrue(digraph.contains("bar"));

		Assert.assertEquals(1, digraph.put("foo", "bar", 2).intValue());
		Assert.assertTrue(digraph.contains("foo", "bar"));

		Assert.assertEquals(2, digraph.put("foo", "bar", null).intValue());
		Assert.assertTrue(digraph.contains("foo", "bar"));

		Assert.assertNull(digraph.put(null, null, 3));
		Assert.assertTrue(digraph.contains(null));
		Assert.assertTrue(digraph.contains(null, null));

		Assert.assertNull(digraph.put("foo", "foo", 3));
		Assert.assertTrue(digraph.contains("foo", "foo"));
	}

	@Test
	public void testGet() {
		MapDigraph<String,Integer> digraph = new MapDigraph<String,Integer>();

		digraph.put("foo", "bar", 1);
		Assert.assertEquals(1, digraph.get("foo", "bar").intValue());

		digraph.put("foo", "bar", 2);
		Assert.assertEquals(2, digraph.get("foo", "bar").intValue());

		digraph.put("foo", "bar", null);
		Assert.assertNull(digraph.get("foo", "bar"));

		digraph.put(null, null, 3);
		Assert.assertEquals(3, digraph.get(null, null).intValue());

		Assert.assertNull(digraph.get("bar", "foo"));
	}

	@Test
	public void testRemoveVV() {
		MapDigraph<String,Integer> digraph = new MapDigraph<String,Integer>();

		digraph.put("foo", "bar", 1);
		Assert.assertEquals(1, digraph.remove("foo", "bar").intValue());
		Assert.assertFalse(digraph.contains("foo", "bar"));

		digraph.put("foo", "bar", null);
		Assert.assertNull(digraph.remove("foo", "bar"));
		Assert.assertFalse(digraph.contains("foo", "bar"));

		digraph.put("foo", "foo", 1);
		Assert.assertEquals(1, digraph.remove("foo", "foo").intValue());
		Assert.assertFalse(digraph.contains(null, null));

		digraph.put(null, null, 1);
		Assert.assertEquals(1, digraph.remove(null, null).intValue());
		Assert.assertFalse(digraph.contains(null, null));

		Assert.assertNull(digraph.remove("bar", "foo"));
	}

	@Test
	public void testRemoveV() {
		MapDigraph<String,Integer> digraph = new MapDigraph<String,Integer>();

		digraph.add("foo");
		Assert.assertTrue(digraph.remove("foo"));
		Assert.assertFalse(digraph.contains("foo"));

		digraph.add(null);
		Assert.assertTrue(digraph.remove(null));
		Assert.assertFalse(digraph.contains(null));
	}

	@Test
	public void testRemoveV2() {
		MapDigraph<String,Integer> digraph = new MapDigraph<String,Integer>();

		digraph.put("a", "a", 1);
		digraph.put("a", "b", 2);
		digraph.put("a", "c", 3);
		digraph.put("b", "a", 4);

		Assert.assertTrue(digraph.remove("c"));
		Assert.assertEquals(3, digraph.getEdgeCount());
		Assert.assertEquals(2, digraph.getVertexCount());

		Assert.assertTrue(digraph.remove("b"));
		Assert.assertEquals(1, digraph.getEdgeCount());
		Assert.assertEquals(1, digraph.getVertexCount());

		Assert.assertTrue(digraph.remove("a"));
		Assert.assertEquals(0, digraph.getEdgeCount());
		Assert.assertEquals(0, digraph.getVertexCount());
	}

	@Test
	public void testRemoveAll() {
		MapDigraph<String,Integer> digraph = new MapDigraph<String,Integer>();

		digraph.put("a", "a", 1);
		digraph.put("a", "b", 2);
		digraph.put("a", "c", 3);
		digraph.put("b", "a", 4);

		HashSet<String> set = new HashSet<String>();
		set.add("a");
		set.add("b");
		digraph.removeAll(set);

		Assert.assertFalse(digraph.contains("a"));
		Assert.assertFalse(digraph.contains("b"));
		Assert.assertTrue(digraph.contains("c"));
		Assert.assertEquals(1, digraph.getVertexCount());
		Assert.assertEquals(0, digraph.getEdgeCount());
	}

	@Test
	public void testContainsObjectObject() {
		MapDigraph<String,Integer> digraph = new MapDigraph<String,Integer>();

		digraph.add("foo");
		Assert.assertFalse(digraph.contains("foo", "foo"));
		digraph.add("bar");
		Assert.assertFalse(digraph.contains("foo", "bar"));
		digraph.put("foo", "bar", 1);
		Assert.assertTrue(digraph.contains("foo", "bar"));
		Assert.assertFalse(digraph.contains("bar", "foo"));
		digraph.remove("foo", "bar");
		Assert.assertFalse(digraph.contains("foo", "bar"));
	}

	@Test
	public void testContainsObject() {
		MapDigraph<String,Integer> digraph = new MapDigraph<String,Integer>();

		Assert.assertFalse(digraph.contains("foo"));
		digraph.add("foo");
		Assert.assertTrue(digraph.contains("foo"));
		digraph.remove("foo");
		Assert.assertFalse(digraph.contains("foo"));
	}

	@Test
	public void testvertices() {
		MapDigraph<String,Integer> digraph = new MapDigraph<String,Integer>();
		digraph.put("foo", "bar", 1);
		digraph.put("bar", "foo", 2);
		digraph.put("bar", "foobar", 3);
		digraph.add("baz");
		Iterator<String> nodes = digraph.vertices().iterator();
		int nodeCount = 4;
		while (nodes.hasNext()) {
			nodes.next();
			nodes.remove();
			Assert.assertEquals(--nodeCount, digraph.getVertexCount());
		}
		Assert.assertEquals(0, digraph.getEdgeCount());
	}

	@Test
	public void testTargets() {
		MapDigraph<String,Integer> digraph = new MapDigraph<String,Integer>();
		digraph.put("foo", "bar", 1);
		digraph.put("bar", "foo", 2);
		digraph.put("bar", "foobar", 3);
		digraph.add("baz");
		Iterator<String> targets = digraph.targets("bar").iterator();
		Assert.assertEquals("foo", targets.next());
		targets.remove();
		Assert.assertFalse(digraph.contains("bar", "foo"));
		Assert.assertEquals(2, digraph.getEdgeCount());
		Assert.assertEquals("foobar", targets.next());
		targets.remove();
		Assert.assertFalse(digraph.contains("bar", "foobar"));
		Assert.assertEquals(1, digraph.getEdgeCount());
		Assert.assertFalse(targets.hasNext());
		Assert.assertEquals(4, digraph.getVertexCount());
	}

	@Test
	public void testTargets2() {
		MapDigraph<String,Integer> digraph = new MapDigraph<String,Integer>();
		digraph.put("foo", "bar", 1);
		digraph.put("bar", "foo", 2);
		digraph.put("bar", "foobar", 3);
		digraph.add("baz");

		Iterator<String> sources = digraph.vertices().iterator();
		int edgeCount = 3;
		while (sources.hasNext()) {
			Iterator<String> targets = digraph.targets(sources.next()).iterator();
			while (targets.hasNext()) {
				targets.next();
				targets.remove();
				Assert.assertEquals(--edgeCount, digraph.getEdgeCount());
			}
		}
		Assert.assertEquals(0, digraph.getEdgeCount());
		Assert.assertEquals(4, digraph.getVertexCount());
	}

	@Test
	public void testgetVertexCount() {
		MapDigraph<String,Integer> digraph = new MapDigraph<String,Integer>();
		Assert.assertEquals(0, digraph.getVertexCount());

		digraph.add("foo");
		Assert.assertEquals(1, digraph.getVertexCount());

		digraph.put("foo", "bar", 1);
		Assert.assertEquals(2, digraph.getVertexCount());

		digraph.remove("foo");
		Assert.assertEquals(1, digraph.getVertexCount());

		digraph.remove("bar");
		Assert.assertEquals(0, digraph.getVertexCount());
	}

	@Test
	public void testGetOutDegree() {
		MapDigraph<String,Integer> digraph = new MapDigraph<String,Integer>();
		Assert.assertEquals(0, digraph.getOutDegree("a"));

		digraph.put("a", "a", 1);
		digraph.put("a", "b", 2);
		digraph.put("a", "c", 3);
		digraph.put("b", "a", 4);

		Assert.assertEquals(3, digraph.getOutDegree("a"));
		Assert.assertEquals(1, digraph.getOutDegree("b"));
		Assert.assertEquals(0, digraph.getOutDegree("c"));
	}

	@Test
	public void testGetEdgeCount() {
		MapDigraph<String,Integer> digraph = new MapDigraph<String,Integer>();
		Assert.assertEquals(0, digraph.getEdgeCount());

		digraph.put("a", "a", 1);
		digraph.put("a", "b", 2);
		digraph.put("a", "c", 3);
		digraph.put("b", "a", 4);
		Assert.assertEquals(4, digraph.getEdgeCount());
	}

	@Test
	public void testGetDigraphFactory() {
		Assert.assertNotNull(new MapDigraph<String,Integer>().getDigraphFactory());
	}

	@Test
	public void testReverse() {
		MapDigraph<Integer,Object> g = new MapDigraph<Integer,Object>();
		g.put(1, 2, null);
		g.put(1, 3, null);
		g.put(4, 2, null);
		g.put(5, 6, null);
		g.add(7);

		MapDigraph<Integer,Object> r = g.reverse();
		Assert.assertEquals(7, r.getVertexCount());
		Assert.assertEquals(4, r.getEdgeCount());
		Assert.assertTrue(r.contains(2, 1));
		Assert.assertTrue(r.contains(3, 1));
		Assert.assertTrue(r.contains(2, 4));
		Assert.assertTrue(r.contains(6, 5));
	}

	@Test
	public void testSubgraph() {
		MapDigraph<Integer,Object> g = new MapDigraph<Integer,Object>();
		g.put(1, 2, null);
		g.put(1, 3, null);
		g.put(4, 2, null);
		g.put(5, 6, null);
		g.add(7);

		Set<Integer> nodes = new HashSet<Integer>();
		nodes.add(1);
		nodes.add(2);
		nodes.add(3);
		nodes.add(7);
		MapDigraph<Integer,Object> s = g.subgraph(nodes);

		Assert.assertEquals(4, s.getVertexCount());
		Assert.assertEquals(2, s.getEdgeCount());
		Assert.assertTrue(s.contains(1, 2));
		Assert.assertTrue(s.contains(1, 3));
	}

	@Test
	public void testIsAcyclic() {
		MapDigraph<Integer,Object> g = new MapDigraph<Integer,Object>();
		g.put(1, 2, null);
		g.put(2, 3, null);
		g.put(3, 4, null);
		g.put(1, 3, null);
		g.put(2, 4, null);
		Assert.assertTrue(Digraphs.isAcyclic(g));

		g = new MapDigraph<Integer,Object>();
		g.put(1, 2, null);
		g.put(2, 3, null);
		g.put(3, 2, null);
		g.put(3, 4, null);
		Assert.assertFalse(Digraphs.isAcyclic(g));
	}
}
