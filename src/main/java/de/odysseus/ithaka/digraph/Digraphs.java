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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

/**
 * This class provides some common digraph utilities.
 *
 * @author Christoph Beck
 */
public class Digraphs {
	/**
	 * Get an unmodifiable empty digraph.
	 * @return empty digraph
	 */
	public static <V,E> DoubledDigraph<V,E> emptyDigraph() {
		return new EmptyDigraph<V,E>();
	}

	/**
	 * Wraps the given digraph to make it unmodifiable. Whenever a method
	 * is called on the resulting digraph that could modify the underlying
	 * digraph, an exception is thrown.
	 * @param <V> vertex type
	 * @param <E> edge type
	 * @param digraph
	 * @return unmodifiable digraph equivalent to the given digraph
	 */
	public static <V,E> Digraph<V,E> unmodifiableDigraph(Digraph<V,E> digraph) {
		return new UnmodifiableDigraph<V, E>(digraph);
	}

	/**
	 * Topologically sort vertices of an acyclic directed graph (DAG).
	 * This method will produce an ordering of vertices, such that all
	 * edges go from left right.
	 * If the input graph is not a DAG, the algorithm will still perform,
	 * but in the resulting list there will be edges from vertices to
	 * vertices prior in the list.
	 * @param <V> vertex type
	 * @param digraph input graph
	 * @param descending let edges go from right to left if <code>true</code>
	 * @return list of vertices toplologically ordered.
	 */
	public static <V> List<V> topsort(Digraph<V,?> digraph, boolean descending) {
		List<V> finished = new ArrayList<V>();
		Set<V> discovered = new HashSet<V>(digraph.getVertexCount());
		for (V vertex : digraph.vertices()) {
			if (!discovered.contains(vertex)) {
				dfs(digraph, vertex, discovered, finished);
			}
		}
		if (!descending) {
			Collections.reverse(finished);
		}
		return finished;
	}

	/**
	 * Compute the set of vertices reachable from the given source in the given digraph.
	 * @param <V> vertex type
	 * @param digraph
	 * @param source source vertex
	 * @return the set of vertices reachable from <code>source</code>
	 */
	public static <V> Set<V> closure(Digraph<V,?> digraph, V source) {
		Set<V> closure = new HashSet<V>();
		dfs(digraph, source, closure, closure);
		return closure;
	}

	/**
	 * Answer <code>true</code> if the given digraph is acyclic (DAG).
	 * Per definition, the empty graph and single vertex digraphs are acyclic.
	 * @param <V> vertex type
	 * @param digraph
	 * @return <code>true</code> iff the given digraph is acyclic
	 */
	public static <V> boolean isAcyclic(Digraph<V,?> digraph) {
		int n = digraph.getVertexCount();
		if (n < 2) {
			return true; // no self loop
		}
		if (digraph.getEdgeCount() > (n*(n-1))/2) {
			return false;
		}
		return Digraphs.<V>scc(digraph).size() == n;
	}

	/**
	 * Test if the given digraphs are equivalent.
	 * This is the case if  and the same vertices are connected by an edge.
	 * <ol>
	 * <li>both digraphs contain the same vertices</li>
	 * <li>the same pairs of vertices are connected by an edge in both digraphs </li>
	 * <li>optionally, this method may require that the corresponding edges are equal.</li>
	 * </ol>
	 * @param <V> vertex type
	 * @param first first digraph.
	 * @param second second digraph.
	 * @param compareEdges if <code>true</code>, compare edges using <code>equals()</code>.
	 * @return <code>true</code> iff the two digraphs are equivalent according to the above description.
	 */
	public static <V> boolean isEquivalent(Digraph<V,?> first, Digraph<V,?> second, boolean compareEdges) {
		if (first == second) {
			return true;
		}
		if (first.getEdgeCount() != second.getEdgeCount() || first.getVertexCount() != second.getVertexCount()) {
			return false;
		}
		for (V source : first.vertices()) {
			if (!second.contains(source)) {
				return false;
			}
			for (V target : first.targets(source)) {
				if (!second.contains(source, target)) {
					return false;
				}
				if (compareEdges) {
					Object edge1 = first.get(source, target);
					Object edge2 = second.get(source, target);
					if (edge1 != edge2 && (edge1 == null || edge2 == null || !edge1.equals(edge2))) {
						return false;
					}
				}
			}
		}
		return true;
	}

