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
