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
package de.odysseus.ithaka.digraph.dot;

import java.awt.Color;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import de.odysseus.ithaka.digraph.DigraphProvider;
import de.odysseus.ithaka.digraph.SimpleDigraph;
import de.odysseus.ithaka.digraph.SimpleDigraphAdapter;

public class DotExporterTest {
	@Test
	public void testSimple() throws IOException {
		DotProvider<Integer, Boolean, SimpleDigraph<Integer>> provider = new DotProvider<Integer, Boolean, SimpleDigraph<Integer>>() {
			@Override
			public String getNodeId(Integer vertex) {
				return "v" + vertex;
			}
			@Override
			public Iterable<DotAttribute> getNodeAttributes(Integer vertex) {
				return vertex == 7 ? Arrays.asList(new DotAttribute("color", Color.red)) : null;
			}
			@Override
			public Iterable<DotAttribute> getEdgeAttributes(Integer source, Integer target, Boolean edge) {
				return source == 1 ? Arrays.asList(new DotAttribute("arrowtail", "diamond")) : null;
			}
			@Override
			public Iterable<DotAttribute> getDefaultNodeAttributes(SimpleDigraph<Integer> digraph) {
				return Arrays.asList(new DotAttribute("shape", "plaintext"));
			}
			
			@Override
			public Iterable<DotAttribute> getDefaultGraphAttributes(SimpleDigraph<Integer> digraph) {
				return null;
			}
			
			@Override
			public Iterable<DotAttribute> getDefaultEdgeAttributes(SimpleDigraph<Integer> digraph) {
				return null;
			}
			
			@Override
			public Iterable<DotAttribute> getSubgraphAttributes(SimpleDigraph<Integer> subgraph, Integer vertex) {
				return null;
			}
		};
		
		SimpleDigraph<Integer> digraph = new SimpleDigraphAdapter<Integer>();
		digraph.add(1, 2);
		digraph.add(1, 3);
		digraph.add(4, 2);
		digraph.add(5, 6);
		digraph.add(7);

		StringWriter writer = new StringWriter();
		new DotExporter("", "").export(provider, digraph, null, writer);
		Assert.assertEquals(
				"digraph G {node[shape=plaintext];v1;v2;v3;v4;v5;v6;v7[color=\"#FF0000\"];v1 -> v2[arrowtail=diamond];v1 -> v3[arrowtail=diamond];v4 -> v2;v5 -> v6;}",
				writer.toString());
		
//		writer = new StringWriter();
//		new DotExporter().export(provider, digraph, null, writer);
//		System.out.print(writer);

	}

	@Test
	public void testSubgraph() throws IOException {
		DotProvider<Integer, Boolean, SimpleDigraph<Integer>> provider = new DotProvider<Integer, Boolean, SimpleDigraph<Integer>>() {
			@Override
			public String getNodeId(Integer vertex) {
				return "v" + vertex;
			}
			@Override
			public Iterable<DotAttribute> getNodeAttributes(Integer vertex) {
				return null;
			}
			@Override
			public Iterable<DotAttribute> getEdgeAttributes(Integer source, Integer target, Boolean edge) {
				return null;
			}
			@Override
			public Iterable<DotAttribute> getDefaultNodeAttributes(SimpleDigraph<Integer> digraph) {
				return null;
			}
			
			@Override
			public Iterable<DotAttribute> getDefaultGraphAttributes(SimpleDigraph<Integer> digraph) {
				return null;
			}
			
			@Override
			public Iterable<DotAttribute> getDefaultEdgeAttributes(SimpleDigraph<Integer> digraph) {
				return null;
			}
			
			@Override
			public Iterable<DotAttribute> getSubgraphAttributes(SimpleDigraph<Integer> subgraph, Integer vertex) {
				return Arrays.asList(new DotAttribute("label", getNodeId(vertex)));
			}
		};
		
		DigraphProvider<Integer, SimpleDigraph<Integer>> subgraphs = new DigraphProvider<Integer, SimpleDigraph<Integer>>() {
			SimpleDigraph<Integer> subgraph1 = new SimpleDigraphAdapter<Integer>();
			SimpleDigraph<Integer> subgraph2 = new SimpleDigraphAdapter<Integer>();
			{
				subgraph1.add(10);
				subgraph2.add(20);
			}
			@Override
			public SimpleDigraph<Integer> get(Integer value) {
				return value == 1 ? subgraph1 : value == 2 ? subgraph2 : null;
			}
		};
		
		SimpleDigraph<Integer> digraph = new SimpleDigraphAdapter<Integer>();
		digraph.add(1, 2);
		digraph.add(1, 3);
		digraph.add(3, 2);

		Writer writer = new StringWriter();
		new DotExporter("", "").export(provider, digraph, subgraphs, writer);		
		Assert.assertEquals(
				"digraph G {compound=true;subgraph cluster_v1 {graph[label=v1];v10;}subgraph cluster_v2 {graph[label=v2];v20;}v3;v10 -> v20[ltail=cluster_v1, lhead=cluster_v2];v10 -> v3[ltail=cluster_v1];v3 -> v20[lhead=cluster_v2];}",
				writer.toString());

//		writer = new StringWriter();
//		new DotExporter().export(provider, digraph, subgraphs, writer);
//		System.out.print(writer);
	}
}
