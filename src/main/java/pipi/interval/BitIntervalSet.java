package pipi.interval;

import java.util.List;

import org.junit.Assert;

import pipi.PiolaBitset;

import com.google.common.collect.Lists;

public class BitIntervalSet implements IntervalSet {
	private final PiolaBitset froms;
	private final PiolaBitset tos;
//	private TreeIntervalSet treeIntervalSet;
	
	public BitIntervalSet(int range) {
		this.froms = new PiolaBitset(range + 1);
		this.tos = new PiolaBitset(range + 1);
//		this.treeIntervalSet =  new TreeIntervalSet(new Interval(0, range));
	}

	private BitIntervalSet(PiolaBitset from, PiolaBitset to) {
//		this.treeIntervalSet =  new TreeIntervalSet(new Interval(0, from.size() - 1));
		this.froms = from;
		this.tos = to;
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
//		treeIntervalSet.addInterval(interval);
//		Assert.assertEquals(treeIntervalSet.getIntervals(), this.getIntervals());
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
//		treeIntervalSet.removeInterval(interval);
//		Assert.assertEquals(treeIntervalSet.getIntervals(), this.getIntervals());

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
		List<Interval> intervals = Lists.newArrayList();
		for (int from = this.froms.nextSetBit(0); from >= 0; from = this.froms.nextSetBit(from + 1)) {
			int to = this.tos.nextSetBit(from);
			intervals.add(Interval.of(from, to));
		}
		return intervals;
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

		
//		Assert.assertEquals(treeIntervalSet.getContainedIntervals(verticalRange).getIntervals(), subs.getIntervals());

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
				int previousSetBitTo = this.froms.previousSetBit(to);
				if (previousSetBitTo >= 0 && previousSetBitTo <= to) {
					subs.tos.set(to);
				}
			}
		}
		subs.clearOutside(from, to);

//		Assert.assertEquals(treeIntervalSet.getSubIntervals(verticalRange).getIntervals(), subs.getIntervals());
		return subs;
		

	}

	@Override
	public boolean isEmpty() {
		return this.froms.isEmpty();
	}

	@Override
	public boolean contains(Interval verticalRange) {
		boolean realContains = realContains(verticalRange);

		
//		Assert.assertEquals(treeIntervalSet.contains(verticalRange), realContains);

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
	public IntervalSet complement() {
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
//		Assert.assertEquals(this.treeIntervalSet.complement().getIntervals(), complement.getIntervals());
//		complement.treeIntervalSet=(TreeIntervalSet) this.treeIntervalSet.complement();
		return complement;
	}

	private int lastIndex() {
		return this.tos.size() - 1;
	}

	void clearOutside(int from, int to) {
		this.froms.clear(0, from);
		this.tos.clear(0, from+1);
		this.froms.clear(to, this.froms.size());
		this.tos.clear(to + 1, this.tos.size());
	}

	@Override
	public String toString() {
		return this.getIntervals().toString();
	}

	@Override
	public boolean isAnythingInside(Interval bound) {
		int toSetBit = this.tos.nextSetBit(bound.getFrom()+1);
		if(toSetBit != -1){
			if(toSetBit <= bound.getTo()){
				return true;
			}
		}else{
			return false;
		}

		int fromSetBit = this.froms.nextSetBit(bound.getFrom());
		if(fromSetBit != -1){
			if(fromSetBit < bound.getTo()){
				return true;
			}else{
				return false;
			}
		}else{
			return true;
		}

	}
}
