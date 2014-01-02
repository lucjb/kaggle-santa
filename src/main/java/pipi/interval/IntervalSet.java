package pipi.interval;

import java.util.List;
import java.util.Set;

import com.google.common.collect.BoundType;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;

public class IntervalSet {

	private RangeSet<Integer> rangeSet;
	private Interval span;

	public IntervalSet(Interval span) {
		this.span = span;
		this.rangeSet = TreeRangeSet.create();
	}
	private IntervalSet(Interval span,RangeSet<Integer> rangeSet) {
		this.span = span;
		this.rangeSet = rangeSet;
	}

	
	public void addRange(Interval interval) {
		this.rangeSet.add(rangeFromInt(this.span.bound(interval)));
	}

	public void removeRange(Interval interval) {
		this.rangeSet.remove(rangeFromInt(this.span.bound(interval)));
	}

	private Range<Integer> rangeFromInt(Interval interval) {
		return Range.closedOpen(interval.getFrom(), interval.getTo());
	}

	public List<Interval> getRanges() {
		return intsFromRanges(this.rangeSet.asRanges());
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

	public Interval getEmptyRange(int y) {
		RangeSet<Integer> complement = boundComplement();
		Range<Integer> rangeContaining = complement.rangeContaining(y);
		return intFromRange(rangeContaining);
	}

	private RangeSet<Integer> boundComplement() {
		return this.rangeSet.complement().subRangeSet(Range.closedOpen(this.span.getFrom(), this.span.getTo()));
	}

	public Interval getSpan() {
		return this.span;
	}

	public List<Interval> getEmptyRanges(Interval verticalRange) {
		RangeSet<Integer> boundComplement = this.boundComplement();
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
		return intsFromRanges(subRangeSet.asRanges());
	}

	public IntervalSet getIntervals(Interval verticalRange) {
		RangeSet<Integer> subRangeSet = internalGetIntervals(verticalRange);
		return new IntervalSet(this.span,subRangeSet);
	}
	
	private List<Interval> getRanges(Interval verticalRange) {
		RangeSet<Integer> subRangeSet = internalGetIntervals(verticalRange);
		return intsFromRanges(subRangeSet.asRanges());
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

	public IntervalSet getIntervalsInRange(Interval verticalRange) {
		IntervalSet intervalSet = new IntervalSet(this.span);
		intervalSet.addAllRanges(this.getRanges(verticalRange));
		return intervalSet;
	}

	public Interval getContainingInterval(Interval interval){
		Range<Integer> rangeContaining = this.rangeSet.rangeContaining(interval.getFrom());
		if(rangeContaining == null){
			return null;
		}
		return intFromRange(rangeContaining);
	}
	@Override
	public String toString() {
		return this.rangeSet.toString();
	}

	public IntervalSet copy() {
		IntervalSet intervalSet = new IntervalSet(this.span);
		intervalSet.rangeSet.addAll(this.rangeSet);
		return intervalSet;
	}

	public boolean intersects(IntervalSet rightLine) {
		RangeSet<Integer> subRangeSet = rightLine.rangeSet.subRangeSet(rangeFromInt(this.span));
		return !subRangeSet.isEmpty();
	}

	public Interval getRange(int y) {
		Range<Integer> rangeContaining = this.rangeSet.rangeContaining(y);
		return intFromRange(rangeContaining);
	}

	public boolean isEmpty() {
		return this.rangeSet.isEmpty();
	}

	public void addAllRanges(IntervalSet intervalSets) {
		this.rangeSet.addAll(intervalSets.rangeSet);
	}

	private void addAllRanges(List<Interval> ranges) {
		for (Interval interval : ranges) {
			this.addRange(interval);
		}
	}

	private void removeAllRanges(List<Interval> ranges) {
		for (Interval interval : ranges) {
			this.removeRange(interval);
		}
	}

	public void removeAllRanges(IntervalSet ranges) {
		this.rangeSet.removeAll(ranges.rangeSet);
	}

	public boolean contains(Interval verticalRange) {
		return this.rangeSet.encloses(rangeFromInt(verticalRange));
	}

	public void boundear(IntervalSet lineRanges) {
		RangeSet<Integer> boundComplement = this.boundComplement();
		lineRanges.rangeSet.removeAll(boundComplement);
	}

	public IntervalSet complement(){
		return new IntervalSet(this.span, this.boundComplement());
	}
	public IntervalSet complement(Interval interval){
		RangeSet<Integer> subRangeSet = this.rangeSet.complement().subRangeSet(rangeFromInt(interval));
		return new IntervalSet(this.span, subRangeSet);
	}

}
