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
package de.odysseus.ithaka.digraph.io.graphml.yfiles;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.image.BufferedImage;

import de.odysseus.ithaka.digraph.layout.DigraphLayoutDimension;
import de.odysseus.ithaka.digraph.layout.DigraphLayoutDimensionProvider;

public class LabelDimensionProvider<V> implements DigraphLayoutDimensionProvider<V> {
	private static FontMetrics getFontMetrics(Font font) {
		Graphics graphics = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB).getGraphics();
		FontMetrics metrics = graphics.getFontMetrics(font);
		graphics.dispose();
		return metrics;
	}
	
	private final LabelResolver<? super V> labels;
	private final FontMetrics metrics;
	private final Insets insets;

	public LabelDimensionProvider(LabelResolver<? super V> labels, Font font) {
		this(labels, font, new Insets(5, 5, 5, 5));
	}

	public LabelDimensionProvider(LabelResolver<? super V> labels, Font font, Insets insets) {
		this(labels, getFontMetrics(font), insets);
	}

	public LabelDimensionProvider(LabelResolver<? super V> labels, FontMetrics metrics, Insets insets) {
		this.labels = labels;
		this.metrics = metrics;
		this.insets = insets;
	}

	@Override
	public DigraphLayoutDimension getDimension(V vertex) {
		return vertex == null ? new DigraphLayoutDimension(0, 0) : new DigraphLayoutDimension(
				metrics.stringWidth(labels.getLabel(vertex)) + insets.left + insets.right,
				metrics.getHeight() + insets.top + insets.bottom);
	}
}
