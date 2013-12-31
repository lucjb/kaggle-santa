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

import pipi.Slice;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;

public class IntervalSlice implements Slice {

	private final int width;
	private final int height;
	private final TreeMap<Integer, SleighColumn> lefts = Maps.newTreeMap();
	private final TreeMap<Integer, SleighColumn> rights = Maps.newTreeMap();

	private IntervalSlice(int width, int height) {
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

		addColumn(this.lefts, this.rights, horizontalRange.getTo(), verticalRange);
		addColumn(this.rights, this.lefts, horizontalRange.getFrom(), verticalRange);
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
			if (sleighColumn.getSides().isEmpty()) {
				iterator.remove();
			}
		}
	}

	private void addColumn(TreeMap<Integer, SleighColumn> side, TreeMap<Integer, SleighColumn> otherSide, int insertionPoint, Interval verticalRange) {
		SleighColumn sleighColumn = side.get(insertionPoint);
		if (sleighColumn == null) {
			IntervalSet intervalSet = new IntervalSet(new Interval(0, this.height));
			intervalSet.addRange(verticalRange);
			SleighColumn otherColumn = otherSide.get(insertionPoint);
			if(otherColumn!= null){
				intervalSet.removeAllRanges(otherColumn.getSides());
			}
			IntervalSet lines = buildLinesForIntervalSet(intervalSet, insertionPoint);
			sleighColumn = new SleighColumn(new Interval(0, this.height));
			sleighColumn.getLines().addAllRanges(lines);
			sleighColumn.addInterval(verticalRange);
			if(!sleighColumn.getSides().isEmpty()){
				side.put(insertionPoint, sleighColumn);
			}
			return;
		}

		if (!sleighColumn.getLines().contains(verticalRange)) {
			IntervalSet intervalSet = new IntervalSet(new Interval(0, this.height));
			intervalSet.addRange(verticalRange);
			intervalSet.removeAllRanges(sleighColumn.getLines());
			IntervalSet buildLinesForIntervalSet = this.buildLinesForIntervalSet(intervalSet, insertionPoint);
			sleighColumn.getLines().addAllRanges(buildLinesForIntervalSet);
		}
		sleighColumn.addInterval(verticalRange);
	}

	private IntervalSet buildLinesForInterval(Interval verticalRange, int insertionPoint) {
		IntervalSet toAccount = new IntervalSet(new Interval(0, this.height));
		toAccount.addRange(verticalRange);
		return buildLinesForIntervalSet(toAccount, insertionPoint);
	}

	private IntervalSet buildLinesForIntervalSet(IntervalSet toAccount, int insertionPoint) {
		IntervalSet lines = new IntervalSet(new Interval(0, this.height));
		// should remove lines already accounted for?

		Entry<Integer, SleighColumn> leftColumn = this.lefts.floorEntry(insertionPoint);
		Entry<Integer, SleighColumn> rightColumn = this.rights.floorEntry(insertionPoint);
		while (!toAccount.isEmpty() && (leftColumn != null || rightColumn != null)) {
			if ((leftColumn != null) && (rightColumn == null || leftColumn.getKey() > rightColumn.getKey())) {
				SleighColumn leftColumnValue = leftColumn.getValue();
				List<Interval> rangesToAccount = toAccount.getRanges();
				IntervalSet boundedLines = new IntervalSet(new Interval(0, this.height));
				for (Interval intervalToAccount : rangesToAccount) {
					IntervalSet lineRanges = leftColumnValue.getLines().getIntervalsInRange(intervalToAccount);
					boundedLines.addAllRanges(lineRanges);
				}
				toAccount.removeAllRanges(boundedLines);
				lines.addAllRanges(boundedLines);
				leftColumn = this.lefts.lowerEntry(leftColumn.getKey());
			} else {
				SleighColumn rightColumnValue = rightColumn.getValue();
				List<Interval> rangesToAccount = toAccount.getRanges();
				IntervalSet deltaAccount = new IntervalSet(new Interval(0, this.height));


				for (Interval intervalToAccount : rangesToAccount) {

					IntervalSet lineRanges = rightColumnValue.getLines().getIntervalsInRange(intervalToAccount);
					IntervalSet emptyLines = new IntervalSet(new Interval(0, this.height));
					emptyLines.addAllRanges(rightColumnValue.getLines());
					emptyLines.removeAllRanges(rightColumnValue.getSides());
					IntervalSet emptyIntervals = emptyLines.getIntervals(intervalToAccount);
					lines.addAllRanges(emptyIntervals);
					
					deltaAccount.addAllRanges(lineRanges);
				}
				toAccount.removeAllRanges(deltaAccount);
				rightColumn = this.rights.lowerEntry(rightColumn.getKey());
			}
		}
		return lines;
	}

	public Collection<MaximumRectangle> getMaximumRectangles() {
		Collection<MaximumRectangle> maximumRectangles = Lists.newArrayList();
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
				IntervalSet lines = getRanges1(bounderLine, rightColumn);
				if(lines.getRanges().size()>1){
					throw new RuntimeException("GUARDA 1");
				}
				if (!lines.isEmpty()) {
					for (Interval lineInterval : lines.getRanges()) {
						Interval bound = bounderLine.bound(lineInterval);
						if(!bound.equals(bounderLine)){
							throw new RuntimeException("GUARDA 2");
						}
						IntervalSet rightSides = getRanges2(rightColumn, bound);
						IntervalSet leftSides = getRanges3(leftPair, bound);
						if (!rightSides.isEmpty() && !leftSides.isEmpty()) {
							IntervalSet intervalSet = new IntervalSet(bound);
							intervalSet.addRange(bound);
							intervalSet.removeAllRanges(rightSides);
							maximumRectangles.add(new MaximumRectangle(new Interval(leftPair.getLeft(), higherEntry.getKey()), bound));
							List<Interval> ranges = intervalSet.getRanges();
							for (Interval interval : ranges) {
								Interval rebound = bound.bound(interval);
								if (!rebound.isEmpty()) {
									leftDeque.addLast(new StartLine(leftPair.getSleighColumn(), leftPair.getLeft(),
											higherEntry.getKey(), rebound));
								}
							}
						} else {
							if (!leftSides.isEmpty()) {
								leftDeque.addLast(new StartLine(leftPair.getSleighColumn(), leftPair.getLeft(), higherEntry
										.getKey(), bound));
							}
						}
					}
				} else {
					leftDeque.addLast(new StartLine(leftPair.getSleighColumn(), leftPair.getLeft(), higherEntry.getKey(),
							bounderLine));
				}

			}
			leftPair = leftDeque.pollFirst();
		}

		return maximumRectangles;
	}

	private IntervalSet getRanges3(StartLine leftPair, Interval bound) {
		return leftPair.getSleighColumn().getSides().getIntervals(bound);
	}

	private IntervalSet getRanges2(SleighColumn rightColumn, Interval bound) {
		return rightColumn.getSides().getIntervals(bound);
	}

	private IntervalSet getRanges1(Interval bounderLine, SleighColumn rightColumn) {
		return rightColumn.getLines().getIntervals(bounderLine);
	}

	public static IntervalSlice empty(int width, int height) {
		IntervalSlice intervalSlice = new IntervalSlice(width, height);
		SleighColumn leftSleighColumn = intervalSlice.lefts.lastEntry().getValue();
		leftSleighColumn.addLine(new Interval(0, height));
		leftSleighColumn.getSides().addRange(new Interval(0, height));
		SleighColumn rightSleighColumn = intervalSlice.rights.lastEntry().getValue();
		rightSleighColumn.addLine(new Interval(0, height));
		rightSleighColumn.getSides().addRange(new Interval(0, height));
		return intervalSlice;
	}

}
