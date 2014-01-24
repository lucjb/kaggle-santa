package pipi.packer;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.apache.commons.lang3.tuple.Pair;

import pipi.OrientedDimension3d;
import pipi.interval.Rectangle;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.primitives.Ints;

public class CompositePacker implements Packer {

	private final Packer[] packers;
	private Packer bestPacker;
	private final int[] wins;
	private final int[] ties;
	private ExecutorService executorService;

	public CompositePacker(ExecutorService newFixedThreadPool, Packer... packers) {
		this.packers = packers;
		this.wins = new int[packers.length];
		this.ties = new int[packers.length];
		this.executorService = newFixedThreadPool;
	}

	@Override
	public PackResult packPesents(final List<OrientedDimension3d> dimensions) {
		Set<Integer> putIndexes = Sets.newHashSet();
		Set<Integer> notIndexes = Sets.newHashSet();

		List<Pair<Integer, PackResult>> packerResults = Lists.newArrayList();
		List<Future<Pair<Integer, PackResult>>> callables = Lists.newArrayList();
		for (int i = 0; i < this.packers.length; i++) {
			final Packer packer = this.packers[i];
			final int index = i;

			Future<Pair<Integer, PackResult>> future = this.executorService
					.submit(new Callable<Pair<Integer, PackResult>>() {

						@Override
						public Pair<Integer, PackResult> call() throws Exception {
							PackResult packedPesents = packer.packPesents(dimensions);
							return Pair.of(index, packedPesents);
						}
					});
			callables.add(future);
		}

		for (Future<Pair<Integer, PackResult>> future : callables) {
			Pair<Integer, PackResult> pair;
			try {
				pair = future.get();
				putIndexes.addAll(pair.getValue().getPutIndexes());
				notIndexes.addAll(pair.getValue().getNotIndexes());
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			packerResults.add(pair);
		}

		Collections.sort(packerResults, new Comparator<Pair<Integer, PackResult>>() {

			@Override
			public int compare(Pair<Integer, PackResult> o1, Pair<Integer, PackResult> o2) {
				return -Ints.compare(o1.getRight().getPutRectangles().size(), o2.getRight().getPutRectangles().size());
			}
		});

		PackResult bestPackedPesents = packerResults.get(0).getRight();
		int lastIndex = 1;
		while (lastIndex < packerResults.size()
				&& packerResults.get(lastIndex).getRight().getPutRectangles().size() == bestPackedPesents.getPutRectangles().size()) {
			lastIndex++;
		}
		int winners = lastIndex;

		if (bestPackedPesents.getPutRectangles().size() == dimensions.size()) {
			if (winners == 1) {
				this.wins[packerResults.get(0).getLeft()]++;
			} else {
				for (int i = 0; i < lastIndex; i++) {
					this.ties[packerResults.get(i).getLeft()]++;
				}
			}

		}
		this.bestPacker = this.packers[packerResults.get(0).getKey()];

		return new PackResult(bestPackedPesents.getPutRectangles(), putIndexes, notIndexes);
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
			strings.add(String.format("%s[w: %d, t: %d]", this.packers[i], this.wins[i], this.ties[i]));
		}
		return strings.toString();

	}

}
