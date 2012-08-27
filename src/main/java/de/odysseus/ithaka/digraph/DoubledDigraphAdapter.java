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
package de.odysseus.ithaka.digraph;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

/**
 * Doubled digraph implementation.
 * 
 * @param <V> vertex type
 * @param <E> edge type
 */
public class DoubledDigraphAdapter<V,E> extends DigraphAdapter<V,E> implements DoubledDigraph<V,E> {
	/**
	 * Factory creating <code>DoubledDigraph</code>.
	 * @param factory delegate factory
	 * @return doubled digraph factory
	 */
	public static <V,E> DigraphFactory<DoubledDigraphAdapter<V,E>> getAdapterFactory(final DigraphFactory<? extends Digraph<V,E>> factory) {
		return new DigraphFactory<DoubledDigraphAdapter<V,E>>() {
			@Override
			public DoubledDigraphAdapter<V,E> create() {
				return new DoubledDigraphAdapter<V,E>(factory);
			}
		};
	}
	
	private final DoubledDigraphAdapter<V,E> reverse;
	private final DigraphFactory<? extends Digraph<V,E>> factory;
	
	public DoubledDigraphAdapter() {
		this(MapDigraph.<V,E>getDefaultDigraphFactory());
	}

	public DoubledDigraphAdapter(DigraphFactory<? extends Digraph<V,E>> factory) {
		super(factory.create());
		this.factory = factory;
		this.reverse = createReverse();
	}

	protected DoubledDigraphAdapter(DigraphFactory<? extends Digraph<V,E>> factory, DoubledDigraphAdapter<V,E> reverse) {
		super(factory.create());
		this.factory = factory;
		this.reverse = reverse;
	}

	protected DoubledDigraphAdapter<V,E> createReverse() {
		return new DoubledDigraphAdapter<V, E>(factory, this);
	}

	public DigraphFactory<? extends DoubledDigraph<V, E>> getDigraphFactory() {
		return getAdapterFactory(factory);
	}
	
	protected DigraphFactory<? extends Digraph<V,E>> getDelegateFactory() {
		return factory;
	}
	
	@Override
	public int getInDegree(Object vertex) {
		return reverse.getOutDegree(vertex);
	}
	
	@Override
	public Iterable<V> sources(Object target) {
		return reverse.targets(target);
	}
	
	@Override
	public final boolean add(V vertex) {
		reverse.add0(vertex);
		return add0(vertex);
	}

	protected boolean add0(V vertex) {
		return super.add(vertex);
	}

	@Override
	public final boolean remove(V vertex) {
		reverse.remove0(vertex);
		return remove0(vertex);
	}

	protected boolean remove0(V vertex) {
		return super.remove(vertex);
	}

	@Override
	public void removeAll(Collection<V> vertices) {
		reverse.removeAll0(vertices);
		removeAll0(vertices);
	}

	protected void removeAll0(Collection<V> vertices) {
		super.removeAll(vertices);
	}
	
	/**
	 * Make sure the reverse digraph is kept in sync if <code>Iterator.remove()</code> is called.
	 */
	@Override
	public Iterable<V> vertices() {
		final Iterator<V> delegate = super.vertices().iterator();
		if (!delegate.hasNext()) {
			return Collections.emptySet();
		}
		return new Iterable<V>() {
			@Override
			public Iterator<V> iterator() {
				return new Iterator<V>() {
					V vertex;
					@Override
					public boolean hasNext() {
						return delegate.hasNext();
					}
					@Override
					public V next() {
						return vertex = delegate.next();
					}
					@Override
					public void remove() {
						delegate.remove();
						reverse.remove0(vertex);
					}
				};
			}
			@Override
			public String toString() {
				return DoubledDigraphAdapter.super.vertices().toString();
			}
		};
	}
	
	/**
	 * Make sure the reverse digraph is kept in sync if <code>Iterator.remove()</code> is called.
	 */
	@Override
	public Iterable<V> targets(final Object source) {
		final Iterator<V> delegate = super.targets(source).iterator();
		if (!delegate.hasNext()) {
			return Collections.emptySet();
		}
		return new Iterable<V>() {
			@Override
			public Iterator<V> iterator() {
				return new Iterator<V>() {
					V target;
					@Override
					public boolean hasNext() {
						return delegate.hasNext();
					}
					@Override
					public V next() {
						return target = delegate.next();
					}
					@Override
					public void remove() {
						delegate.remove();
						@SuppressWarnings("unchecked")
						V v = (V)source;
						reverse.remove0(target, v);
					}
				};
			}			
			@Override
			public String toString() {
				return DoubledDigraphAdapter.super.targets(source).toString();
			}
		};
	}
	
	@Override
	public final E put(V source, V target, E edge) {
		reverse.put0(target, source, edge);
		return put0(source, target, edge);
	}

	protected E put0(V source, V target, E edge) {
		return super.put(source, target, edge);
	}

	@Override
	public final E remove(V source, V target) {
		reverse.remove0(target, source);
		return remove0(source, target);
	}

	protected E remove0(V source, V target) {
		return super.remove(source, target);
	}

	@Override
	public final DoubledDigraphAdapter<V, E> reverse() {
		return reverse;
	}
}
