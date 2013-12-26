package pipi.interval;

import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang3.tuple.Pair;

import pipi.SleighSlice;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;

public class InvervalSleghSlice implements SleighSlice {

	private final int width;
	private final int height;
	private final TreeMap<Integer, SleighColumn> lefts = Maps.newTreeMap();
	private final TreeMap<Integer, SleighColumn> rights = Maps.newTreeMap();

	private InvervalSleghSlice(int width, int height) {
		this.width = width;
		this.height = height;
		this.lefts.put(0, new SleighColumn(new Interval(0, height)));
		this.rights.put(width, new SleighColumn(new Interval(0, height)));
	}

	@Override
	public boolean isFree(int x, int y, int dx, int dy) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void free(int x, int y, int dx, int dy) {
		// TODO Auto-generated method stub

	}

	@Override
	public void fill(int x, int y, int dx, int dy) {
		Interval verticalRange = new Interval(y, y + dy);
		Interval horizontalRange = new Interval(x, x + dx);

		addColumn(this.lefts, horizontalRange.getTo(), verticalRange);
		addColumn(this.rights, horizontalRange.getFrom(), verticalRange);
		destroyColumns(verticalRange, horizontalRange, this.lefts, 0);
		destroyColumns(verticalRange, horizontalRange, this.rights, 1);
	}

	private void destroyColumns(Interval verticalRange, Interval horizontalRange, TreeMap<Integer, SleighColumn> columns,
			int d) {
		Set<Entry<Integer, SleighColumn>> entrySet = columns.subMap(horizontalRange.getFrom() + d,
				horizontalRange.getTo() + d).entrySet();

		for (Iterator<Entry<Integer, SleighColumn>> iterator = entrySet.iterator(); iterator.hasNext();) {
			Entry<Integer, SleighColumn> entry = iterator.next();
			SleighColumn sleighColumn = entry.getValue();
			sleighColumn.destroy(verticalRange);
			if (sleighColumn.getRanges().isEmpty()) {
				iterator.remove();
			}
		}
	}

	private void addColumn(TreeMap<Integer, SleighColumn> side, int insertionPoint, Interval verticalRange) {
		SleighColumn sleighColumn = side.get(insertionPoint);
		if (sleighColumn == null) {
			sleighColumn = new SleighColumn(new Interval(0, this.height));

			//ARREGLAR!!!!!
			
			Entry<Integer, SleighColumn> leftColumn = leftWithLine(this.lefts, insertionPoint, verticalRange);
			
			Entry<Integer, SleighColumn> rightColumn = leftWithLine(this.rights, insertionPoint, verticalRange);
			
			if (rightColumn == null || leftColumn.getKey() >= rightColumn.getKey()) {// FIXME
																						// both
																						// null
				if (leftColumn.getKey() < insertionPoint) {
					SleighColumn leftColumnValue = leftColumn.getValue();
					List<Interval> ranges = leftColumnValue.getLines().getRanges(verticalRange);
					if (ranges.isEmpty()) {
						return;
					}
					sleighColumn.getLines().addAllRanges(ranges);
				} else {
					SleighColumn leftColumnValue = leftColumn.getValue();
					// FIXME maintain invariant in remove and add of column
					List<Interval> lineRanges = leftColumnValue.getLines().getRanges(verticalRange);
					sleighColumn.getLines().addAllRanges(lineRanges);
					sleighColumn.addInterval(verticalRange);//hack
					for (Interval interval : lineRanges) {
						List<Interval> ranges = leftColumnValue.getRanges().getRanges(interval);
						sleighColumn.removeLines(ranges);
					}
					if (sleighColumn.isEmpty()) {
						return;
					}
				}
			} else {
				SleighColumn rightColumnValue = rightColumn.getValue();
				// FIXME maintain invariant in remove and add of column
				List<Interval> lineRanges = rightColumnValue.getLines().getRanges(verticalRange);
				sleighColumn.getLines().addAllRanges(lineRanges);
				sleighColumn.addInterval(verticalRange);//hack
				for (Interval interval : lineRanges) {
					List<Interval> ranges = rightColumnValue.getRanges().getRanges(interval);
					sleighColumn.removeLines(ranges);
				}
				if (sleighColumn.isEmpty()) {
					return;
				}
			}
			side.put(insertionPoint, sleighColumn);

		}
		sleighColumn.addInterval(verticalRange);
	}

