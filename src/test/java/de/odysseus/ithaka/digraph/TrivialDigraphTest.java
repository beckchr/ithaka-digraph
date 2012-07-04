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
package de.odysseus.ithaka.digraph;

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

public class TrivialDigraphTest {

	@Test
	public void testAdd() {
		Assert.assertTrue(new TrivialDigraph<Object,Object>().add("foo"));
	}

	@Test(expected=UnsupportedOperationException.class)
	public void testAdd2() {
		TrivialDigraph<Object,Object> g = new TrivialDigraph<Object,Object>();
		g.add("foo");
		g.add("bar");
	}

	@Test(expected=IllegalArgumentException.class)
	public void testAdd3() {
		new TrivialDigraph<Object,Object>().add(null);
	}

	@Test
	public void testContainsObjectObject() {
		Assert.assertFalse(new TrivialDigraph<Object,Object>().contains("foo", "bar"));
	}

	@Test
	public void testContainsObject() {
		TrivialDigraph<Object,Object> g = new TrivialDigraph<Object,Object>();
		Assert.assertFalse(g.contains("foo"));
		g.add("foo");
		Assert.assertTrue(g.contains("foo"));
	}

	@Test
	public void testGet() {
		Assert.assertNull(new TrivialDigraph<Object,Object>().get("foo", "bar"));
	}

	@Test
	public void testGetInDegree() {
		Assert.assertEquals(0, new TrivialDigraph<Object,Object>().getInDegree("foo"));
	}

	@Test
	public void testGetOutDegree() {
		Assert.assertEquals(0, new TrivialDigraph<Object,Object>().getOutDegree("foo"));
	}

	@Test
	public void testGetEdgeCount() {
		Assert.assertEquals(0, new TrivialDigraph<Object,Object>().getEdgeCount());
	}

	@Test
	public void testgetVertexCount() {
		TrivialDigraph<Object,Object> g = new TrivialDigraph<Object,Object>();
		Assert.assertEquals(0, g.getVertexCount());
		g.add("foo");
		Assert.assertEquals(1, g.getVertexCount());
	}

	@Test
	public void testNodes() {
		TrivialDigraph<Object,Object> g = new TrivialDigraph<Object,Object>();
		Assert.assertFalse(g.vertices().iterator().hasNext());
		g.add("foo");
		Assert.assertTrue(g.vertices().iterator().hasNext());
		Assert.assertEquals("foo", g.vertices().iterator().next());
	}

	@Test(expected=UnsupportedOperationException.class)
	public void testPut() {
		new TrivialDigraph<Object,Object>().put("foo", "bar", "foobar");
	}

	@Test
	public void testRemoveVV() {
		Assert.assertNull(new TrivialDigraph<Object,Object>().remove("foo", "bar"));
	}

	@Test
	public void testRemoveV() {
		TrivialDigraph<Object,Object> g = new TrivialDigraph<Object,Object>();
		Assert.assertFalse(g.remove("foo"));
		g.add("foo");
		Assert.assertTrue(g.remove("foo"));
		Assert.assertEquals(0, g.getVertexCount());
	}

	@Test
	public void testRemoveAll() {
		TrivialDigraph<Object,Object> g = new TrivialDigraph<Object,Object>();
		g.add("foo");
		HashSet<Object> set = new HashSet<Object>();
		set.add("bar");
		g.removeAll(set);
		Assert.assertTrue(g.contains("foo"));
		set.add("foo");
		g.removeAll(set);
		Assert.assertFalse(g.contains("foo"));
		Assert.assertEquals(0, g.getVertexCount());
	}

	@Test
	public void testReverse() {
		TrivialDigraph<Object,Object> g = new TrivialDigraph<Object,Object>();
		Assert.assertSame(g, g.reverse());
	}

	@Test
	public void testSubgraph() {
		TrivialDigraph<Object,Object> g = new TrivialDigraph<Object,Object>();
		Set<Object> set = new HashSet<Object>();
		set.add("foo");
		Assert.assertEquals(0, g.subgraph(set).getVertexCount());
		g.add("foo");
		Assert.assertEquals(1, g.subgraph(set).getVertexCount());
	}

	@Test
	public void testSources() {
		Assert.assertFalse(new TrivialDigraph<Object,Object>().sources("foo").iterator().hasNext());
	}

	@Test
	public void testTargets() {
		Assert.assertFalse(new TrivialDigraph<Object,Object>().targets("foo").iterator().hasNext());
	}

	@Test
	public void testIsAcyclic() {
		TrivialDigraph<Object,Object> g = new TrivialDigraph<Object,Object>();
		Assert.assertTrue(g.isAcyclic());
		g.add("foo");
		Assert.assertTrue(g.isAcyclic());
	}
}
