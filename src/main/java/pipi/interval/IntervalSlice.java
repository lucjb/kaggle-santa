package pipi.interval;

import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import pipi.Slice;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;

public class IntervalSlice implements Slice {

	private final int width;
	private final int height;
	private final TreeMap<Integer, SliceColumn> columns = Maps.newTreeMap();

	private IntervalSlice(int width, int height) {
		this.width = width;
		this.height = height;
		this.columns.put(0, newColumn());
		this.columns.put(width, newColumn());
	}

	private SliceColumn newColumn() {
		return new SliceColumn(newIntervalSet(), newIntervalSet());
	}

	@Override
	public boolean isFree(int x, int y, int dx, int dy) {
		Interval verticalRange = new Interval(y, y + dy);
		Interval horizontalRange = new Interval(x, x + dx);

		IntervalSet freeSpace = calculateFreeSpace(horizontalRange.getFrom());

		if(!freeSpace.contains(verticalRange)){
			return false;
		}
		
		SortedMap<Integer, SliceColumn> subMap = this.columns.subMap(horizontalRange.getFrom()+1, horizontalRange.getTo());
		for (Entry<Integer, SliceColumn> entry : subMap.entrySet()) {
			updateFreeSpace(freeSpace, entry.getValue());
			if(!freeSpace.contains(verticalRange)){
				return false;
			}
		}
		return true;
	}

	public boolean nothingIsFree(int x, int y, int dx, int dy) {
		Interval verticalInterval = new Interval(y, y + dy);
		Interval horizontalRange = new Interval(x, x + dx);

		IntervalSet freeSpace = calculateFreeSpace(horizontalRange.getFrom());

		if(freeSpace.isAnythingInside(verticalInterval)){
			return false;
		}
		
		SortedMap<Integer, SliceColumn> subMap = this.columns.subMap(horizontalRange.getFrom()+1, horizontalRange.getTo());
		for (Entry<Integer, SliceColumn> entry : subMap.entrySet()) {
			freeSpace.addAllRanges(entry.getValue().getLeft());
			freeSpace.removeAllRanges(entry.getValue().getLeft());
			if(freeSpace.isAnythingInside(verticalInterval)){
				return false;
			}
		}
		return true;
	}

	
	private IntervalSet calculateFreeSpace(int from) {
		SortedMap<Integer, SliceColumn> subColumns = this.columns.subMap(0, from+1);

		IntervalSet freeSpace = newIntervalSet();
		
		for (Entry<Integer, SliceColumn> entry : subColumns.entrySet()) {
			SliceColumn sliceColumn = entry.getValue();
			updateFreeSpace(freeSpace, sliceColumn);
		}
		return freeSpace;
	}

	public void updateFreeSpace(IntervalSet freeSpace, SliceColumn sliceColumn) {
		IntervalSet left = sliceColumn.getLeft();
		if(!left.isEmpty()) {
			freeSpace.addAllRanges(left);
		}
		IntervalSet right = sliceColumn.getRight();
		if(!right.isEmpty()) {
			freeSpace.removeAllRanges(right);
		}
	}

	
	private IntervalSet newIntervalSet() {
		return buildIntervalSet(this.height);
	}

	@Override
	public void free(int x, int y, int dx, int dy) {
		assert nothingIsFree(x, y, dx, dy);
		Interval verticalRange = new Interval(y, y + dy);
		Interval horizontalRange = new Interval(x, x + dx);

		int leftInsertionPoint = horizontalRange.getFrom();
		int rightInsertionPoint = horizontalRange.getTo();

		SliceColumn leftColumn = getOrAddColumn(leftInsertionPoint);
		SliceColumn rightColumn = getOrAddColumn(rightInsertionPoint);

		IntervalSet prevLeft = null;
		IntervalSet prevRight = null;

		assert nothing(prevLeft = leftColumn.getRight().copy());
		assert nothing(prevRight = rightColumn.getLeft().copy());

		updateColumns(verticalRange, leftColumn, rightColumn);
		removeColumnsIfEmpty(leftInsertionPoint, rightInsertionPoint, leftColumn, rightColumn);

		assert leftColumn.isClean();
		assert rightColumn.isClean();
		assert prevLeft == null || SliceColumn.disjoint(leftColumn.getLeft(), prevLeft); 
		assert prevRight == null || SliceColumn.disjoint(rightColumn.getRight(), prevRight); 

	}

	public void removeColumnsIfEmpty(int leftInsertionPoint, int rightInsertionPoint, SliceColumn leftColumn,
			SliceColumn rightColumn) {
		if(leftColumn.isEmpty()){
			this.columns.remove(leftInsertionPoint);
		}

		if(rightColumn.isEmpty()){
			this.columns.remove(rightInsertionPoint);
		}
	}

	public void updateColumns(Interval verticalRange, SliceColumn leftColumn, SliceColumn rightColumn) {
		addSide(verticalRange, leftColumn.getLeft(), leftColumn.getRight());
		addSide(verticalRange, rightColumn.getRight(), rightColumn.getLeft());
	}

	@Override
	public void fill(int x, int y, int dx, int dy) {
		assert isFree(x, y, dx, dy);
		Interval verticalRange = new Interval(y, y + dy);
		Interval horizontalRange = new Interval(x, x + dx);

		int leftInsertionPoint = horizontalRange.getTo();
		int rightInsertionPoint = horizontalRange.getFrom();
		SliceColumn leftColumn = getOrAddColumn(leftInsertionPoint);
		SliceColumn rightColumn = getOrAddColumn(rightInsertionPoint);

		IntervalSet prevLeft = null;
		IntervalSet prevRight = null;

		assert nothing(prevLeft = leftColumn.getRight().copy());
		assert nothing(prevRight = rightColumn.getLeft().copy());
		
		updateColumns(verticalRange, leftColumn, rightColumn);
		removeColumnsIfEmpty(leftInsertionPoint, rightInsertionPoint, leftColumn, rightColumn);
		
		assert leftColumn.isClean();
		assert rightColumn.isClean();
		assert prevLeft == null || SliceColumn.disjoint(leftColumn.getLeft(), prevLeft); 
		assert prevRight == null || SliceColumn.disjoint(rightColumn.getRight(), prevRight); 
	}

