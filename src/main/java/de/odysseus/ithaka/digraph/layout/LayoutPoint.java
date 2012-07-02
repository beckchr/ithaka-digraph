/*
 * Copyright 2008 Odysseus Software GmbH, Frankfurt am Main/Germany.
 */
package de.odysseus.ithaka.digraph.layout;

public class LayoutPoint {
	public int x;
	public int y;
	
	public LayoutPoint(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public LayoutPoint copy() {
		return new LayoutPoint(x, y);
	}
	
	public LayoutPoint transpose() {
		int temp = x;
		x = y;
		y = temp;
		return this;
	}
	
	public void translate(int dx, int dy) {
		x += dx;
		y += dy;
	}
	
	@Override
	public String toString() {
		return "(" + x + "," + y + ")";
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		LayoutPoint other = (LayoutPoint)obj;
		return x == other.x && y == other.y;
	}

	@Override
	public int hashCode() {
		return x ^ y;
	}
}
