package pipi.interval;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class Line {

	private TreeMap<Integer, IntRange> ranges = Maps.newTreeMap();
	private IntRange span;

	public Line(IntRange span) {
		this.span = span;
	}

	public void addRange(IntRange intRange) {
		intRange = this.span.bound(intRange);
		int from;
		Entry<Integer, IntRange> floorEntry = this.ranges.floorEntry(intRange
				.getFrom());
		if (floorEntry != null) {
			IntRange value = floorEntry.getValue();
			if (intRange.getFrom() <= value.getTo()) {
				from = value.getFrom();
			} else {
				from = intRange.getFrom();
			}
		} else {
			from = intRange.getFrom();
		}
		int to;
		Entry<Integer, IntRange> lastEntry = this.ranges.floorEntry(intRange
				.getTo());
		if (lastEntry != null) {
			to = Math.max(lastEntry.getValue().getTo(), intRange.getTo());
		} else {
			to = intRange.getTo();
		}

		this.ranges.subMap(from, to).clear();
		;
		this.ranges.put(from, new IntRange(from, to));
	}

	public Collection<IntRange> getRanges() {
		return this.ranges.values();
	}

	public IntRange getEmptyRange(int y) {
		int from = this.span.getFrom();
		int to = this.span.getTo();

		Entry<Integer, IntRange> floorEntry = this.ranges.floorEntry(y);
		Entry<Integer, IntRange> ceilingEntry = this.ranges.ceilingEntry(y);
		if (floorEntry != null) {
			from = floorEntry.getValue().getTo();
		}
		if (ceilingEntry != null) {
			to = ceilingEntry.getValue().getFrom();
		}
		return new IntRange(from, to);
	}

	public IntRange getSpan() {
		return this.span;
	}

	public List<IntRange> getEmptyRanges(IntRange verticalRange) {
		if (this.ranges.isEmpty()) {
			return Collections.singletonList(this.span);
		}
		List<IntRange> ranges = Lists.newArrayList();
		Entry<Integer, IntRange> lowFloorEntry = this.ranges
				.floorEntry(verticalRange.getFrom());
		Entry<Integer, IntRange> lowCeilingEntry = this.ranges
				.ceilingEntry(verticalRange.getFrom());
		Entry<Integer, IntRange> highFloorEntry = this.ranges
				.floorEntry(verticalRange.getTo());
		Entry<Integer, IntRange> highCeilingEntry = this.ranges
				.ceilingEntry(verticalRange.getTo());
		int low;
		if (lowFloorEntry == null) {
			ranges.add(new IntRange(this.span.getFrom(), lowCeilingEntry
					.getValue().getFrom()));
			low = lowCeilingEntry.getKey(); // this.range.from?
		} else {
			low = lowFloorEntry.getKey();
		}
		int high;
		IntRange last = null;
		if(highFloorEntry != null){
			if(verticalRange.getTo() <= highFloorEntry.getValue().getTo()){
				high = highFloorEntry.getValue().getTo();
			}else{
				if(highCeilingEntry != null){
					high = highCeilingEntry.getValue().getTo();
				}else{
					last = new IntRange(highFloorEntry.getValue().getTo(),
							this.span.getTo());
					high = highFloorEntry.getValue().getTo(); // this.range.to?
				}
			}
		}else{
			high = low;
		}

		SortedMap<Integer, IntRange> subMap = this.ranges.subMap(low, high);
		Set<Entry<Integer, IntRange>> entrySet = subMap.entrySet();

		Iterator<Entry<Integer, IntRange>> iterator = entrySet.iterator();
		if (iterator.hasNext()) {
			Entry<Integer, IntRange> previous = iterator.next();
			while (iterator.hasNext()) {
				Entry<Integer, IntRange> entry = iterator.next();
				ranges.add(new IntRange(previous.getValue().getTo(), entry
						.getValue().getFrom()));
				previous = entry;
			}
		}
		if (last != null && !last.isEmpty()) {
			ranges.add(last);
		}
		return ranges;
	}
}
