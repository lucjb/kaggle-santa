package pipi.interval;

import java.util.Collections;
import java.util.List;

import pipi.PiolaBitset;

import com.google.common.collect.Lists;

public class BitIntervalSet implements IntervalSet {
	private final PiolaBitset froms;
	private final PiolaBitset tos;

	// private TreeIntervalSet treeIntervalSet;

	public BitIntervalSet(int range) {
		this.froms = new PiolaBitset(range + 1);
		this.tos = new PiolaBitset(range + 1);
	}

	private BitIntervalSet(PiolaBitset from, PiolaBitset to) {
		this.froms = from;
		this.tos = to;
		assert this.froms.cardinality() == this.tos.cardinality();
	}

	@Override
	public void addInterval(Interval interval) {
		int from = interval.getFrom();
		int to = interval.getTo();

		from = lowerOverlapping(from);
		to = upperOverlapping(to);

		this.froms.clear(from, to);
		this.froms.set(from);
		this.tos.clear(from + 1, to + 1);
		this.tos.set(to);

		assert this.froms.cardinality() == this.tos.cardinality();
	}

	@Override
	public void removeInterval(Interval interval) {
		int from = interval.getFrom();
		int to = interval.getTo();

		int previousSetBitFrom = this.froms.previousSetBit(from - 1);
		if (previousSetBitFrom != -1) {
			int nextSetBitFrom = this.tos.nextSetBit(previousSetBitFrom);
			if (nextSetBitFrom > from) {
				this.tos.set(from);
			}
		}

		int nextSetBitTo = this.tos.nextSetBit(to + 1);
		{
			if (nextSetBitTo != -1) {
				int previousSetBitTo = this.froms.previousSetBit(nextSetBitTo);
				if (previousSetBitTo >= 0 && previousSetBitTo < to) {
					this.froms.set(to);
				}
			}
		}

		this.froms.clear(from, to);
		this.tos.clear(from + 1, to + 1);
		assert this.froms.cardinality() == this.tos.cardinality();

	}

	public int upperOverlapping(int to) {
		int nextSetBitTo = this.tos.nextSetBit(to);
		{
			if (nextSetBitTo != -1) {
				int previousSetBitTo = this.froms.previousSetBit(nextSetBitTo);
				if (previousSetBitTo >= 0 && previousSetBitTo <= to) {
					to = nextSetBitTo;
				}
			}
		}
		return to;
	}

	public int lowerOverlapping(int from) {
		int previousSetBitFrom = this.froms.previousSetBit(from);
		if (previousSetBitFrom != -1) {
			int nextSetBitFrom = this.tos.nextSetBit(previousSetBitFrom);
			if (nextSetBitFrom >= from) {
				from = previousSetBitFrom;
			}
		}
		return from;
	}

	@Override
	public void addAllRanges(IntervalSet treeIntervalSets) {
		List<Interval> intervals = treeIntervalSets.getIntervals();
		for (Interval interval : intervals) {
			this.addInterval(interval);
		}
	}

	@Override
	public void removeAllRanges(IntervalSet ranges) {
		List<Interval> intervals = ranges.getIntervals();
		for (Interval interval : intervals) {
			this.removeInterval(interval);
		}
	}

	@Override
	public List<Interval> getIntervals() {
		int startFrom = this.froms.nextSetBit(0);
		int lastIndex = this.lastIndex() + 1;
		return internalGetIntervals(this.froms, this.tos, startFrom, lastIndex);
	}

	private List<Interval> internalGetIntervals(PiolaBitset froms, PiolaBitset tos, int startFrom, int lastIndex) {
		List<Interval> intervals = Lists.newArrayList();
		for (int from = startFrom; from >= 0 && from < lastIndex; from = froms.nextSetBit(from + 1)) {
			int to = tos.nextSetBit(from);
			intervals.add(Interval.of(from, to));
		}
		return intervals;
	}

