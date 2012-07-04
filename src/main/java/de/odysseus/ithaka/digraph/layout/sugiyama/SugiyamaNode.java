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

import java.util.Comparator;

import de.odysseus.ithaka.digraph.layout.LayoutDimension;
import de.odysseus.ithaka.digraph.layout.LayoutNode;
import de.odysseus.ithaka.digraph.layout.LayoutPoint;

public class SugiyamaNode<V> implements LayoutNode<V> {
	public static final Comparator<SugiyamaNode<?>> CMP_ID = new Comparator<SugiyamaNode<?>>() {
		@Override
		public int compare(SugiyamaNode<?> o1, SugiyamaNode<?> o2) {
			return o1.id < o2.id ? -1 : o1.id > o2.id ? 1 : 0;
		}
	};

	public static final Comparator<SugiyamaNode<?>> CMP_INDEX = new Comparator<SugiyamaNode<?>>() {
		@Override
		public int compare(SugiyamaNode<?> o1, SugiyamaNode<?> o2) {
			return o1.getIndex() < o2.getIndex() ? -1 : o1.getIndex() > o2.getIndex() ? 1 : 0;
		}
	};

	private static int NEXT_ID = 1;

	private int id;
	private double position;
	private int layer;
	private int index;
	private int temporary;
	private V vertex;
	private LayoutPoint point;
	private LayoutDimension dimension;
	private int lowerSlots;
	private int upperSlots;
	private int lowerCenterSlot = -1;
	private int upperCenterSlot = -1;
	private int maxSlotDistance = 0;

	private SugiyamaNode<V> upper, lower; // for dummy nodes: upper and lower neighbors

	/**
	 * Create dummy node.
	 * Dummy nodes have <code>null</code> data.
	 * @param dummyDimension
	 */
	public SugiyamaNode(LayoutDimension dummyDimension) {
		this(null, dummyDimension, 0);
	}

	/**
	 * Create new vertex.
	 * @param vertex data
	 * @param dimension
	 */
	public SugiyamaNode(V vertex, LayoutDimension dimension, int maxSlotDistance) {
		this.id = NEXT_ID++;
		this.vertex = vertex;
		this.dimension = dimension;
		this.maxSlotDistance = Math.min(maxSlotDistance, dimension.w);
	}

	public int nextLowerSlot() {
		return lowerSlots++;
	}

	public LayoutPoint getLowerSlotPoint(int slot) {
		return new LayoutPoint(point.x + getOffset(slot, lowerSlots, lowerCenterSlot), point.y + dimension.h);
	}

	public int nextUpperSlot() {
		return upperSlots++;
	}

	public LayoutPoint getUpperSlotPoint(int slot) {
		return new LayoutPoint(point.x + getOffset(slot, upperSlots, upperCenterSlot), point.y);
	}

	private int getOffset(int slot, int slots, int center) {
		int x = 0;
		if (center < 0) {
			int d = Math.min(maxSlotDistance, dimension.w / slots);
			x = dimension.w / 2 + (slot - slots / 2) * d;
			if (slots % 2 == 0) {
				x += d / 2;
			}
		} else {
			int d = Math.min(maxSlotDistance, dimension.w / (2 * Math.max(center, slots - center)));
			x = dimension.w / 2 + (slot - center) * d;
		}
		return x;
	}

	public void setLowerCenterSlot(int slot) {
		lowerCenterSlot = slot;
	}

	public void setUpperCenterSlot(int slot) {
		upperCenterSlot = slot;
	}

	@Override
	public LayoutDimension getDimension() {
		return dimension;
	}

	public boolean isDummy() {
		return vertex == null;
	}

	@Override
	public V getVertex() {
		return vertex;
	}

	public double getPosition() {
		return position;
	}

	public void setPosition(double position) {
		this.position = position;
	}

	public int getLayer() {
		return layer;
	}

	public void setLayer(int layer) {
		this.layer = layer;
	}

	@Override
	public LayoutPoint getPoint() {
		return point;
	}

	public void setPoint(LayoutPoint point) {
		this.point = point;
	}

	@Override
	public String toString() {
		return isDummy() ? "<dummy>" : vertex.toString();
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public SugiyamaNode<V> getLower() {
		return lower;
	}

	public void setLower(SugiyamaNode<V> lower) {
		this.lower = lower;
	}

	public SugiyamaNode<V> getUpper() {
		return upper;
	}

	public void setUpper(SugiyamaNode<V> upper) {
		this.upper = upper;
	}

	public int getTemporary() {
		return temporary;
	}

	public void setTemporary(int temporary) {
		this.temporary = temporary;
	}
}
