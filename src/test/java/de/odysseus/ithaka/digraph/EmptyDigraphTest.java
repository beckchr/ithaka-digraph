/*
 * Copyright 2008 Odysseus Software GmbH, Frankfurt am Main/Germany.
 */
package de.odysseus.ithaka.digraph;

import org.junit.Assert;
import org.junit.Test;

public class EmptyDigraphTest {

	@Test(expected=UnsupportedOperationException.class)
	public void testAdd() {
		new EmptyDigraph<Object,Object>().add("foo");
	}

	@Test
	public void testContainsObjectObject() {
		Assert.assertFalse(new EmptyDigraph<Object,Object>().contains("foo", "bar"));
	}

	@Test
	public void testContainsObject() {
		Assert.assertFalse(new EmptyDigraph<Object,Object>().contains("foo"));
	}

	@Test
	public void testGet() {
		Assert.assertNull(new EmptyDigraph<Object,Object>().get("foo", "bar"));
	}

	@Test
	public void testGetInDegree() {
		Assert.assertEquals(0, new EmptyDigraph<Object,Object>().getInDegree("foo"));
	}

	@Test
	public void testGetOutDegree() {
		Assert.assertEquals(0, new EmptyDigraph<Object,Object>().getOutDegree("foo"));
	}

	@Test
	public void testGetEdgeCount() {
		Assert.assertEquals(0, new EmptyDigraph<Object,Object>().getEdgeCount());
	}

	@Test
	public void testGetNodeCount() {
		Assert.assertEquals(0, new EmptyDigraph<Object,Object>().getVertexCount());
	}

	@Test
	public void testNodes() {
		Assert.assertFalse(new EmptyDigraph<Object,Object>().vertices().iterator().hasNext());
	}

	@Test(expected=UnsupportedOperationException.class)
	public void testPut() {
		new EmptyDigraph<Object,Object>().put("foo", "bar", "foobar");
	}

	@Test
	public void testRemoveObjectObject() {
		Assert.assertNull(new EmptyDigraph<Object,Object>().remove("foo", "bar"));
	}

	@Test
	public void testRemoveObject() {
		Assert.assertFalse(new EmptyDigraph<Object,Object>().remove("foo"));
	}

	@Test
	public void testReverse() {
		EmptyDigraph<Object,Object> g = new EmptyDigraph<Object,Object>();
		Assert.assertSame(g, g.reverse());
	}

	@Test
	public void testSubgraph() {
		EmptyDigraph<Object,Object> g = new EmptyDigraph<Object,Object>();
		Assert.assertSame(g, g.subgraph(null));
	}

	@Test
	public void testSources() {
		Assert.assertFalse(new EmptyDigraph<Object,Object>().sources("foo").iterator().hasNext());
	}

	@Test
	public void testTargets() {
		Assert.assertFalse(new EmptyDigraph<Object,Object>().targets("foo").iterator().hasNext());
	}

	@Test
	public void testIsAcyclic() {
		Assert.assertTrue(new EmptyDigraph<Object,Object>().isAcyclic());
	}

}
