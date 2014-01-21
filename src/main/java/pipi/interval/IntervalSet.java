package pipi.interval;

import java.util.List;

public interface IntervalSet {

	public abstract void addInterval(Interval interval);

	public abstract void removeInterval(Interval interval);

	public abstract void addAllRanges(IntervalSet treeIntervalSets);

	public abstract void removeAllRanges(IntervalSet ranges);

	public abstract List<Interval> getIntervals();

	public abstract IntervalSet getContainedIntervals(Interval interval);

	public abstract IntervalSet getSubIntervals(Interval interval);

	public abstract boolean isEmpty();

	public abstract boolean contains(Interval interval);

	public abstract IntervalSet getComplement();

	public abstract boolean isAnythingInside(Interval interval);

	public int count(Interval interval);
	
	public IntervalSet copy();
	
}