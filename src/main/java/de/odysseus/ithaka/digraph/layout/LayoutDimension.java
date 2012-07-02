/*
 * Copyright 2008 Odysseus Software GmbH, Frankfurt am Main/Germany.
 */
package de.odysseus.ithaka.digraph.layout;

public class LayoutDimension {
	public int w;
	public int h;
	
	public LayoutDimension(int w, int h) {
		this.w = w;
		this.h = h;
	}

	public LayoutDimension copy() {
		return new LayoutDimension(w, h);
	}
	
	public LayoutDimension transpose() {
		int temp = w;
		w = h;
		h = temp;
		return this;
	}
	
	@Override
	public String toString() {
		return "(" + w + "," + h + ")";
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		LayoutDimension other = (LayoutDimension)obj;
		return w == other.w && h == other.h;
	}

	@Override
	public int hashCode() {
		return w ^ h;
	}
}
