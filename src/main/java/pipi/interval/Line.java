package pipi.interval;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class Line {

	private TreeMap<Integer, IntRange> segments = Maps.newTreeMap();
	private IntRange range;

	public Line(IntRange empty) {
		this.range = empty;
	}

	public void addSegment(IntRange intRange) {
		IntRange boundIntRange = this.range.bound(intRange);
		//TODO OVERLAP!!!!
		this.segments.put(intRange.getFrom(), boundIntRange);
	}

	public IntRange getEmptyRange(int y) {
		int from = this.range.getFrom();
		int to = this.range.getTo();

		Entry<Integer, IntRange> floorEntry = this.segments.floorEntry(y);
		Entry<Integer, IntRange> ceilingEntry = this.segments
				.ceilingEntry(y);
		if (floorEntry != null) {
			from = floorEntry.getValue().getTo();
		}
		if (ceilingEntry != null) {
			to = ceilingEntry.getValue().getFrom();
		}
		return new IntRange(from, to);
	}

	public IntRange getRange() {
		return this.range;
	}

	public List<IntRange> getEmptyRanges(IntRange verticalRange) {
		List<IntRange> ranges = Lists.newArrayList();
		Entry<Integer, IntRange> lowFloorEntry = this.segments.floorEntry(verticalRange.getFrom());
		Entry<Integer, IntRange> lowCeilingEntry = this.segments.ceilingEntry(verticalRange.getFrom());
		Entry<Integer, IntRange> highFloorEntry = this.segments.floorEntry(verticalRange.getTo());
		Entry<Integer, IntRange> highCeilingEntry = this.segments.ceilingEntry(verticalRange.getTo());
		int low;
		if (lowFloorEntry == null) {
			ranges.add(new IntRange(this.range.getFrom(), lowCeilingEntry
					.getValue().getFrom()));
			low = lowCeilingEntry.getKey(); //this.range.from?
		} else {
			low = lowFloorEntry.getKey();
		}
		int high;
		IntRange last = null;
		if(highCeilingEntry == null){
			last = new IntRange(lowFloorEntry.getValue().getTo(), this.range.getTo());
			high = highFloorEntry.getValue().getTo(); //this.range.to?
		}else{
			high = highCeilingEntry.getValue().getFrom();
		}
		SortedMap<Integer, IntRange> subMap = this.segments.subMap(low, high);
		Set<Entry<Integer, IntRange>> entrySet = subMap.entrySet();
		
		Iterator<Entry<Integer, IntRange>> iterator = entrySet.iterator();
		Entry<Integer, IntRange> previous = iterator.next();
		while (iterator.hasNext()) {
			Entry<Integer, IntRange> entry = iterator.next();
			ranges.add(new IntRange(previous.getValue().getTo(),entry.getValue().getFrom() ));
			previous = entry;
		}
		if(last != null){
			ranges.add(last);
		}
		return ranges;
	}
}
