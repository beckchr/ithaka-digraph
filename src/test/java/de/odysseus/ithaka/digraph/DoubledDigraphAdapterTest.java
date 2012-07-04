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

import org.junit.Assert;
import org.junit.Test;

public class DoubledDigraphAdapterTest {

	@Test
	public void testAdd() {
		DoubledDigraphAdapter<String,Integer> digraph = new DoubledDigraphAdapter<String,Integer>();

		digraph.add("foo");
		Assert.assertTrue(digraph.contains("foo"));
		Assert.assertTrue(digraph.reverse().contains("foo"));
	}

	@Test
	public void testvertices() {
		DoubledDigraphAdapter<String,Integer> digraph = new DoubledDigraphAdapter<String,Integer>();
		digraph.add("foo");
		digraph.add("bar");
		digraph.add("foobar");

		Iterator<String> iter1 = digraph.vertices().iterator();
		Iterator<String> iter2 = digraph.reverse().vertices().iterator();
		while (iter1.hasNext()) {
			Assert.assertTrue(iter2.hasNext());
			Assert.assertSame(iter1.next(), iter2.next());
		}
		Assert.assertFalse(iter2.hasNext());
	}

	@Test
	public void testNodes2() {
		DoubledDigraphAdapter<String,Integer> digraph = new DoubledDigraphAdapter<String,Integer>();
		digraph.add("foo");
		digraph.add("bar");
		digraph.add("foobar");

		Iterator<String> iter1 = digraph.vertices().iterator();
		while (iter1.hasNext()) {
			iter1.next();
			iter1.remove();
			Assert.assertEquals(digraph.getVertexCount(), digraph.reverse().getVertexCount());
		}
		Assert.assertEquals(0, digraph.getVertexCount());
	}

	@Test
	public void testPut() {
		DoubledDigraphAdapter<String,Integer> digraph = new DoubledDigraphAdapter<String,Integer>();

		digraph.put("foo", "bar", 1);
		Assert.assertEquals(1, digraph.get("foo", "bar").intValue());
		Assert.assertEquals(1, digraph.reverse().get("bar", "foo").intValue());
	}

	@Test
	public void testRemoveVV() {
		DoubledDigraphAdapter<String,Integer> digraph = new DoubledDigraphAdapter<String,Integer>();

		digraph.put("foo", "bar", 1);
		digraph.remove("foo", "bar");
		Assert.assertFalse(digraph.contains("foo", "bar"));
		Assert.assertFalse(digraph.reverse().contains("bar", "foo"));
	}

	@Test
	public void testRemoveV() {
		DoubledDigraphAdapter<String,Integer> digraph = new DoubledDigraphAdapter<String,Integer>();

		digraph.add("foo");
		digraph.remove("foo");
		Assert.assertFalse(digraph.contains("foo"));
		Assert.assertFalse(digraph.reverse().contains("foo"));
	}

	@Test
	public void testRemoveAll() {
		DoubledDigraphAdapter<String,Integer> digraph = new DoubledDigraphAdapter<String,Integer>();

		digraph.put("a", "a", 1);
		digraph.put("a", "b", 2);
		digraph.put("a", "c", 3);
		digraph.put("b", "a", 4);

		HashSet<String> set = new HashSet<String>();
		set.add("a");
		set.add("b");
		digraph.removeAll(set);

		Assert.assertFalse(digraph.contains("a"));
		Assert.assertFalse(digraph.reverse().contains("a"));
		Assert.assertFalse(digraph.contains("b"));
		Assert.assertFalse(digraph.reverse().contains("b"));
		Assert.assertTrue(digraph.contains("c"));
		Assert.assertTrue(digraph.reverse().contains("c"));
		Assert.assertEquals(1, digraph.getVertexCount());
		Assert.assertEquals(1, digraph.reverse().getVertexCount());
		Assert.assertEquals(0, digraph.getEdgeCount());
		Assert.assertEquals(0, digraph.reverse().getEdgeCount());
	}


	@Test
	public void testTargets() {
		DoubledDigraphAdapter<String,Integer> digraph = new DoubledDigraphAdapter<String,Integer>();
		digraph.put("foo", "bar", 1);
		digraph.put("bar", "foo", 2);
		digraph.put("bar", "foobar", 3);
		digraph.add("baz");

		Iterator<String> targets = digraph.targets("bar").iterator();
		Assert.assertEquals("foo", targets.next());
		targets.remove();
		Assert.assertFalse(digraph.contains("bar", "foo"));
		Assert.assertFalse(digraph.reverse().contains("foo", "bar"));
		Assert.assertEquals(2, digraph.getEdgeCount());
		Assert.assertEquals(2, digraph.reverse().getEdgeCount());
		Assert.assertEquals("foobar", targets.next());
		targets.remove();
		Assert.assertFalse(digraph.contains("bar", "foobar"));
		Assert.assertFalse(digraph.reverse().contains("foobar", "bar"));
		Assert.assertEquals(1, digraph.getEdgeCount());
		Assert.assertEquals(1, digraph.reverse().getEdgeCount());
		Assert.assertFalse(targets.hasNext());
		Assert.assertEquals(4, digraph.getVertexCount());
		Assert.assertEquals(4, digraph.reverse().getVertexCount());
	}

	@Test
	public void testGetAdapterFactory() {
		DigraphFactory<MapDigraph<String,Integer>> factory = MapDigraph.getDefaultDigraphFactory();
		Assert.assertNotNull(DoubledDigraphAdapter.<String,Integer>getAdapterFactory(factory).create());
	}

	@Test
	public void testGetDigraphFactory() {
		Assert.assertNotNull(new DoubledDigraphAdapter<String,Integer>().getDigraphFactory().create());
	}

	@Test
	public void testGetInDegree() {
		DoubledDigraphAdapter<String,Integer> digraph = new DoubledDigraphAdapter<String,Integer>();
		digraph.put("foo", "bar", 1);
		digraph.put("bar", "foo", 2);
		digraph.put("bar", "foobar", 3);
		digraph.add("baz");

		Assert.assertEquals(1, digraph.getInDegree("foo"));
		Assert.assertEquals(1, digraph.getInDegree("bar"));
		Assert.assertEquals(1, digraph.getInDegree("foobar"));
		Assert.assertEquals(0, digraph.getInDegree("baz"));

		Assert.assertEquals(1, digraph.reverse().getInDegree("foo"));
		Assert.assertEquals(2, digraph.reverse().getInDegree("bar"));
		Assert.assertEquals(0, digraph.reverse().getInDegree("foobar"));
		Assert.assertEquals(0, digraph.reverse().getInDegree("baz"));
	}

	@Test
	public void testSources() {
		DoubledDigraphAdapter<String,Integer> digraph = new DoubledDigraphAdapter<String,Integer>();
		digraph.put("foo", "bar", 1);
		digraph.put("bar", "foo", 2);
		digraph.put("bar", "foobar", 3);
		digraph.add("baz");

		for (String node : digraph.vertices()) {
			Iterator<String> iter1 = digraph.sources(node).iterator();
			Iterator<String> iter2 = digraph.reverse().targets(node).iterator();
			while (iter1.hasNext()) {
				Assert.assertTrue(iter2.hasNext());
				Assert.assertSame(iter1.next(), iter2.next());
			}
			Assert.assertFalse(iter2.hasNext());
		}
	}

	@Test
	public void testReverse() {
		DoubledDigraphAdapter<String,Integer> digraph = new DoubledDigraphAdapter<String,Integer>();
		Assert.assertNotSame(digraph, digraph.reverse());
		Assert.assertSame(digraph, digraph.reverse().reverse());
	}

}
