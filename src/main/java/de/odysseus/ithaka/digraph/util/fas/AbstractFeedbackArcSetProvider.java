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
package de.odysseus.ithaka.digraph.util.fas;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import de.odysseus.ithaka.digraph.Digraph;
import de.odysseus.ithaka.digraph.Digraphs;
import de.odysseus.ithaka.digraph.EdgeWeights;
import de.odysseus.ithaka.digraph.MapDigraph;

/**
 * Abstract feedback arc set provider.
 */
public abstract class AbstractFeedbackArcSetProvider implements FeedbackArcSetProvider {
	class FeedbackTask<V,E> implements Callable<FeedbackArcSet<V,E>> {
		final Digraph<V,E> digraph;
		final EdgeWeights<? super V> weights;
		final FeedbackArcSetPolicy policy;
		final Set<V> scc;

		FeedbackTask(Digraph<V,E> digraph, EdgeWeights<? super V> weights, FeedbackArcSetPolicy policy, Set<V> scc) {
			this.digraph = digraph;
			this.weights = weights;
			this.policy = policy;
			this.scc = scc;
		}

		@Override
		public FeedbackArcSet<V, E> call() {
			return fas(digraph.subgraph(scc), weights, policy);			
		}
	}
	
	private final boolean decompose;
	private final int numberOfThreads;

	/**
	 * Create provider which calculates a feedback arc set on a digraph (in the
	 * current thread).
	 * If the <code>decompose</code> flag set to <code>true</code>, the provider
	 * decomposes a digraph into strongly connected components and computes
	 * feedback arc sets on the components and combines the results.
	 * If the <code>decompose</code> flag set to <code>false</code>, the
	 * {@link #mfas(Digraph, EdgeWeights)} and {@link #lfas(Digraph, EdgeWeights)}
	 * implementation methods must be able to handle arbitrary digraphs or the
	 * {@link #getFeedbackArcSet(Digraph, EdgeWeights, FeedbackArcSetPolicy)}
	 * method <em>must</em> be called with strongly connected components only!
	 * @param decompose whether to decompose into strongly connected components.
	 */
	protected AbstractFeedbackArcSetProvider(boolean decompose) {
		this.decompose = decompose;
		this.numberOfThreads = 0;
	}

	/**
	 * Create provider which decomposes a digraph into strongly connected components
	 * and computes feedback arc sets on the components and combines the results.
	 * Feedback calculations can be distributed to a given number of threads.
	 * If <code>numberOfThreads == 0</code>, calculation is done in the current thread.
	 * @param numberOfThreads number
	 */
	protected AbstractFeedbackArcSetProvider(int numberOfThreads) {
		this.decompose = true;
		this.numberOfThreads = numberOfThreads;
	}

	/**
	 * Compute minimum feedback arc set.
	 * @param digraph
	 * @param weights
	 * @return feedback arc set or <code>null</code>
	 */
	protected <V,E> Digraph<V,E> mfas(Digraph<V,E> digraph, EdgeWeights<? super V> weights) {
		return null;
	}

	/**
	 * Compute light feedback arc set.
	 * @param digraph original graph or tangle of it (if decompose == true)
	 * @param weights
	 * @return feedback arc set
	 */
	protected abstract <V,E> Digraph<V,E> lfas(Digraph<V,E> digraph, EdgeWeights<? super V> weights);
	
	private <V,E> FeedbackArcSet<V,E> fas(Digraph<V,E> digraph, EdgeWeights<? super V> weights, FeedbackArcSetPolicy policy) {
		EdgeWeights<? super V> filteredWeights = weights;
		if (policy == FeedbackArcSetPolicy.MIN_SIZE) {
			/*
			 * Manipulate graph weights if the feedback arc set has to be of minimum size (i.e., #arcs):
			 * all weights are increased by an amount (delta) equal to the sum of all weights,
			 * so that every arc is heavier than any arc subset with the original weights.
			 * A minimum weight feedback arc set (mwfas) of the resulting graph has a total weight of
			 *  #arcs(mwfas) * delta + origWeight(mwfas).
			 * Since origWeight(mwfas) < delta, the determined mwfas has a minimum #arcs and
			 * from all those feedback arc sets of minimum size it has minimum original weight (we could
			 * have obtained the first result easily by setting all weights to 1, but not the second).
			 */
			final EdgeWeights<? super V> origWeights = weights;
			final int delta = totalWeight(digraph, origWeights);
			filteredWeights = new EdgeWeights<V>() {
				@Override
				public Integer get(V source, V target) {
					return origWeights.get(source, target) + delta;
				}
			};
		}
		Digraph<V, E> result = mfas(digraph, filteredWeights);
		boolean exact = true;
		if (result == null) {
			result = lfas(digraph, filteredWeights);
			exact = false;
		}
		return new FeedbackArcSet<V, E>(result, totalWeight(result, weights), policy, exact);
	}

	protected <V,E> int totalWeight(Digraph<V,E> digraph, EdgeWeights<? super V> weights) {
		int weight = 0;
		for (V source : digraph.vertices()) {
			for (V target : digraph.targets(source)) {
				weight += weights.get(source, target);
			}
		}
		return weight;
	}

	private <V,E> List<FeedbackArcSet<V,E>> executeAll(List<FeedbackTask<V,E>> tasks) {
		List<FeedbackArcSet<V, E>> result = new ArrayList<FeedbackArcSet<V,E>>();
		if (numberOfThreads <= 0) {
			for (FeedbackTask<V,E> task : tasks) {
				result.add(task.call());
			}
		} else {
			ExecutorService executor = Executors.newFixedThreadPool(Math.min(numberOfThreads, tasks.size()));
			try {
				for (Future<FeedbackArcSet<V, E>> future : executor.invokeAll(tasks)) {
					result.add(future.get());
				}
			} catch (ExecutionException e) {
				return null; // should not happen
			} catch (InterruptedException e) {
				return null; // should not happen
			} finally {
				executor.shutdown();
			}
		}
		return result;
	}

	@Override
	public <V,E> FeedbackArcSet<V,E> getFeedbackArcSet(Digraph<V,E> digraph, EdgeWeights<? super V> weights, FeedbackArcSetPolicy policy) {
		if (digraph.isAcyclic()) {
			return new FeedbackArcSet<V, E>(Digraphs.<V, E>emptyDigraph(), 0, policy, true);
		}
		if (decompose) {
			List<FeedbackTask<V,E>> tasks = new ArrayList<FeedbackTask<V,E>>();
			for (Set<V> component : Digraphs.scc(digraph)) {
				if (component.size() > 1) {
					tasks.add(new FeedbackTask<V, E>(digraph, weights, policy, component));
				}
			}

			List<FeedbackArcSet<V,E>> feedbacks = executeAll(tasks);
			if (feedbacks == null) {
				return null;
			}

			int weight = 0;
			boolean exact = true;
			Digraph<V, E> result = new MapDigraph<V, E>();
			for (FeedbackArcSet<V,E> feedback : feedbacks) {
				for (V source : feedback.vertices()) {
					for (V target : feedback.targets(source)) {
						result.put(source, target, digraph.get(source, target));
					}
				}
				exact &= feedback.isExact();
				weight += feedback.getWeight();
			}
			return new FeedbackArcSet<V, E>(result, weight, policy, exact);
		} else {
			return fas(digraph, weights, policy);
		}
	}
}
