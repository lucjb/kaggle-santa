package pipi.interval;

import java.util.List;
import java.util.Set;

import com.google.common.collect.BoundType;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;

public class TreeIntervalSet implements IntervalSet {

	private RangeSet<Integer> rangeSet;
	private Interval span;

	public TreeIntervalSet(Interval span) {
		this.span = span;
		this.rangeSet = TreeRangeSet.create();
	}
	private TreeIntervalSet(Interval span,RangeSet<Integer> rangeSet) {
		this.span = span;
		this.rangeSet = rangeSet;
	}

	
	/* (non-Javadoc)
	 * @see pipi.interval.IntervalSet#addInterval(pipi.interval.Interval)
	 */
	@Override
	public void addInterval(Interval interval) {
		this.rangeSet.add(rangeFromInt(this.span.bound(interval)));
	}

	/* (non-Javadoc)
	 * @see pipi.interval.IntervalSet#removeInterval(pipi.interval.Interval)
	 */
	@Override
	public void removeInterval(Interval interval) {
		this.rangeSet.remove(rangeFromInt(this.span.bound(interval)));
	}

	/* (non-Javadoc)
	 * @see pipi.interval.IntervalSet#addAllRanges(pipi.interval.TreeIntervalSet)
	 */
	@Override
	public void addAllRanges(IntervalSet treeIntervalSets) {
		this.rangeSet.addAll(((TreeIntervalSet)treeIntervalSets).rangeSet);
	}

	/* (non-Javadoc)
	 * @see pipi.interval.IntervalSet#removeAllRanges(pipi.interval.TreeIntervalSet)
	 */
	@Override
	public void removeAllRanges(IntervalSet ranges) {
		this.rangeSet.removeAll(((TreeIntervalSet)ranges).rangeSet);
	
	}

	/* (non-Javadoc)
	 * @see pipi.interval.IntervalSet#getIntervals()
	 */
	@Override
	public List<Interval> getIntervals() {
		return intsFromRanges(this.rangeSet.asRanges());
	}
	
	/* (non-Javadoc)
	 * @see pipi.interval.IntervalSet#getContainedIntervals(pipi.interval.Interval)
	 */
	@Override
	public TreeIntervalSet getContainedIntervals(Interval verticalRange) {
		RangeSet<Integer> subRangeSet = internalGetIntervals(verticalRange);
		return new TreeIntervalSet(this.span,subRangeSet);
	}

	/* (non-Javadoc)
	 * @see pipi.interval.IntervalSet#getSubIntervals(pipi.interval.Interval)
	 */
	@Override
	public TreeIntervalSet getSubIntervals(Interval verticalRange) {
		return new TreeIntervalSet(this.span, TreeRangeSet.create(this.rangeSet.subRangeSet(rangeFromInt(verticalRange))));
	}
	
	/* (non-Javadoc)
	 * @see pipi.interval.IntervalSet#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return this.rangeSet.isEmpty();
	}

	/* (non-Javadoc)
	 * @see pipi.interval.IntervalSet#contains(pipi.interval.Interval)
	 */
	@Override
	public boolean contains(Interval verticalRange) {
		return this.rangeSet.encloses(rangeFromInt(verticalRange));
	}

	/* (non-Javadoc)
	 * @see pipi.interval.IntervalSet#complement()
	 */
	@Override
	public IntervalSet complement(){
		return new TreeIntervalSet(this.span, this.boundComplement());
	}


	
	
	private Range<Integer> rangeFromInt(Interval interval) {
		return Range.closedOpen(interval.getFrom(), interval.getTo());
	}


	private List<Interval> intsFromRanges(Set<Range<Integer>> asRanges) {
		List<Interval> intervals = Lists.newArrayList();
		for (Range<Integer> range : asRanges) {
			intervals.add(intFromRange(range));
		}
		return intervals;
	}

	private Interval intFromRange(Range<Integer> range) {
		int lowerEndpoint = range.lowerEndpoint();
		if (range.lowerBoundType() == BoundType.OPEN) {
			lowerEndpoint++;
		}
		Integer upperEndpoint = range.upperEndpoint();
		if (range.upperBoundType() == BoundType.CLOSED) {
			upperEndpoint++;
		}
		return new Interval(lowerEndpoint, upperEndpoint);
	}

	private RangeSet<Integer> boundComplement() {
		return this.rangeSet.complement().subRangeSet(Range.closedOpen(this.span.getFrom(), this.span.getTo()));
	}


	
	private RangeSet<Integer> internalGetIntervals(Interval verticalRange) {
		RangeSet<Integer> boundComplement = this.rangeSet.subRangeSet(rangeFromInt(this.span));
		Range<Integer> lowerRangeContaining = boundComplement.rangeContaining(verticalRange.getFrom());
		Range<Integer> rangeContaining = boundComplement.rangeContaining(verticalRange.getTo() - 1);
		if (lowerRangeContaining == null) {
			lowerRangeContaining = Range.closedOpen(verticalRange.getFrom(), verticalRange.getFrom() + 1);
		}
		if (rangeContaining == null) {
			rangeContaining = Range.closedOpen(verticalRange.getTo() - 1, verticalRange.getTo());
		}
		Range<Integer> span2 = lowerRangeContaining.span(rangeContaining);
		RangeSet<Integer> subRangeSet = boundComplement.subRangeSet(span2);
		return subRangeSet;
	}


	@Override
	public String toString() {
		return this.rangeSet.toString();
	}

}
