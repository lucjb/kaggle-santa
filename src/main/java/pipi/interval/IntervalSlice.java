package pipi.interval;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import pipi.Box2d;
import pipi.Point2d;
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
		IntervalSet leftLines = this.buildLinesForIntervalSet(this.lefts, this.rights, leftInterval,
				horizontalRange.getFrom());
		if (!leftLines.contains(verticalRange)) {
			return false;
		}

		IntervalSet rightInterval = buildIntervalSet(this.height);
		rightInterval.addInterval(verticalRange);
		IntervalSet rightLines = this.buildLinesForIntervalSet(this.lefts, this.rights, rightInterval,
				horizontalRange.getTo() - 1);
		if (!rightLines.contains(verticalRange)) {
			return false;
		}

		return true;
	}

	@Override
	public void free(int x, int y, int dx, int dy) {
		Interval verticalRange = new Interval(y, y + dy);
		Interval horizontalRange = new Interval(x, x + dx);

		int insertionPoint = horizontalRange.getFrom();

		SliceColumn otherColumn = this.lefts.get(horizontalRange.getTo());
		IntervalSet sides = buildIntervalSet(this.height);
		if (otherColumn != null) {
			sides.addAllRanges(otherColumn.getSides());
		}
		BitIntervalSet freeSpace = removeColumn(this.lefts, this.rights, insertionPoint, verticalRange);

		Entry<Integer, SliceColumn> leftColumn = this.lefts.higherEntry(insertionPoint);
		Entry<Integer, SliceColumn> rightColumn = this.rights.ceilingEntry(insertionPoint);

		SliceColumn lastColumn = null;
		if (leftColumn != null) {
			if (leftColumn.getKey() > horizontalRange.getTo()) {
				leftColumn = null;
			}
		}

		if (rightColumn != null) {
			if (rightColumn.getKey() == horizontalRange.getTo()) {
				lastColumn = rightColumn.getValue();
			}
			if (rightColumn.getKey() >= horizontalRange.getTo()) {
				rightColumn = null;
			}
		}

		while ((leftColumn != null || rightColumn != null)) {
			boolean leftAdvance = false;
			boolean rightAdvance = false;
			if ((leftColumn != null) && (rightColumn == null || leftColumn.getKey() <= rightColumn.getKey())) {
				SliceColumn left = leftColumn.getValue();
				left.getLines().addInterval(verticalRange);
				freeSpace.addAllRanges(left.getSides()); // capaz solo los sides
				left.getSides().removeInterval(verticalRange);
				if (left.getSides().isEmpty()) {
					this.lefts.remove(leftColumn.getKey());
				}
				leftAdvance = true;

			}
			if ((rightColumn != null) && (leftColumn == null || rightColumn.getKey() <= leftColumn.getKey())) {
				SliceColumn right = rightColumn.getValue();
				right.getLines().addInterval(verticalRange);
				freeSpace.removeAllRanges(right.getSides());
				right.getSides().removeInterval(verticalRange);
				if (right.getSides().isEmpty()) {
					this.rights.remove(rightColumn.getKey());
				}
				rightAdvance = true;
			}

			if (leftAdvance && rightAdvance) {
				leftColumn.getValue().getLines().removeAllRanges(rightColumn.getValue().getSides());
				rightColumn.getValue().getLines().removeAllRanges(leftColumn.getValue().getSides());
			}

			if (leftAdvance) {
				leftColumn.getValue().removeEmptyLines();
				leftColumn = this.lefts.higherEntry(leftColumn.getKey());
				if (leftColumn != null) {
					if (leftColumn.getKey() > horizontalRange.getTo()) {
						leftColumn = null;
					}
				}
			}
			if (rightAdvance) {
				rightColumn.getValue().removeEmptyLines();
				rightColumn = this.rights.higherEntry(rightColumn.getKey());
				if (rightColumn != null) {
					if (rightColumn.getKey() == horizontalRange.getTo()) {
						lastColumn = rightColumn.getValue();
					}
					if (rightColumn.getKey() >= horizontalRange.getTo()) {
						rightColumn = null;
					}
				}
			}

		}
		if (freeSpace.contains(verticalRange)) {
			return;
		}


		IntervalSet toadd = buildIntervalSet(this.height);
		toadd.addInterval(verticalRange);
		toadd.removeAllRanges(freeSpace);
		freeSpace.addInterval(verticalRange);
		toadd.removeAllRanges(sides);
		if(toadd.isEmpty()){
			return;
		}
		if (lastColumn == null) {
			lastColumn = new SliceColumn(new Interval(0, this.height));
			this.rights.put(horizontalRange.getTo(), lastColumn);
		}
		lastColumn.getSides().addAllRanges(toadd);
		lastColumn.getLines().addAllRanges(freeSpace);
		lastColumn.removeEmptyLines();

		assert !lastColumn.isEmpty();

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

	private void emptyColumns(Interval verticalRange, Interval horizontalRange, TreeMap<Integer, SliceColumn> columns, int d) {
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

	private BitIntervalSet removeColumn(TreeMap<Integer, SliceColumn> side, TreeMap<Integer, SliceColumn> otherSide,
			int insertionPoint, Interval verticalRange) {

		IntervalSet treeIntervalSet = buildIntervalSet(this.height);
		treeIntervalSet.addInterval(Interval.of(0, this.height));

		BitIntervalSet freeSpace = (BitIntervalSet) buildLinesForIntervalSet(this.lefts, this.rights, treeIntervalSet,
				insertionPoint);

		if (freeSpace.contains(verticalRange)) {
			return freeSpace;
		}

		IntervalSet toadd = buildIntervalSet(this.height);
		toadd.addInterval(verticalRange);
		toadd.removeAllRanges(freeSpace);
		SliceColumn otherColumn = otherSide.get(insertionPoint);
		if (otherColumn != null) {
			toadd.removeAllRanges(otherColumn.getSides());
		}
		// freeSpace.addInterval(verticalRange);
		if (toadd.isEmpty()) {
			return freeSpace;
		}
		SliceColumn sliceColumn = side.get(insertionPoint);
		if (sliceColumn == null) {
			sliceColumn = new SliceColumn(new Interval(0, this.height));
			side.put(insertionPoint, sliceColumn);
		}
		sliceColumn.getLines().addAllRanges(freeSpace);
		sliceColumn.getLines().addInterval(verticalRange);
		sliceColumn.getSides().addAllRanges(toadd);
		sliceColumn.removeEmptyLines();

		assert !sliceColumn.isEmpty();
		return freeSpace;
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
			IntervalSet lines = buildLinesForIntervalSet(this.lefts, this.rights, treeIntervalSet, insertionPoint);
			sliceColumn = new SliceColumn(new Interval(0, this.height));
			sliceColumn.getLines().addAllRanges(lines);
			sliceColumn.addSide(verticalRange);
			if (!sliceColumn.getSides().isEmpty()) {
				side.put(insertionPoint, sliceColumn);
			}
			return;
		}

		if (!sliceColumn.getLines().contains(verticalRange)) {
			IntervalSet treeIntervalSet = buildIntervalSet(this.height);
			treeIntervalSet.addInterval(verticalRange);
			treeIntervalSet.removeAllRanges(sliceColumn.getLines());
			IntervalSet buildLinesForIntervalSet = this.buildLinesForIntervalSet(this.lefts, this.rights, treeIntervalSet,
					insertionPoint);
			sliceColumn.getLines().addAllRanges(buildLinesForIntervalSet);
		}
		sliceColumn.addSide(verticalRange);
	}

	private IntervalSet buildLinesForIntervalSet(TreeMap<Integer, SliceColumn> lefts, TreeMap<Integer, SliceColumn> rights,
			IntervalSet toAccount, int insertionPoint) {
		IntervalSet lines = buildIntervalSet(this.height);
		// should remove lines already accounted for?

		Entry<Integer, SliceColumn> leftColumn = lefts.floorEntry(insertionPoint);
		Entry<Integer, SliceColumn> rightColumn = rights.floorEntry(insertionPoint);
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
				leftColumn = lefts.lowerEntry(leftColumn.getKey());
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
				rightColumn = rights.lowerEntry(rightColumn.getKey());
			}
		}
		return lines;
	}

	public static IntervalSet buildIntervalSet(int height2) {
		// return new TreeIntervalSet(new Interval(0, height2));
		return new BitIntervalSet(height2);
	}

	public Collection<MaximumRectangle> getMaximumRectangles(PerimeterSlice perimeterSlice) {
		Collection<MaximumRectangle> maximumRectangles = Lists.newArrayList();
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
			int index = nextIndex(rightIndexes, start);
			while (index < rightIndexes.length) {
				SliceColumn rightColumn = rightColumns[index];
				if (rightIsNotEmpty(rightColumn, line, perimeterSlice, rightIndexes[index])) {
					Rectangle rectangle = Rectangle.of(left, line.getFrom(), rightIndexes[index] - left, line.length());

					maximumRectangles.add(new MaximumRectangle(rectangle, null));
					List<Interval> emptyPaths = emptyPaths(rightColumn.getSides(), line);
					Iterator<Interval> emptyPathsIterator = emptyPaths.iterator();
					if (emptyPathsIterator.hasNext()) {
						Interval path = emptyPathsIterator.next();
						while (emptyPathsIterator.hasNext()) {
							Interval next = emptyPathsIterator.next();
							if (isLeftEmpty(leftColumn, next, perimeterSlice, left)) {
								break;
							}
							leftDeque.addLast(new StartLine(leftColumn, left, rightIndexes[index], next));
						}
						line = path;
						start = rightIndexes[index];
						index++;
						if (isLeftEmpty(leftColumn, line, perimeterSlice, left)) {
							break;
						}
					} else {
						break;
					}
				} else {
					index++;
				}
			}
			leftPair = leftDeque.pollFirst();
		}

		return maximumRectangles;
	}

	private boolean rightIsNotEmpty(SliceColumn rightColumn, Interval bounderLine, PerimeterSlice perimeterSlice,
			int rightIndexes) {
		// return perimeterSlice.getRightPerimeter(rightIndexes, bounderLine) !=
		// 0;
		return rightColumn.getSides().isAnythingInside(bounderLine);
	}

	private boolean isLeftEmpty(SliceColumn sleighColumn, Interval bound, PerimeterSlice perimeterSlice, int left) {
		// return perimeterSlice.getLeftPerimeter(left, bound) != 0;
		IntervalSet sides = sleighColumn.getSides();
		return !sides.isAnythingInside(bound);
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

	public Perimeter getPerimeter(Point2d point2d, Box2d box2d, IntervalSlice perimeterSlice) {
		Interval line = Interval.of(point2d.y, point2d.y + box2d.dy);
		Interval hori = Interval.of(point2d.x, point2d.x + box2d.dx);

		IntervalSet perimeterLeft = getSideInterval(this.lefts, hori.getFrom(), line);
		IntervalSet perimeterRight = getSideInterval(this.rights, hori.getTo(), line);
		IntervalSet perimeterUp = getSideInterval(perimeterSlice.lefts, line.getFrom(), hori);
		IntervalSet perimeterDown = getSideInterval(perimeterSlice.rights, line.getTo(), hori);

		Perimeter perimeter = new Perimeter(perimeterLeft, perimeterRight, perimeterUp, perimeterDown);
		return perimeter;
	}

	private IntervalSet getSideInterval(TreeMap<Integer, SliceColumn> tree, int point, Interval line) {
		SliceColumn sliceColumn = tree.get(point);
		if (sliceColumn == null) {
			return new BitIntervalSet(1000);
		}
		return sliceColumn.getSides().getSubIntervals(line);
	}

	public int getPerimeterInt(Point2d point2d, Box2d box2d, IntervalSlice perimeterSlice) {
		Interval line = Interval.of(point2d.y, point2d.y + box2d.dy);
		Interval hori = Interval.of(point2d.x, point2d.x + box2d.dx);

		int perimeterLeft = getSideIntervalInt(this.lefts, hori.getFrom(), line);
		int perimeterRight = getSideIntervalInt(this.rights, hori.getTo(), line);
		int perimeterUp = getSideIntervalInt(perimeterSlice.lefts, line.getFrom(), hori);
		int perimeterDown = getSideIntervalInt(perimeterSlice.rights, line.getTo(), hori);
		return perimeterLeft + perimeterRight + perimeterUp + perimeterDown;
	}

	private int getSideIntervalInt(TreeMap<Integer, SliceColumn> tree, int point, Interval line) {
		SliceColumn sliceColumn = tree.get(point);
		if (sliceColumn == null) {
			return 0;
		}
		return ((BitIntervalSet) sliceColumn.getSides()).count(line);
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

	public static IntervalSlice filled(int width, int height) {
		IntervalSlice intervalSlice = new IntervalSlice(width, height);
		intervalSlice.lefts.clear();
		intervalSlice.rights.clear();
		return intervalSlice;
	}

	public boolean isEmpty() {
		if (this.lefts.size() != 1) {
			return false;
		}
		if (this.rights.size() != 1) {
			return false;
		}
		if (!checkColumn(this.lefts.get(0))) {
			return false;
		}

		if (!checkColumn(this.rights.get(this.width))) {
			return false;
		}

		return true;
	}

	private boolean checkColumn(SliceColumn sliceColumn) {
		if (sliceColumn == null) {
			return false;
		}
		if (!checkFullIntervalSet(sliceColumn.getLines())) {
			return false;
		}
		if (!checkFullIntervalSet(sliceColumn.getSides())) {
			return false;
		}
		return true;
	}

	private boolean checkFullIntervalSet(IntervalSet lines) {
		List<Interval> intervals = lines.getIntervals();
		if (intervals.size() != 1) {
			return false;
		}
		Interval interval = intervals.get(0);
		if (!interval.equals(Interval.of(0, this.height))) {
			return false;
		}
		return true;
	}

}