	private Entry<Integer, SleighColumn> leftWithLine(TreeMap<Integer, SleighColumn> columns, int insertionPoint,
			Interval verticalRange) {
		Entry<Integer, SleighColumn> leftColumn = columns.floorEntry(insertionPoint);
		while(leftColumn != null && leftColumn.getValue().getLines().getRanges(verticalRange).isEmpty()){
			leftColumn = columns.lowerEntry(leftColumn.getKey());
		}
		return leftColumn;
	}

	public Collection<Rectangle> getMaximumRectangles() {
		Collection<Rectangle> rectangles = Lists.newArrayList();
		Deque<StartLine> leftDeque = Queues.newArrayDeque();
		for (Entry<Integer, SleighColumn> leftEntry : this.lefts.entrySet()) {
			for (Interval line : leftEntry.getValue().getLines().getRanges()) {
				Interval bounderLine = line;
				leftDeque.addLast(new StartLine(leftEntry.getValue(), leftEntry.getKey(), leftEntry.getKey(), bounderLine));
			}
		}

		StartLine leftPair = leftDeque.pollFirst();
		while (leftPair != null) {
			Interval bounderLine = leftPair.getLine();

			Entry<Integer, SleighColumn> higherEntry = this.rights.higherEntry(leftPair.getStart());
			if (higherEntry != null) {
				SleighColumn rightColumn = higherEntry.getValue();
				List<Interval> lines = rightColumn.getLines().getRanges(bounderLine);

				for (Interval lineInterval : lines) { // maybe one line only..
					Interval bound = bounderLine.bound(lineInterval);// maybe
																		// aldope
					List<Interval> rightSides = rightColumn.getRanges().getRanges(bound);
					List<Interval> leftRanges = leftPair.getSleighColumn().getRanges().getRanges(bound);
					if (!rightSides.isEmpty() && !leftRanges.isEmpty()) {
						IntervalSet intervalSet = new IntervalSet(bound);
						intervalSet.addRange(bound);
						intervalSet.removeAllRanges(rightSides);
						rectangles.add(new Rectangle(new Interval(leftPair.getLeft(), higherEntry.getKey()), bound));
						List<Interval> ranges = intervalSet.getRanges();
						for (Interval interval : ranges) {
							Interval rebound = bound.bound(interval);
							if (!rebound.isEmpty()) {
								leftDeque.addLast(new StartLine(leftPair.getSleighColumn(), leftPair.getLeft(), higherEntry
										.getKey(), rebound));
							}
						}
					} else {
						if (!leftRanges.isEmpty()) {
							leftDeque.addLast(new StartLine(leftPair.getSleighColumn(), leftPair.getLeft(), higherEntry
									.getKey(), bound));
						}
					}
				}

			}
			leftPair = leftDeque.pollFirst();
		}

		return rectangles;
	}

	public static InvervalSleghSlice empty(int width, int height) {
		InvervalSleghSlice invervalSleghSlice = new InvervalSleghSlice(width, height);
		SleighColumn leftSleighColumn = invervalSleghSlice.lefts.lastEntry().getValue();
		leftSleighColumn.addLine(new Interval(0, height));
		leftSleighColumn.getRanges().addRange(new Interval(0, height));
		SleighColumn rightSleighColumn = invervalSleghSlice.rights.lastEntry().getValue();
		rightSleighColumn.addLine(new Interval(0, height));
		rightSleighColumn.getRanges().addRange(new Interval(0, height));
		return invervalSleghSlice;
	}

}
