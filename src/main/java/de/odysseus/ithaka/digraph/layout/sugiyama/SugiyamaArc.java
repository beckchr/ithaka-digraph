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

import java.util.Collections;
import java.util.List;

import de.odysseus.ithaka.digraph.layout.DigraphLayoutArc;
import de.odysseus.ithaka.digraph.layout.DigraphLayoutPoint;

public class SugiyamaArc<V,E> implements DigraphLayoutArc<V,E> {
	private E edge;
	private List<DigraphLayoutPoint> points;
	private int sourceSlot;
	private int targetSlot;
	private SugiyamaNode<V> source;
	private SugiyamaNode<V> target;
	private SugiyamaArc<V,E> backArc;
	private boolean feedback;
	private DigraphLayoutPoint startPoint;
	private DigraphLayoutPoint endPoint;

	public SugiyamaArc(SugiyamaNode<V> source, SugiyamaNode<V> target, boolean feedback, E edge) {
		this.source = source;
		this.target = target;
		this.feedback = feedback;
		this.edge = edge;
	}

	public SugiyamaArc(SugiyamaNode<V> source, SugiyamaNode<V> target, E edge, E backArc) {
		this(source, target, true, edge);
		this.backArc = new SugiyamaArc<V, E>(source, target, false, backArc);
	}

	@Override
	public SugiyamaNode<V> getSource() {
		return source;
	}

	@Override
	public SugiyamaNode<V> getTarget() {
		return target;
	}

	@Override
	public boolean isFeedback() {
		return feedback;
	}

	@Override
	public E getEdge() {
		return edge;
	}

	public SugiyamaArc<V,E> getBackArc() {
		return backArc;
	}

	public int getSourceSlot() {
		return sourceSlot;
	}

	public void setSourceSlot(int sourceSlot) {
		this.sourceSlot = sourceSlot;
	}

	@Override
	public List<DigraphLayoutPoint> getBendPoints() {
		return points == null ? Collections.<DigraphLayoutPoint>emptyList() : points;
	}

	public void setBendPoints(List<DigraphLayoutPoint> points) {
		this.points = points;
	}

	@Override
	public String toString() {
		return source + " --" + edge.toString() + "--> " + target;
	}

	public int getTargetSlot() {
		return targetSlot;
	}

	public void setTargetSlot(int targetSlot) {
		this.targetSlot = targetSlot;
	}

	@Override
	public DigraphLayoutPoint getStartPoint() {
		return startPoint;
	}

	@Override
	public DigraphLayoutPoint getEndPoint() {
		return endPoint;
	}

	public void setStartPoint(DigraphLayoutPoint startPoint) {
		this.startPoint = startPoint;
	}

	public void setEndPoint(DigraphLayoutPoint endPoint) {
		this.endPoint = endPoint;
	}

	public void fixEndPoints() {
		startPoint = feedback ? source.getUpperSlotPoint(sourceSlot) : source.getLowerSlotPoint(sourceSlot);
		endPoint = feedback ? target.getLowerSlotPoint(targetSlot) : target.getUpperSlotPoint(targetSlot);
	}
}
