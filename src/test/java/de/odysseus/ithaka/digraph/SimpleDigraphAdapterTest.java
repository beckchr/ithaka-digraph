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

public class SimpleDigraphAdapterTest {

	@Test
	public void testGetAdapterFactory() {
		DigraphFactory<MapDigraph<String,Boolean>> factory = MapDigraph.getDefaultDigraphFactory();
		Assert.assertNotNull(SimpleDigraphAdapter.<String>getAdapterFactory(factory).create());
	}

	@Test
	public void testGetDigraphFactory() {
		Assert.assertNotNull(new SimpleDigraphAdapter<String>().getDigraphFactory().create());
	}

	@Test
	public void testAddVV() {
		SimpleDigraph<String> digraph = new SimpleDigraphAdapter<String>();
		digraph.add("foo", "bar");
		Assert.assertTrue(digraph.contains("foo", "bar"));
	}

	@Test
	public void testRemoveVV() {
		SimpleDigraph<String> digraph = new SimpleDigraphAdapter<String>();
		digraph.add("foo", "bar");
		digraph.remove("foo", "bar");
		Assert.assertFalse(digraph.contains("foo", "bar"));
	}

	@Test
	public void testPutVVBoolean() {
		SimpleDigraph<String> digraph = new SimpleDigraphAdapter<String>();
		digraph.put("foo", "bar", true);
		Assert.assertTrue(digraph.contains("foo", "bar"));
	}

	@Test(expected=IllegalArgumentException.class)
	public void testPutVVBoolean2() {
		SimpleDigraph<String> digraph = new SimpleDigraphAdapter<String>();
		digraph.put("foo", "bar", false);
	}

	@Test
	public void testReverse() {
		SimpleDigraph<Integer> digraph = new SimpleDigraphAdapter<Integer>();
		digraph.add(1, 2);
		digraph.add(1, 3);
		digraph.add(4, 2);
		digraph.add(5, 6);
		digraph.add(7);

		SimpleDigraph<Integer> reverse = digraph.reverse();
		Assert.assertEquals(7, reverse.getVertexCount());
		Assert.assertEquals(4, reverse.getEdgeCount());
		Assert.assertTrue(reverse.contains(2, 1));
		Assert.assertTrue(reverse.contains(3, 1));
		Assert.assertTrue(reverse.contains(2, 4));
		Assert.assertTrue(reverse.contains(6, 5));
	}

	@Test
	public void testSubgraphSetOfV() {
		SimpleDigraph<Integer> digraph = new SimpleDigraphAdapter<Integer>();
		digraph.add(1, 2);
		digraph.add(1, 3);
		digraph.add(4, 2);
		digraph.add(5, 6);
		digraph.add(7);

		Set<Integer> nodes = new HashSet<Integer>();
		nodes.add(1);
		nodes.add(2);
		nodes.add(3);
		nodes.add(7);
		SimpleDigraph<Integer> subgraph = digraph.subgraph(nodes);

		Assert.assertEquals(4, subgraph.getVertexCount());
		Assert.assertEquals(2, subgraph.getEdgeCount());
		Assert.assertTrue(subgraph.contains(1, 2));
		Assert.assertTrue(subgraph.contains(1, 3));
	}

	@Test
	public void testPartitionBoolean() {
		SimpleDigraphAdapter<Integer> g = new SimpleDigraphAdapter<Integer>();
		g.add(1, 2);
		g.add(2, 1);
		g.add(1, 3);
		g.add(3, 4);
		g.add(4, 2);

		g.add(3, 5);

		WeightedDigraph<SimpleDigraph<Integer>> scc = g.partition(false);
		Assert.assertEquals(2, scc.getVertexCount());
		Assert.assertEquals(1, scc.getEdgeCount());

		WeightedDigraph<SimpleDigraph<Integer>> wcc = g.partition(true);
		Assert.assertEquals(1, wcc.getVertexCount());
		Assert.assertEquals(0, wcc.getEdgeCount());
	}
}
