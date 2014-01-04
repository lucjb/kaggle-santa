package pipi.interval;

import java.util.Arrays;
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
	private final TreeMap<Integer, SliceColumn> lefts = Maps.newTreeMap();
	private final TreeMap<Integer, SliceColumn> rights = Maps.newTreeMap();

	private IntervalSlice(int width, int height) {
		this.width = width;
		this.height = height;
		this.lefts.put(0, new SliceColumn(new Interval(0, height)));
		this.rights.put(width, new SliceColumn(new Interval(0, height)));
	}

	@Override
	public boolean isFree(int x, int y, int dx, int dy) {
		Interval verticalRange = new Interval(y, y + dy);
		Interval horizontalRange = new Interval(x, x + dx);

		Set<Entry<Integer, SliceColumn>> entrySet = this.lefts
				.subMap(horizontalRange.getFrom() + 1, horizontalRange.getTo()).entrySet();
		for (Iterator<Entry<Integer, SliceColumn>> iterator = entrySet.iterator(); iterator.hasNext();) {
			Entry<Integer, SliceColumn> entry = iterator.next();
			SliceColumn sliceColumn = entry.getValue();
			if (sliceColumn.getSides().isAnythingInside(verticalRange)) {
				return false;
			}
		}

		Set<Entry<Integer, SliceColumn>> rightSet = this.rights.subMap(horizontalRange.getFrom() + 1,
				horizontalRange.getTo()).entrySet();
		for (Iterator<Entry<Integer, SliceColumn>> iterator = rightSet.iterator(); iterator.hasNext();) {
			Entry<Integer, SliceColumn> entry = iterator.next();
			SliceColumn sliceColumn = entry.getValue();
			if (sliceColumn.getSides().isAnythingInside(verticalRange)) {
				return false;
			}
		}
		
		IntervalSet leftInterval = buildIntervalSet(this.height);
		leftInterval.addInterval(verticalRange);
		IntervalSet leftLines = this.buildLinesForIntervalSet(leftInterval, horizontalRange.getFrom());
		if (!leftLines.contains(verticalRange)) {
			return false;
		}

		IntervalSet rightInterval = buildIntervalSet(this.height);
		rightInterval.addInterval(verticalRange);
		IntervalSet rightLines = this.buildLinesForIntervalSet(rightInterval, horizontalRange.getTo() - 1);
		if (!rightLines.contains(verticalRange)) {
			return false;
		}

		return true;
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

	private void destroyColumns(Interval verticalRange, Interval horizontalRange, TreeMap<Integer, SliceColumn> columns,
			int d) {
		Set<Entry<Integer, SliceColumn>> entrySet = columns.subMap(horizontalRange.getFrom() + d,
				horizontalRange.getTo() + d).entrySet();

		for (Iterator<Entry<Integer, SliceColumn>> iterator = entrySet.iterator(); iterator.hasNext();) {
			Entry<Integer, SliceColumn> entry = iterator.next();
			SliceColumn sliceColumn = entry.getValue();
			sliceColumn.destroy(verticalRange);
			if (sliceColumn.getSides().isEmpty()) {
				iterator.remove();
			}
		}
	}

	private void addColumn(TreeMap<Integer, SliceColumn> side, TreeMap<Integer, SliceColumn> otherSide, int insertionPoint,
			Interval verticalRange) {
		SliceColumn sliceColumn = side.get(insertionPoint);
		if (sliceColumn == null) {
			IntervalSet treeIntervalSet = buildIntervalSet(this.height);
			treeIntervalSet.addInterval(verticalRange);
			SliceColumn otherColumn = otherSide.get(insertionPoint);
			if (otherColumn != null) {
				treeIntervalSet.removeAllRanges(otherColumn.getSides());
			}
			IntervalSet lines = buildLinesForIntervalSet(treeIntervalSet, insertionPoint);
			sliceColumn = new SliceColumn(new Interval(0, this.height));
			sliceColumn.getLines().addAllRanges(lines);
			sliceColumn.addInterval(verticalRange);
			if (!sliceColumn.getSides().isEmpty()) {
				side.put(insertionPoint, sliceColumn);
			}
			return;
		}

		if (!sliceColumn.getLines().contains(verticalRange)) {
			IntervalSet treeIntervalSet = buildIntervalSet(this.height);
			treeIntervalSet.addInterval(verticalRange);
			treeIntervalSet.removeAllRanges(sliceColumn.getLines());
			IntervalSet buildLinesForIntervalSet = this.buildLinesForIntervalSet(treeIntervalSet, insertionPoint);
			sliceColumn.getLines().addAllRanges(buildLinesForIntervalSet);
		}
		sliceColumn.addInterval(verticalRange);
	}

	private IntervalSet buildLinesForIntervalSet(IntervalSet toAccount, int insertionPoint) {
		IntervalSet lines = buildIntervalSet(this.height);
		// should remove lines already accounted for?

		Entry<Integer, SliceColumn> leftColumn = this.lefts.floorEntry(insertionPoint);
		Entry<Integer, SliceColumn> rightColumn = this.rights.floorEntry(insertionPoint);
		while (!rightIsEmpty(toAccount) && (leftColumn != null || rightColumn != null)) {
			if ((leftColumn != null) && (rightColumn == null || leftColumn.getKey() >= rightColumn.getKey())) {
				SliceColumn leftColumnValue = leftColumn.getValue();
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
				SliceColumn rightColumnValue = rightColumn.getValue();
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
		// return new TreeIntervalSet(new Interval(0, height2));
		return new BitIntervalSet(height2);
	}

	public Collection<Rectangle> getMaximumRectangles() {
		Collection<Rectangle> maximumRectangles = Lists.newArrayList();
		Deque<StartLine> leftDeque = Queues.newArrayDeque();

		Set<Entry<Integer, SliceColumn>> entrySet = this.rights.entrySet();
		int[] rightIndexes = new int[entrySet.size()];
		SliceColumn[] rightColumns = new SliceColumn[entrySet.size()];
		Iterator<Entry<Integer, SliceColumn>> iterator = entrySet.iterator();
		for (int i = 0; i < rightColumns.length; i++) {
			Entry<Integer, SliceColumn> entry = iterator.next();
			rightColumns[i] = entry.getValue();
			rightIndexes[i] = entry.getKey();
		}

		for (Entry<Integer, SliceColumn> leftEntry : this.lefts.entrySet()) {
			for (Interval line : leftEntry.getValue().getLines().getIntervals()) {
				leftDeque.addLast(new StartLine(leftEntry.getValue(), leftEntry.getKey(), leftEntry.getKey(), line));
			}
		}

		StartLine leftPair = leftDeque.pollFirst();
		while (leftPair != null) {
			SliceColumn leftColumn = leftPair.getSleighColumn();
			int left = leftPair.getLeft();
			Interval line = leftPair.getLine();
			int start = leftPair.getStart();
			// if (!isLeftEmpty(leftColumn, line)) {
			int index = nextIndex(rightIndexes, start);
			while (index < rightIndexes.length) {
				SliceColumn rightColumn = rightColumns[index];
				if (rightIsNotEmpty(rightColumn, line)) {
					maximumRectangles.add(Rectangle.of(left, line.getFrom(), rightIndexes[index] - left, line.length()));
					List<Interval> emptyPaths = emptyPaths(rightColumn.getSides(), line);
					Iterator<Interval> emptyPathsIterator = emptyPaths.iterator();
					if (emptyPathsIterator.hasNext()) {
						Interval path = emptyPathsIterator.next();
						while (emptyPathsIterator.hasNext()) {
							Interval next = emptyPathsIterator.next();
							if (isLeftEmpty(leftColumn, next)) {
								break;
							}
							leftDeque.addLast(new StartLine(leftColumn, left, rightIndexes[index], next));
						}
						line = path;
						start = rightIndexes[index];
						index++;
						if (isLeftEmpty(leftColumn, line)) {
							break;
						}
					} else {
						nothing();
						break;
					}
				} else {
					index++;
				}
			}
			// }
			leftPair = leftDeque.pollFirst();
		}

		return maximumRectangles;
	}

	private void nothing() {
		// TODO Auto-generated method stub

	}

	private boolean rightIsNotEmpty(SliceColumn rightColumn, Interval bounderLine) {
		return rightColumn.getSides().isAnythingInside(bounderLine);
	}

	private int nextIndex(int[] rightIndexes, int start) {
		int binarySearch = Arrays.binarySearch(rightIndexes, start);
		if (binarySearch >= 0) {
			binarySearch++;
		} else {
			binarySearch = -binarySearch - 1;
		}
		return binarySearch;
	}

	private boolean isLeftEmpty(SliceColumn sleighColumn, Interval bound) {
		IntervalSet sides = sleighColumn.getSides();
		return !sides.isAnythingInside(bound);
	}

	private List<Interval> emptyPaths(IntervalSet rightSides, Interval bound) {
		// List<Interval> safeGetEmptyIntervals =
		// safeGetEmptyIntervals(rightSides, bound);
		// assert safeGetEmptyIntervals.equals(oldIntervals(rightSides, bound));
		// assert safeGetEmptyIntervals.equals(optimizedEmptyPaths(rightSides,
		// bound));
		// return safeGetEmptyIntervals;
		// List<Interval> ranges = treeIntervalSet.getIntervals();
		// return ranges;
		// List<Interval> intervals = oldIntervals(rightSides, bound);
		// List<Interval> complementaryIntervals = newIntervals(rightSides,
		// bound);
		// if(!intervals.equals(complementaryIntervals)) {
		// System.out.println("NAH");
		// }
		return optimizedEmptyPaths(rightSides, bound);
	}

	private List<Interval> safeGetEmptyIntervals(IntervalSet rightSides, Interval bound) {
		IntervalSet treeIntervalSet = buildIntervalSet(this.height);
		treeIntervalSet.addInterval(bound);
		treeIntervalSet.removeAllRanges(rightSides);
		return treeIntervalSet.getIntervals();
	}

	private List<Interval> optimizedEmptyPaths(IntervalSet rightSides, Interval bound) {
		BitIntervalSet bit = (BitIntervalSet) rightSides;
		bit.complement();
		List<Interval> fastIntervals = bit.fastIntervals(bound);
		bit.complement();
		return fastIntervals;
	}

	private List<Interval> oldIntervals(IntervalSet rightSides, Interval bound) {
		BitIntervalSet complement = (BitIntervalSet) rightSides.getComplement();
		complement.fastSubIntervals(bound);
		List<Interval> intervals = complement.getIntervals();
		return intervals;
	}

	private boolean rightIsEmpty(IntervalSet rightSides) {
		return rightSides.isEmpty();
	}

	public static IntervalSlice empty(int width, int height) {
		IntervalSlice intervalSlice = new IntervalSlice(width, height);
		SliceColumn leftSleighColumn = intervalSlice.lefts.lastEntry().getValue();
		leftSleighColumn.addLine(new Interval(0, height));
		leftSleighColumn.getSides().addInterval(new Interval(0, height));
		SliceColumn rightSleighColumn = intervalSlice.rights.lastEntry().getValue();
		rightSleighColumn.addLine(new Interval(0, height));
		rightSleighColumn.getSides().addInterval(new Interval(0, height));
		return intervalSlice;
	}

}
