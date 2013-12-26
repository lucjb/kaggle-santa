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

	public List<Interval> getRanges(Interval verticalRange) {
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
		return intsFromRanges(subRangeSet.asRanges());
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
	public Interval getRange(int y){
		Range<Integer> rangeContaining = this.rangeSet.rangeContaining(y);
		return intFromRange(rangeContaining);
	}

	public boolean isEmpty() {
		return this.rangeSet.isEmpty();
	}

	public void addAllRanges(IntervalSet intervalSets) {
		this.rangeSet.addAll(intervalSets.rangeSet);
	}

	public void addAllRanges(List<Interval> ranges) {
		for (Interval interval : ranges) {
			this.addRange(interval);
		}
	}

	public void removeAllRanges(List<Interval> ranges) {
		for (Interval interval : ranges) {
			this.removeRange(interval);
		}
	}
	
	
}
