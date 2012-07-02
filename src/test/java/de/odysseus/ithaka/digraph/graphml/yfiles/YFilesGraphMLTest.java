package de.odysseus.ithaka.digraph.graphml.yfiles;

import java.awt.Font;
import java.io.StringWriter;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.junit.Test;

import de.odysseus.ithaka.digraph.Digraph;
import de.odysseus.ithaka.digraph.DigraphProvider;
import de.odysseus.ithaka.digraph.MapDigraph;
import de.odysseus.ithaka.digraph.layout.LayoutBuilder;
import de.odysseus.ithaka.digraph.layout.sugiyama.SugiyamaBuilder;
import de.odysseus.staxon.xml.util.PrettyXMLStreamWriter;

public class YFilesGraphMLTest {
	@Test
	public void test() throws XMLStreamException {
		Digraph<String, Integer> digraph = new MapDigraph<String, Integer>();
		digraph.put("January", "August", 1);
		digraph.put("August", "September", 1);
		digraph.put("September", "January", 1);
		
		LabelResolver<Object> labels = new LabelResolver<Object>() {
			@Override
			public String getLabel(Object value) {
				return value == null ? null : value.toString();
			}
		};

		Font font = new Font(Font.DIALOG, Font.PLAIN, 11);
		LayoutBuilder<String, Integer> builder = new SugiyamaBuilder<String, Integer>(40, 50, false);
		YFilesGraphML<?, ?> graphML = new YFilesGraphML<String, Integer>(digraph, builder, labels, labels, font, font);
		
		StringWriter result = new StringWriter();
		XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(result);
		writer = new PrettyXMLStreamWriter(writer);
		graphML.export(writer);

//		System.out.println(result);
	}

	@Test
	public void testSubgraph() throws XMLStreamException {
		Digraph<String, Integer> digraph = new MapDigraph<String, Integer>();
		digraph.put("January", "August", 1);
		digraph.put("August", "September", 1);
		digraph.put("September", "January", 1);
		
		LabelResolver<Object> labels = new LabelResolver<Object>() {
			@Override
			public String getLabel(Object value) {
				return value == null ? null : value.toString();
			}
		};

		DigraphProvider<String, Digraph<String, Integer>> subgraphs = new DigraphProvider<String, Digraph<String,Integer>>() {
			@Override
			public Digraph<String, Integer> get(String vertex) {
				if ("August".equals(vertex)) {
					Digraph<String, Integer> subgraph = new MapDigraph<String, Integer>();
					subgraph.put("March", "December", 1);
					subgraph.put("December", "January", 1);
					subgraph.put("December", "July", 1);
					subgraph.put("July", "March", 1);
					return subgraph;
				}
				return null;
			}
		};

		Font font = new Font(Font.DIALOG, Font.PLAIN, 12);
		LayoutBuilder<String, Integer> builder = new SugiyamaBuilder<String, Integer>(40, 50, false);
		YFilesGraphML<?, ?> graphML = new YFilesGraphML<String, Integer>(digraph, subgraphs, builder, labels, font, true);
		
		StringWriter result = new StringWriter();
		XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(result);
		writer = new PrettyXMLStreamWriter(writer);
		graphML.export(writer);

		System.out.println(result);
	}
}
