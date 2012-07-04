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

import junit.framework.TestCase;

import org.junit.Test;

import de.odysseus.ithaka.digraph.Digraph;
import de.odysseus.ithaka.digraph.SimpleDigraph;
import de.odysseus.ithaka.digraph.SimpleDigraphAdapter;
import de.odysseus.ithaka.digraph.layout.LayoutDimension;
import de.odysseus.ithaka.digraph.layout.LayoutDimensionProvider;
import de.odysseus.ithaka.digraph.layout.LayoutArc;
import de.odysseus.ithaka.digraph.layout.LayoutNode;
import de.odysseus.ithaka.digraph.layout.sugiyama.SugiyamaBuilder;

public class SugiyamaBuilderTest extends TestCase {
	private LayoutDimensionProvider<Integer> dim = new LayoutDimensionProvider<Integer>() {
		@Override
		public LayoutDimension getDimension(Integer node) {
			return new LayoutDimension(String.valueOf(node).length(), 1);
		}
	};

	@Test public void test1() {
		SimpleDigraph<Integer> dag = new SimpleDigraphAdapter<Integer>();
		dag.add(1, 2);
		dag.add(1, 3);
		dag.add(2, 3);

		SugiyamaBuilder<Integer,Boolean> layouter = new SugiyamaBuilder<Integer, Boolean>(1, 1);
		Digraph<? extends LayoutNode<Integer>,? extends LayoutArc<Integer,Boolean>> sugiyama =
				layouter.layout(dag, dim).getLayoutGraph();
		for (LayoutNode<Integer> node : sugiyama.vertices()) {
			System.out.println(node.getVertex() + " --> " + node.getPoint());
		}
	}
}
