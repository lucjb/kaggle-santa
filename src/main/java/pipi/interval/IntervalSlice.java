package pipi.interval;

import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

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

	private void addColumn(TreeMap<Integer, SleighColumn> side, TreeMap<Integer, SleighColumn> otherSide,
			int insertionPoint, Interval verticalRange) {
		SleighColumn sleighColumn = side.get(insertionPoint);
		if (sleighColumn == null) {
			IntervalSet treeIntervalSet = buildIntervalSet(this.height);
			treeIntervalSet.addInterval(verticalRange);
			SleighColumn otherColumn = otherSide.get(insertionPoint);
			if (otherColumn != null) {
				treeIntervalSet.removeAllRanges(otherColumn.getSides());
			}
			IntervalSet lines = buildLinesForIntervalSet(treeIntervalSet, insertionPoint);
			sleighColumn = new SleighColumn(new Interval(0, this.height));
			sleighColumn.getLines().addAllRanges(lines);
			sleighColumn.addInterval(verticalRange);
			if (!sleighColumn.getSides().isEmpty()) {
				side.put(insertionPoint, sleighColumn);
			}
			return;
		}

		if (!sleighColumn.getLines().contains(verticalRange)) {
			IntervalSet treeIntervalSet = buildIntervalSet(height);
			treeIntervalSet.addInterval(verticalRange);
			treeIntervalSet.removeAllRanges(sleighColumn.getLines());
			IntervalSet buildLinesForIntervalSet = this.buildLinesForIntervalSet(treeIntervalSet, insertionPoint);
			sleighColumn.getLines().addAllRanges(buildLinesForIntervalSet);
		}
		sleighColumn.addInterval(verticalRange);
	}

	private IntervalSet buildLinesForIntervalSet(IntervalSet toAccount, int insertionPoint) {
		IntervalSet lines = buildIntervalSet(height);
		// should remove lines already accounted for?

		Entry<Integer, SleighColumn> leftColumn = this.lefts.floorEntry(insertionPoint);
		Entry<Integer, SleighColumn> rightColumn = this.rights.floorEntry(insertionPoint);
		while (!rightIsEmpty(toAccount) && (leftColumn != null || rightColumn != null)) {
			if ((leftColumn != null) && (rightColumn == null || leftColumn.getKey() >= rightColumn.getKey())) {
				SleighColumn leftColumnValue = leftColumn.getValue();
				List<Interval> rangesToAccount = toAccount.getIntervals();
				IntervalSet boundedLines = buildIntervalSet(this.height);
				for (Interval intervalToAccount : rangesToAccount) {
					IntervalSet lineRanges = leftColumnValue.getLines().getContainedIntervals(intervalToAccount);
					boundedLines.addAllRanges(lineRanges);
				}
				toAccount.removeAllRanges(boundedLines);
				lines.addAllRanges(boundedLines);
				leftColumn = this.lefts.lowerEntry(leftColumn.getKey());
			} else {
				SleighColumn rightColumnValue = rightColumn.getValue();
				List<Interval> rangesToAccount = toAccount.getIntervals();
				IntervalSet deltaAccount = buildIntervalSet(this.height);

				for (Interval intervalToAccount : rangesToAccount) {

					IntervalSet lineRanges = rightColumnValue.getLines().getContainedIntervals(intervalToAccount);
					IntervalSet emptyLines = buildIntervalSet(this.height);
					emptyLines.addAllRanges(rightColumnValue.getLines());
					emptyLines.removeAllRanges(rightColumnValue.getSides());
					IntervalSet emptyIntervals = emptyLines.getContainedIntervals(intervalToAccount);
					lines.addAllRanges(emptyIntervals);

					deltaAccount.addAllRanges(lineRanges);
				}
				toAccount.removeAllRanges(deltaAccount);
				rightColumn = this.rights.lowerEntry(rightColumn.getKey());
			}
		}
		return lines;
	}

	public static IntervalSet buildIntervalSet(int height2) {
//		return new TreeIntervalSet(new Interval(0, height2));
		return new BitIntervalSet(height2);
	}

	public Collection<MaximumRectangle> getMaximumRectangles() {
		Collection<MaximumRectangle> maximumRectangles = Lists.newArrayList();
		Deque<StartLine> leftDeque = Queues.newArrayDeque();
		for (Entry<Integer, SleighColumn> leftEntry : this.lefts.entrySet()) {
			for (Interval line : leftEntry.getValue().getLines().getIntervals()) {
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
				IntervalSet rightSides = getRightSides(rightColumn, bounderLine);
				if (!rightIsEmpty(rightSides)) {
					Interval bound = bounderLine;
					boolean leftEmpty = isLeftEmpty(leftPair, bound);
					if (!rightEmpty(rightSides) && !leftEmpty) {
						List<Interval> //ranges = getComplementRanges1(rightSides, bound);
						ranges = getComplementRanges2(rightSides, bound);
						maximumRectangles.add(new MaximumRectangle(new Interval(leftPair.getLeft(), higherEntry.getKey()),
								bound));
						for (Interval interval : ranges) {
							Interval rebound = bound.bound(interval);
							if (!rebound.isEmpty()) {
								leftDeque.addLast(new StartLine(leftPair.getSleighColumn(), leftPair.getLeft(), higherEntry
										.getKey(), rebound));
							}
						}
					} else {
						if (!leftEmpty) {
							leftDeque.addLast(new StartLine(leftPair.getSleighColumn(), leftPair.getLeft(), higherEntry
									.getKey(), bound));
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

	private boolean isLeftEmpty(StartLine leftPair, Interval bound) {
//		IntervalSet leftSides = leftPair.getSleighColumn().getSides().getSubIntervals(bound);
//		boolean orig = leftSides.isEmpty();
		IntervalSet sides = leftPair.getSleighColumn().getSides();
		boolean opt = !sides.isAnythingInside(bound);
//		if(opt != orig){
//			System.out.println("Todo mal");
//		}
		return opt;
	}

	private List<Interval> getComplementRanges2(IntervalSet rightSides, Interval bound) {
		IntervalSet treeIntervalSet = buildIntervalSet(this.height);
		treeIntervalSet.addInterval(bound);
		treeIntervalSet.removeAllRanges(rightSides);
		List<Interval> ranges = treeIntervalSet.getIntervals();
		return ranges;
	}

	private boolean rightIsEmpty(IntervalSet rightSides) {
		return rightSides.isEmpty();
	}

	private boolean rightEmpty(IntervalSet rightSides) {
		return rightIsEmpty(rightSides);
	}

	private IntervalSet getRightSides(SleighColumn rightColumn, Interval bound) {
		return rightColumn.getSides().getSubIntervals(bound);
	}

	public static IntervalSlice empty(int width, int height) {
		IntervalSlice intervalSlice = new IntervalSlice(width, height);
		SleighColumn leftSleighColumn = intervalSlice.lefts.lastEntry().getValue();
		leftSleighColumn.addLine(new Interval(0, height));
		leftSleighColumn.getSides().addInterval(new Interval(0, height));
		SleighColumn rightSleighColumn = intervalSlice.rights.lastEntry().getValue();
		rightSleighColumn.addLine(new Interval(0, height));
		rightSleighColumn.getSides().addInterval(new Interval(0, height));
		return intervalSlice;
	}

}
