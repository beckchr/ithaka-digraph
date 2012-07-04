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
import java.util.Iterator;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

public class DigraphAdapterTest {
	class TestAdapter extends DigraphAdapter<String,Integer> {
		TestAdapter() {
			super(MapDigraph.<String,Integer>getDefaultDigraphFactory().create());
		}
		TestAdapter(Digraph<String,Integer> delegate) {
			super(delegate);
		}
	}

	@Test
	public void testHashCode() {
		Digraph<String,Integer> delegate = MapDigraph.<String,Integer>getDefaultDigraphFactory().create();
		TestAdapter digraph = new TestAdapter(delegate);
		Assert.assertEquals(delegate.hashCode(), digraph.hashCode());
	}

	@Test
	public void testAdd() {
		TestAdapter digraph = new TestAdapter();
		Assert.assertFalse(digraph.contains("foo"));
		digraph.add("foo");
		Assert.assertTrue(digraph.contains("foo"));
	}

	@Test
	public void testContainsObjectObject() {
		TestAdapter digraph = new TestAdapter();
		Assert.assertFalse(digraph.contains("foo", "bar"));
		digraph.put("foo", "bar", 1);
		Assert.assertTrue(digraph.contains("foo", "bar"));
	}

	@Test
	public void testContainsObject() {
		TestAdapter digraph = new TestAdapter();
		Assert.assertFalse(digraph.contains("foo"));
		digraph.add("foo");
		Assert.assertTrue(digraph.contains("foo"));
	}

	@Test
	public void testGet() {
		TestAdapter digraph = new TestAdapter();
		Assert.assertNull(digraph.get("foo", "bar"));
		digraph.put("foo", "bar", 1);
		Assert.assertEquals(1, digraph.get("foo", "bar").intValue());
	}

	@Test
	public void testGetOutDegree() {
		TestAdapter digraph = new TestAdapter();
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
		TestAdapter digraph = new TestAdapter();
		Assert.assertEquals(0, digraph.getEdgeCount());

		digraph.put("a", "a", 1);
		digraph.put("a", "b", 2);
		digraph.put("a", "c", 3);
		digraph.put("b", "a", 4);
		Assert.assertEquals(4, digraph.getEdgeCount());
	}

	@Test
	public void testgetVertexCount() {
		TestAdapter digraph = new TestAdapter();
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
	public void testvertices() {
		TestAdapter digraph = new TestAdapter();
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
	public void testPut() {
		TestAdapter digraph = new TestAdapter();

		Assert.assertNull(digraph.put("foo", "bar", 1));
		Assert.assertTrue(digraph.contains("foo", "bar"));
	}

	@Test
	public void testRemoveVV() {
		TestAdapter digraph = new TestAdapter();

		digraph.put("foo", "bar", 1);
		Assert.assertEquals(1, digraph.remove("foo", "bar").intValue());
		Assert.assertFalse(digraph.contains("foo", "bar"));
	}

	@Test
	public void testRemoveV() {
		TestAdapter digraph = new TestAdapter();

		digraph.add("foo");
		Assert.assertTrue(digraph.remove("foo"));
		Assert.assertFalse(digraph.contains("foo"));
	}

	@Test
	public void testRemoveAll() {
		TestAdapter digraph = new TestAdapter();
		digraph.add("foo");
		HashSet<String> set = new HashSet<String>();
		set.add("bar");
		digraph.removeAll(set);
		Assert.assertTrue(digraph.contains("foo"));
		set.add("foo");
		digraph.removeAll(set);
		Assert.assertFalse(digraph.contains("foo"));
		Assert.assertEquals(0, digraph.getVertexCount());
	}

	@Test
	public void testReverse() {
		TestAdapter digraph = new TestAdapter();
		digraph.put("foo", "bar", 1);
		digraph.put("bar", "foo", 2);
		digraph.put("bar", "foobar", 3);
		digraph.add("baz");

		Digraph<String,Integer> reverse = digraph.reverse();
		Assert.assertEquals(digraph.getVertexCount(), reverse.getVertexCount());
		Assert.assertEquals(digraph.getEdgeCount(), reverse.getEdgeCount());
		for (String source : digraph.vertices()) {
			for (String target : digraph.targets(source)) {
				Assert.assertEquals(reverse.get(target, source), digraph.get(source, target));
			}
		}
	}

	@Test
	public void testSubgraph() {
		TestAdapter digraph = new TestAdapter();
		digraph.put("foo", "bar", 1);
		digraph.put("bar", "foo", 2);
		digraph.put("bar", "foobar", 3);
		digraph.add("baz");

		Set<String> nodes = new HashSet<String>();
		nodes.add("foo");
		nodes.add("bar");

		Digraph<String,Integer> subgraph = digraph.subgraph(nodes);
		Assert.assertEquals(2, subgraph.getVertexCount());
		Assert.assertEquals(2, subgraph.getEdgeCount());
		Assert.assertTrue(subgraph.contains("foo", "bar"));
		Assert.assertTrue(subgraph.contains("bar", "foo"));
	}

	@Test
	public void testIsAcyclic() {
		TestAdapter digraph = new TestAdapter();
		digraph.put("foo", "bar", 1);
		digraph.put("bar", "foo", 2);
		digraph.put("bar", "foobar", 3);
		digraph.add("baz");

		Assert.assertFalse(digraph.isAcyclic());
		digraph.remove("foo", "bar");
		Assert.assertTrue(digraph.isAcyclic());
	}

	@Test
	public void testTargets() {
		TestAdapter digraph = new TestAdapter();
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
	public void testToString() {
		Digraph<String,Integer> delegate = MapDigraph.<String,Integer>getDefaultDigraphFactory().create();
		TestAdapter digraph = new TestAdapter(delegate);
		Assert.assertEquals(delegate.toString(), digraph.toString());
	}

	@Test
	public void testEqualsObject() {
		Digraph<String,Integer> delegate = MapDigraph.<String,Integer>getDefaultDigraphFactory().create();
		Assert.assertTrue(new TestAdapter(delegate).equals(new TestAdapter(delegate)));
	}
}