	public List<Interval> getComplementaryIntervals(Interval bound) {
		boolean start = this.froms.get(bound.getFrom());

		int lastIndex = bound.getTo();
		boolean end = this.tos.get(lastIndex);
		BitIntervalSet complement = new BitIntervalSet(this.tos, this.froms);
		int startWordIndex = PiolaBitset.wordIndex(bound.getFrom());
		int endWordIndex = PiolaBitset.wordIndex(bound.getTo());
		long startFromWord = complement.froms.words[startWordIndex];
		long startTosWord = complement.tos.words[startWordIndex];
		long endFromWord = complement.froms.words[endWordIndex];
		long endTosWord = complement.tos.words[endWordIndex];
		if (!start) {
			complement.froms.set(bound.getFrom());
		} else {
			complement.tos.clear(bound.getFrom());
		}

		if (!end) {
			complement.tos.set(lastIndex);
		} else {
			complement.froms.clear(lastIndex);
		}

		List<Interval> intervals = Lists.newArrayList();
		for (int from = complement.froms.nextSetBit(bound.getFrom()); from >= 0 && from < bound.getTo(); from = complement.froms
				.nextSetBit(from + 1)) {
			int to = complement.tos.nextSetBit(from);
			intervals.add(Interval.of(from, to));
		}

		complement.froms.words[startWordIndex] = startFromWord;
		complement.tos.words[startWordIndex] = startTosWord;
		complement.froms.words[endWordIndex] = endFromWord;
		complement.tos.words[endWordIndex] = endTosWord;
		assert this.froms.cardinality() == this.tos.cardinality();

		return intervals;

		// Assert.assertEquals(this.treeIntervalSet.complement().getIntervals(),
		// complement.getIntervals());
		// complement.treeIntervalSet=(TreeIntervalSet)
		// this.treeIntervalSet.complement();

	}

	@Override
	public IntervalSet getContainedIntervals(Interval verticalRange) {
		BitIntervalSet subs = new BitIntervalSet(this.froms.copy(), this.tos.copy());

		int from = verticalRange.getFrom();
		int to = verticalRange.getTo();
		int from1 = from;
		int previousSetBitFrom = this.froms.previousSetBit(from1);
		if (previousSetBitFrom != -1) {
			int nextSetBitFrom = this.tos.nextSetBit(previousSetBitFrom);
			if (nextSetBitFrom > from1) {
				from1 = previousSetBitFrom;
			}
		}

		from = from1;
		int to1 = to;
		int nextSetBitTo = this.tos.nextSetBit(to1);
		{
			if (nextSetBitTo != -1) {
				int previousSetBitTo = this.froms.previousSetBit(nextSetBitTo);
				if (previousSetBitTo >= 0 && previousSetBitTo < to1) {
					to1 = nextSetBitTo;
				}
			}
		}
		to = to1;
		subs.clearOutside(from, to);

		// Assert.assertEquals(treeIntervalSet.getContainedIntervals(verticalRange).getIntervals(),
		// subs.getIntervals());

		return subs;
	}

	@Override
	public IntervalSet getSubIntervals(Interval verticalRange) {
		BitIntervalSet subs = new BitIntervalSet(this.froms.copy(), this.tos.copy());
		int from = verticalRange.getFrom();
		int to = verticalRange.getTo();

		int previousSetBitFrom = this.froms.previousSetBit(from);
		if (previousSetBitFrom != -1) {
			int nextSetBitFrom = this.tos.nextSetBit(previousSetBitFrom);
			if (nextSetBitFrom > from) {
				subs.froms.set(from);
			}
		}
		int nextSetBitTo = this.tos.nextSetBit(to);
		{
			if (nextSetBitTo != -1) {
				int previousSetBitTo = this.froms.previousSetBit(nextSetBitTo);
				if (previousSetBitTo >= 0 && previousSetBitTo < to) {
					subs.tos.set(to);
				}
			}
		}
		subs.clearOutside(from, to);

		// Assert.assertEquals(treeIntervalSet.getSubIntervals(verticalRange).getIntervals(),
		// subs.getIntervals());
		return subs;
	}

	public void fastSubIntervals(Interval verticalRange) {
		int from = verticalRange.getFrom();
		int to = verticalRange.getTo();

		int previousSetBitFrom = this.froms.previousSetBit(from);
		if (previousSetBitFrom != -1) {
			int nextSetBitFrom = this.tos.nextSetBit(previousSetBitFrom);
			if (nextSetBitFrom > from) {
				this.froms.set(from);
			}
		}
		int nextSetBitTo = this.tos.nextSetBit(to);
		{
			if (nextSetBitTo != -1) {
				int previousSetBitTo = this.froms.previousSetBit(nextSetBitTo);
				if (previousSetBitTo >= 0 && previousSetBitTo < to) {
					this.tos.set(to);
				}
			}
		}
		this.clearOutside(from, to);

		// Assert.assertEquals(treeIntervalSet.getSubIntervals(verticalRange).getIntervals(),
		// subs.getIntervals());

	}

