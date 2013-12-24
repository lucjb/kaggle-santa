package pipi.interval;

import java.util.List;
import java.util.Set;

import com.google.common.collect.BoundType;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;

public class Line {

	private RangeSet<Integer> rangeSet;
	private IntRange span;

	public Line(IntRange span) {
		this.span = span;
		this.rangeSet = TreeRangeSet.create();
	}

	public void addRange(IntRange intRange) {
		this.rangeSet.add(rangeFromInt(this.span.bound(intRange)));
	}

	private Range<Integer> rangeFromInt(IntRange intRange) {
		return Range.closedOpen(intRange.getFrom(), intRange.getTo());
	}

	public List<IntRange> getRanges() {
		return intsFromRanges(this.rangeSet.asRanges());
	}

	private List<IntRange> intsFromRanges(Set<Range<Integer>> asRanges) {
		List<IntRange> intRanges = Lists.newArrayList();
		for (Range<Integer> range : asRanges) {
			intRanges.add(intFromRange(range));
		}
		return intRanges;
	}

	private IntRange intFromRange(Range<Integer> range) {
		int lowerEndpoint = range.lowerEndpoint();
		if (range.lowerBoundType() == BoundType.OPEN) {
			lowerEndpoint++;
		}
		Integer upperEndpoint = range.upperEndpoint();
		if (range.upperBoundType() == BoundType.CLOSED) {
			upperEndpoint++;
		}
		return new IntRange(lowerEndpoint, upperEndpoint);
	}

	public IntRange getEmptyRange(int y) {
		RangeSet<Integer> complement = boundComplement();
		Range<Integer> rangeContaining = complement.rangeContaining(y);
		return intFromRange(rangeContaining);
	}

	private RangeSet<Integer> boundComplement() {
		return this.rangeSet.complement().subRangeSet(Range.closedOpen(this.span.getFrom(), this.span.getTo()));
	}

	public IntRange getSpan() {
		return this.span;
	}

	public List<IntRange> getEmptyRanges(IntRange verticalRange) {
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

	@Override
	public String toString() {
		return this.span + this.rangeSet.toString();
	}

	public Line copy() {
		Line line = new Line(this.span);
		line.rangeSet.addAll(this.rangeSet);
		return line;
	}

	public boolean intersects(Line rightLine) {
		RangeSet<Integer> subRangeSet = rightLine.rangeSet.subRangeSet(rangeFromInt(this.span));
		return !subRangeSet.isEmpty();
	}
}