	/**
	 * Answer <code>true</code> if the given digraph is strongly connected.
	 * Per definition, the empty graph and single vertex digraphs are strongly connected.
	 *
	 * @param <V> vertex type
	 * @param digraph
	 * @return <code>true</code> iff the given digraph is strongly connected
	 */
	public static <V> boolean isStronglyConnected(Digraph<V,?> digraph) {
		int n = digraph.getVertexCount();
		if (n < 2) {
			return true;
		}
		return Digraphs.<V>scc(digraph).size() == 1;
	}

	/**
	 * Answer <code>true</code> if there is a path from the given source to the given target
	 * in the supplied graph. If source is equal to target, answer <code>true</code>.
	 * @param <V> vertex type
	 * @param digraph
	 * @param source source vertex
	 * @param target target vertex
	 * @return <code>true</code> iff there's a path from <code>source</code> to <code>target</code> in <code>digraph</code>
	 */
	public static <V> boolean isReachable(Digraph<V,?> digraph, V source, V target) {
		return digraph.contains(source, target) || Digraphs.<V>closure(digraph, source).contains(target);
	}

	/**
	 * Perform a depth first search.
	 *
	 * @param <V> vertex type
	 * @param digraph
	 * @param source dfs start vertex
	 * @param discovered set of vertices already discovered during search
	 * @param finished collection of vertices visited during search
	 */
	public static <V> void dfs(Digraph<V,?> digraph, V source, Set<? super V> discovered, Collection<? super V> finished) {
		if (discovered.add(source)) {
			for (V target : digraph.targets(source)) {
				dfs(digraph, target, discovered, finished);
			}
			finished.add(source);
		}
	}

	/**
	 * Perform an undirected depth first search.
	 *
	 * @param <V> vertex type
	 * @param digraph
	 * @param source dfs start vertex
	 * @param discovered set of vertices already discovered during search
	 * @param finished collection of vertices visited during search
	 */
	public static <V> void dfs2(Digraph<V,?> digraph, V source, Set<? super V> discovered, Collection<? super V> finished) {
		dfs2(digraph, digraph.reverse(), source, discovered, finished);
	}

	private static <V> void dfs2(Digraph<V,?> forward, Digraph<V,?> backward, V source, Set<? super V> discovered, Collection<? super V> finished) {
		if (discovered.add(source)) {
			for (V target : forward.targets(source)) {
				dfs2(forward, backward, target, discovered, finished);
			}
			for (V target : backward.targets(source)) {
				dfs2(forward, backward, target, discovered, finished);
			}
			finished.add(source);
		}
	}

	/**
	 * Compute strongly connected components.
	 * @param <V>
	 * @param digraph
	 * @return strongly connected components
	 */
	public static <V> List<Set<V>> scc(Digraph<V,?> digraph) {
		List<Set<V>> components = new ArrayList<Set<V>>();
		Digraph<V,?> reverse = digraph.reverse();

		// dfs on this graph
		Stack<V> stack = new Stack<V>();
		Set<V> discovered = new HashSet<V>();
		for (V vertex : digraph.vertices()) {
			dfs(digraph, vertex, discovered, stack);
		}

		// dfs on reverse graph
		discovered = new HashSet<V>();
		while (!stack.isEmpty()) {
			V vertex = stack.pop();
			if (!discovered.contains(vertex)) {
				Set<V> component = new HashSet<V>();
				dfs(reverse, vertex, discovered, component);
				components.add(component);
			}
		}

		return components;
	}

	/**
	 * Compute weakly connected components.
	 * @param <V>
	 * @param digraph
	 * @return weakly connected components
	 */
	public static <V> List<Set<V>> wcc(Digraph<V,?> digraph) {
		List<Set<V>> components = new ArrayList<Set<V>>();
		Digraph<V,?> reverse = digraph.reverse();

		// dfs on both graphs
		Set<V> discovered = new HashSet<V>();
		for (V vertex : digraph.vertices()) {
			if (!discovered.contains(vertex)) {
				Set<V> component = new HashSet<V>();
				dfs2(digraph, reverse, vertex, discovered, component);
				components.add(component);
			}
		}
		return components;
	}

