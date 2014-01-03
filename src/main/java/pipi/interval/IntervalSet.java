package pipi.interval;

import java.util.List;

public interface IntervalSet {

	public abstract void addInterval(Interval interval);

	public abstract void removeInterval(Interval interval);

	public abstract void addAllRanges(IntervalSet treeIntervalSets);

	public abstract void removeAllRanges(IntervalSet ranges);

	public abstract List<Interval> getIntervals();

	public abstract IntervalSet getContainedIntervals(Interval verticalRange);

	public abstract IntervalSet getSubIntervals(Interval verticalRange);

	public abstract boolean isEmpty();

	public abstract boolean contains(Interval verticalRange);

	public abstract IntervalSet getComplement();

	public abstract boolean isAnythingInside(Interval bound);

}