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
package de.odysseus.ithaka.digraph.tgf;

import java.io.IOException;
import java.io.StringWriter;

import org.junit.Assert;
import org.junit.Test;

import de.odysseus.ithaka.digraph.SimpleDigraph;
import de.odysseus.ithaka.digraph.SimpleDigraphAdapter;
import de.odysseus.ithaka.digraph.WeightedDigraph;
import de.odysseus.ithaka.digraph.WeightedDigraphAdapter;

public class TGFExporterTest {
	@Test
	public void testSimple() throws IOException {
		SimpleDigraph<Integer> digraph = new SimpleDigraphAdapter<Integer>();
		digraph.add(1, 2);
		digraph.add(1, 3);
		digraph.add(4, 2);
		digraph.add(5, 6);
		digraph.add(7);

		TGFProvider<Integer, Object> provider = new TGFProvider<Integer, Object>() {
			@Override
			public String getVertexLabel(Integer vertex) {
				return String.valueOf(vertex);
			}
			@Override
			public String getEdgeLabel(Object edge) {
				return null;
			}
		};
		
		StringWriter writer = new StringWriter();
		new TGFExporter("  ").export(provider, digraph, writer);
		Assert.assertEquals("1 1  2 2  3 3  4 4  5 5  6 6  7 7  #  1 2  1 3  4 2  5 6  ", writer.toString());
	}

	@Test
	public void testWeighted() throws IOException {
		WeightedDigraph<Integer> digraph = new WeightedDigraphAdapter<Integer>();
		digraph.add(1, 2, 1);
		digraph.add(1, 3, 2);
		digraph.add(4, 2, 3);
		digraph.add(5, 6, 4);
		digraph.add(7);

		TGFProvider<Integer, Integer> provider = new TGFProvider<Integer, Integer>() {
			@Override
			public String getVertexLabel(Integer vertex) {
				return String.valueOf(vertex);
			}
			@Override
			public String getEdgeLabel(Integer edge) {
				return String.valueOf(edge);
			}
		};
		
		StringWriter writer = new StringWriter();
		new TGFExporter("  ").export(provider, digraph, writer);
		Assert.assertEquals("1 1  2 2  3 3  4 4  5 5  6 6  7 7  #  1 2 1  1 3 2  4 2 3  5 6 4  ", writer.toString());
	}
}