	/**
	 * Compute the reverse graph.
	 * @param <V> vertex type
	 * @param <E> edge type
	 * @param <G> result type
	 * @param digraph input digraph
	 * @param factory factory used to create result graph
	 * @return the reverse digraph
	 */
	public static <V,E,G extends Digraph<V, E>> G reverse(Digraph<V,E> digraph, DigraphFactory<? extends G> factory) {
		G reverse = factory.create();
		for (V source : digraph.vertices()) {
			reverse.add(source);
			for (V target : digraph.targets(source)) {
				reverse.put(target, source, digraph.get(source, target));
			}
		}
		return reverse;
	}

	/**
	 * Copy a digraph.
	 * @param digraph graph to copy
	 * @param factory factory used to create copy
	 * @return a copy of the given digraph
	 */
	public static <V, E, G extends Digraph<V, E>> G copy(Digraph<V, E> digraph, DigraphFactory<? extends G> factory) {
		G result = factory.create();
		for (V source : digraph.vertices()) {
			result.add(source);
			for (V target : digraph.targets(source)) {
				result.put(source, target, digraph.get(source, target));
			}
		}
		return result;
	}
	
	/**
	 * Create subgraph induced by the specified vertices.
	 * @param <V> vertex type
	 * @param <E> edge type
	 * @param <G> subgraph type
	 * @param digraph
	 * @param vertices
	 * @param factory
	 * @return subgraph of the supplied digraph containing the specified vertices.
	 */
	public static <V,E,G extends Digraph<V,E>> G subgraph(
			Digraph<V,E> digraph,
			Set<V> vertices,
			DigraphFactory<? extends G> factory) {
		G subgraph = factory.create();
		for (V v : vertices) {
			if (digraph.contains(v)) {
				subgraph.add(v);
				for (V w : digraph.targets(v)) {
					if (vertices.contains(w)) {
						subgraph.put(v, w, digraph.get(v, w));
					}
				}
			}
		}
		return subgraph;
	}

	/**
	 * Create partition graph from a given vertex decomposition.
	 * @param <G> the type of the component graphs
	 * @param <P> the type of the result graph
	 * @param <C> the edge type of the result graph
	 * @param factory1 used to create the partition graph
	 * @param factory2 used to create the subgraphs
	 * @param cumulator used to cumulate edges between subgraphs
	 * @return a digraph of subgraphs of this digraph
	 */
	public static <V,E,G extends Digraph<V,E>,P extends Digraph<G,C>, C> P partition(
			Digraph<V,E> digraph,
			Iterable<Set<V>> sets,
			DigraphFactory<? extends P> factory1,
			DigraphFactory<? extends G> factory2,
			EdgeCumulator<? super G,C,? super E> cumulator) {
		P partition = factory1.create();

		// map vertices to their component graph
		Map<V,G> vertex2subgraph = new HashMap<V,G>();
		for (Set<V> set : sets) {
			G subgraph = subgraph(digraph, set, factory2);
			for (V v : subgraph.vertices()) {
				assert digraph.contains(v) && !vertex2subgraph.containsKey(v);
				vertex2subgraph.put(v, subgraph);
			}
			partition.add(subgraph);
		}
		assert vertex2subgraph.size() == digraph.getVertexCount();

		// build component graph
		for (V v : digraph.vertices()) {
			G source = vertex2subgraph.get(v);
			for (V w : digraph.targets(v)) {
				G target = vertex2subgraph.get(w);
				if (source != target) {
					C cumulatedEdge = null;
					if (cumulator != null) {
						E subgraphEdge = digraph.get(v, w);
						C componentEdge = partition.get(source, target);
						cumulatedEdge = cumulator.add(source, target, componentEdge, subgraphEdge);
					}
					partition.put(source, target, cumulatedEdge);
				}
			}
		}
		return partition;
	}
}