	private <T> boolean  nothing(T unused) {
		return true;
	}

	private void addSide(Interval verticalRange, IntervalSet side, IntervalSet other) {
		side.addInterval(verticalRange);
		side.removeAllRanges(other);
		other.removeInterval(verticalRange);
	}

	private SliceColumn getOrAddColumn(int to) {
		SliceColumn leftColumn = this.columns.get(to);
		if(leftColumn == null){
			leftColumn = newColumn();
			this.columns.put(to, leftColumn);
		}
		return leftColumn;
	}

	public static IntervalSet buildIntervalSet(int height2) {
		// return new TreeIntervalSet(new Interval(0, height2));
		return new BitIntervalSet(height2);
	}

	public Collection<MaximumRectangle> getMaximumRectangles() {
		Collection<MaximumRectangle> maximumRectangles = Lists.newArrayList();
		Deque<StartLine> startLinesDeque = Queues.newArrayDeque();

		int[] rightIndexes = new int[this.columns.size()];
		IntervalSet[] rightColumns = new IntervalSet[this.columns.size()];
		int rights = 0;
		IntervalSet freeSpace = newIntervalSet();

		for (Entry<Integer, SliceColumn> entry : this.columns.entrySet()) {
			SliceColumn sliceColumn = entry.getValue();
			IntervalSet right = sliceColumn.getRight();
			if(!right.isEmpty()){
				rightColumns[rights] = right;
				rightIndexes[rights] = entry.getKey();
				rights++;
			}
			updateFreeSpace(freeSpace, sliceColumn);
			IntervalSet left = sliceColumn.getLeft();
			if(!left.isEmpty()){
				List<Interval> intervals = freeSpace.getIntervals();
				for (Interval interval : intervals) {
					startLinesDeque.addLast(new StartLine(left,  entry.getKey(), entry.getKey(), interval));
				}
			}
		}
		rightIndexes = Arrays.copyOf(rightIndexes, rights);
		rightColumns = Arrays.copyOf(rightColumns, rights);
		
		
		StartLine startLine = startLinesDeque.pollFirst();
		while (startLine != null) {
//			IntervalSet leftColumn = startLine.getSleighColumn();
			int left = startLine.getLeft();
			Interval line = startLine.getLine();
			int start = startLine.getStart();
			int index = nextIndex(rightIndexes, start);
			while (index < rightIndexes.length) {
				IntervalSet rightColumn = rightColumns[index];
				if (rightIsNotEmpty(rightColumn, line)) {
					Rectangle rectangle = Rectangle.of(left, line.getFrom(), rightIndexes[index] - left, line.length());

					maximumRectangles.add(new MaximumRectangle(rectangle, null));
					List<Interval> emptyPaths = emptyPaths(rightColumn, line);
					Iterator<Interval> emptyPathsIterator = emptyPaths.iterator();
					if (emptyPathsIterator.hasNext()) {
						Interval path = emptyPathsIterator.next();
						while (emptyPathsIterator.hasNext()) {
							Interval next = emptyPathsIterator.next();
							if (line.bound(next).isEmpty()) {
								break;
							}
							startLinesDeque.addLast(new StartLine(null, left, rightIndexes[index], next));
						}
						if (line.bound(path).isEmpty()) {
							break;
						}
						line = path;
						start = rightIndexes[index];
						index++;
					} else {
						break;
					}
				} else {
					index++;
				}
			}
			startLine = startLinesDeque.pollFirst();
		}

		return maximumRectangles;
	}

	private boolean rightIsNotEmpty(IntervalSet rightColumn, Interval bounderLine) {
		// return perimeterSlice.getRightPerimeter(rightIndexes, bounderLine) !=
		// 0;
		return rightColumn.isAnythingInside(bounderLine);
	}

	private boolean isLeftEmpty(IntervalSet leftColumn, Interval bound) {
		// return perimeterSlice.getLeftPerimeter(left, bound) != 0;
		return !leftColumn.isAnythingInside(bound);
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
		return optimizedEmptyPaths(rightSides, bound);
	}

	private List<Interval> safeGetEmptyIntervals(IntervalSet rightSides, Interval bound) {
		IntervalSet treeIntervalSet = newIntervalSet();
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

		Interval interval = new Interval(0, height);

		SliceColumn leftSleighColumn = intervalSlice.columns.get(0);
		leftSleighColumn.getLeft().addInterval(interval);

		SliceColumn rightSleighColumn = intervalSlice.columns.get(width);
		rightSleighColumn.getRight().addInterval(interval);

		return intervalSlice;
	}

	public static IntervalSlice filled(int width, int height) {
		IntervalSlice intervalSlice = new IntervalSlice(width, height);
		intervalSlice.columns.clear();
		return intervalSlice;
	}

	public boolean isEmpty() {
		if (this.columns.size() != 2) {
			return false;
		}
		SliceColumn firstColumn = this.columns.get(0);
		if(firstColumn == null){
			return false;
		}
		if(!checkFullIntervalSet(firstColumn.getLeft())){
			return false;
		}

		SliceColumn lastColumn = this.columns.get(this.width);
		if(lastColumn == null){
			return false;
		}
		if(!checkFullIntervalSet(lastColumn.getRight())){
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