	public List<Interval> fastIntervals(Interval verticalRange) {
		int from = verticalRange.getFrom();
		int to = verticalRange.getTo();

		int fromWordIndex = from >>> PiolaBitset.ADDRESS_BITS_PER_WORD;
		int toWordIndex = to >>> PiolaBitset.ADDRESS_BITS_PER_WORD;
		long startFromWord = this.froms.words[fromWordIndex];
		long endTosWord = this.tos.words[toWordIndex];

		int nextSetBitFrom = this.tos.nextSetBit(from + 1);
		if (nextSetBitFrom != -1) {
			int previousSetBitFrom = this.froms.nextSetBitWord(fromWordIndex, from, nextSetBitFrom);
			if (previousSetBitFrom >= nextSetBitFrom) {
				this.froms.setWord(fromWordIndex, from);
			} else {
				from = previousSetBitFrom;
			}
		} else {
			return Collections.emptyList();
		}

		int nextSetBitTo = this.tos.nextSetBitWord(toWordIndex, to);
		{
			if (nextSetBitTo != -1) {
				int wordIndex = PiolaBitset.wordIndex(nextSetBitTo);
				int previousSetBitTo = this.froms.previousSetBitWord(wordIndex, nextSetBitTo);
				if (previousSetBitTo >= 0 && previousSetBitTo < to) {
					this.tos.words[toWordIndex] |= 1L << to; // Restores
																// invariants
				}
			}
		}
		List<Interval> internalGetIntervals = internalGetIntervals(this.froms, this.tos, from, to);
		this.froms.words[fromWordIndex] = startFromWord;
		this.tos.words[toWordIndex] = endTosWord;

		return internalGetIntervals;

	}

	@Override
	public boolean isEmpty() {
		return this.froms.isEmpty();
	}

	@Override
	public boolean contains(Interval verticalRange) {
		boolean realContains = realContains(verticalRange);

		// Assert.assertEquals(treeIntervalSet.contains(verticalRange),
		// realContains);

		return realContains;
	}

	private boolean realContains(Interval verticalRange) {
		int from = verticalRange.getFrom();
		int previousSetBitFrom = this.froms.previousSetBit(from);
		if (previousSetBitFrom == -1) {
			return false;
		}
		int nextSetBitFrom = this.tos.nextSetBit(previousSetBitFrom);
		if (nextSetBitFrom < verticalRange.getTo()) {
			return false;
		}
		return true;
	}

	@Override
	public IntervalSet getComplement() {
		BitIntervalSet complement = new BitIntervalSet(this.tos.copy(), this.froms.copy());
		if (!this.froms.get(0)) {
			complement.froms.set(0);
		} else {
			complement.tos.clear(0);
		}

		int lastIndex = lastIndex();
		if (!this.tos.get(lastIndex)) {
			complement.tos.set(lastIndex);
		} else {
			complement.froms.clear(lastIndex);
		}
		assert complement.froms.cardinality() == complement.tos.cardinality();
		return complement;
	}

	public IntervalSet complement() {
		long firstMask = this.froms.words[0] & 1L;
		this.tos.words[0] |= (1L - firstMask); // Restores invariants
		this.froms.words[0] &= ~(firstMask);

		int lastIndex = this.tos.nbits - 1;
		int wordLastIndex = lastIndex >>> PiolaBitset.ADDRESS_BITS_PER_WORD;

		long mask = 1L << lastIndex;
		long lastMask = this.tos.words[wordLastIndex] & mask;
		this.froms.words[wordLastIndex] |= (mask - lastMask);
		this.tos.words[wordLastIndex] &= ~(mask);

		long[] temp = this.froms.words;
		this.froms.words = this.tos.words;
		this.tos.words = temp;
		return this;
	}

	private int lastIndex() {
		return this.tos.size() - 1;
	}

	public void clearOutside(int from, int to) {
		this.froms.clearTo(from);
		this.tos.clearTo(from + 1);
		this.froms.clearFrom(to);
		this.tos.clearFrom(to + 1);
		assert this.froms.cardinality() == this.tos.cardinality();
	}

	@Override
	public String toString() {
		return this.getIntervals().toString();
	}

	@Override
	public boolean isAnythingInside(Interval bound) {
		int toSetBit = this.tos.nextSetBit(bound.getFrom() + 1);
		if (toSetBit == -1) {
			return false;
		}
		if (toSetBit <= bound.getTo()) {
			return true;
		}

		int fromSetBit = this.froms.nextSetBit(bound.getFrom());
		if (fromSetBit == -1) {
			return true;
		}
		if (fromSetBit < bound.getTo()) {
			return true;
		}
		if (fromSetBit < toSetBit) {
			return false;
		}
		return true;
	}

	public PiolaBitset getFroms() {
		return this.froms;
	}

	public PiolaBitset getTos() {
		return this.tos;
	}
	
	
}
