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

public class DigraphLayoutPoint {
	public int x;
	public int y;
	
	public DigraphLayoutPoint(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public DigraphLayoutPoint copy() {
		return new DigraphLayoutPoint(x, y);
	}
	
	public DigraphLayoutPoint transpose() {
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
		DigraphLayoutPoint other = (DigraphLayoutPoint)obj;
		return x == other.x && y == other.y;
	}

	@Override
	public int hashCode() {
		return x ^ y;
	}
}
