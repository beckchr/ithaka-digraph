/*
 * Copyright 2008 Odysseus Software GmbH, Frankfurt am Main/Germany.
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
