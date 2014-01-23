package pipi.packer;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.lang3.tuple.Pair;

import pipi.OrientedDimension3d;
import pipi.interval.PutRectangle;
import pipi.interval.Rectangle;

import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;

public class CompositePacker implements Packer {

	private final Packer[] packers;
	private Packer bestPacker;
	private final int[] wins;
	private final int[] ties;
	private final int[] perfects;
	private ExecutorService executorService;

	public CompositePacker(Packer... packers) {
		this.packers = packers;
		this.wins = new int[packers.length];
		this.ties = new int[packers.length];
		this.perfects = new int[packers.length];
		this.executorService = Executors.newFixedThreadPool(6);
	}

	@Override
	public List<PutRectangle> packPesents(final Collection<OrientedDimension3d> dimensions) {
		List<Pair<Integer, List<PutRectangle>>> packerResults = Lists.newArrayList();
		List<Future<Pair<Integer, List<PutRectangle>>>> callables = Lists.newArrayList();
		for (int i = 0; i < this.packers.length; i++) {
			final Packer packer = this.packers[i];
			final int index = i;

			Future<Pair<Integer, List<PutRectangle>>> future = this.executorService
					.submit(new Callable<Pair<Integer, List<PutRectangle>>>() {

						@Override
						public Pair<Integer, List<PutRectangle>> call() throws Exception {
							List<PutRectangle> packedPesents = packer.packPesents(dimensions);
							return Pair.of(index, packedPesents);
						}
					});
			callables.add(future);
		}

		for (Future<Pair<Integer, List<PutRectangle>>> future : callables) {
			Pair<Integer, List<PutRectangle>> pair;
			try {
				pair = future.get();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			packerResults.add(pair);
		}

		Collections.sort(packerResults, new Comparator<Pair<Integer, List<PutRectangle>>>() {

			@Override
			public int compare(Pair<Integer, List<PutRectangle>> o1, Pair<Integer, List<PutRectangle>> o2) {
				return -Ints.compare(o1.getRight().size(), o2.getRight().size());
			}
		});

		List<PutRectangle> bestPackedPesents = packerResults.get(0).getRight();
		int lastIndex = 1;
		while (lastIndex < packerResults.size()
				&& packerResults.get(lastIndex).getRight().size() == bestPackedPesents.size()) {
			lastIndex++;
		}
		int winners = lastIndex;

		if (winners == 1) {
			this.wins[packerResults.get(0).getLeft()]++;
		} else {
			for (int i = 0; i < lastIndex; i++) {
				this.ties[packerResults.get(i).getLeft()]++;
			}

		}
		if (bestPackedPesents.size() == dimensions.size()) {
			for (Pair<Integer, List<PutRectangle>> pair : packerResults.subList(0, lastIndex)) {
				this.perfects[pair.getLeft()]++;
			}
		}
		this.bestPacker = this.packers[packerResults.get(0).getKey()];
		return bestPackedPesents;
	}

	@Override
	public void freeAll(Collection<Rectangle> prefill) {
		this.bestPacker.freeAll(prefill);
		for (Packer packer : this.packers) {
			if (packer != this.bestPacker) {
				packer.parasite(this.bestPacker);
			}
		}
	}

	@Override
	public boolean isEmpty() {
		return this.bestPacker.isEmpty();
	}

	@Override
	public void parasite(Packer packer) {
		throw new RuntimeException("not impemented");
	}

	@Override
	public String toString() {
		List<String> strings = Lists.newArrayList();
		for (int i = 0; i < this.packers.length; i++) {
			String.format("%s[p: %d, w: %d, t: %d]", this.packers[i], this.perfects[i], this.wins[i], this.ties[i]);
		}
		return strings.toString();

	}

}
