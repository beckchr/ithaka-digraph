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
package de.odysseus.ithaka.digraph.layout.sugiyama;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.junit.Test;

import de.odysseus.ithaka.digraph.Digraph;
import de.odysseus.ithaka.digraph.SimpleDigraph;
import de.odysseus.ithaka.digraph.SimpleDigraphAdapter;
import de.odysseus.ithaka.digraph.layout.DigraphLayoutDimension;
import de.odysseus.ithaka.digraph.layout.DigraphLayoutDimensionProvider;
import de.odysseus.ithaka.digraph.layout.DigraphLayoutArc;
import de.odysseus.ithaka.digraph.layout.DigraphLayoutNode;
import de.odysseus.ithaka.digraph.layout.sugiyama.SugiyamaBuilder;

public class SugiyamaBuilderTest extends TestCase {
	private DigraphLayoutDimensionProvider<Integer> dim = new DigraphLayoutDimensionProvider<Integer>() {
		@Override
		public DigraphLayoutDimension getDimension(Integer node) {
			return new DigraphLayoutDimension(String.valueOf(node).length(), 1);
		}
	};

	@Test public void test1() {
		SimpleDigraph<Integer> dag = new SimpleDigraphAdapter<Integer>();
		dag.add(1, 2);
		dag.add(1, 3);
		dag.add(2, 3);

		SugiyamaBuilder<Integer,Boolean> builder = new SugiyamaBuilder<Integer, Boolean>(1, 1);
		Digraph<? extends DigraphLayoutNode<Integer>,? extends DigraphLayoutArc<Integer,Boolean>> sugiyama =
				builder.build(dag, dim).getLayoutGraph();
		Assert.assertEquals(dag.getVertexCount(), sugiyama.getVertexCount());
		Assert.assertEquals(dag.getEdgeCount(), sugiyama.getEdgeCount());
		for (DigraphLayoutNode<Integer> source : sugiyama.vertices()) {
			Assert.assertTrue(dag.contains(source.getVertex()));
			for (DigraphLayoutNode<Integer> target : sugiyama.targets(source)) {
				Assert.assertTrue(dag.contains(source.getVertex(), target.getVertex()));
				DigraphLayoutArc<Integer, Boolean> arc = sugiyama.get(source, target);
				Assert.assertEquals(dag.get(source.getVertex(), target.getVertex()), arc.getEdge());
				Assert.assertEquals(source, arc.getSource());
				Assert.assertEquals(target, arc.getTarget());
			}
		}
	}
}
