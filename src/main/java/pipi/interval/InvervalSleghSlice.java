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
			if (sleighColumn.isEmpty()) {
				iterator.remove();
			}
		}
	}

	private void addColumn(TreeMap<Integer, SleighColumn> side, int insertionPoint, Interval verticalRange) {
		SleighColumn sleighColumn = side.get(insertionPoint);
		if (sleighColumn == null) {
			sleighColumn = new SleighColumn(new Interval(0, this.height));

			Entry<Integer, SleighColumn> leftColumn = this.lefts.floorEntry(insertionPoint);
			Entry<Integer, SleighColumn> rightColumn = this.rights.floorEntry(insertionPoint);
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
					sleighColumn.getLines().addAllRanges(leftColumnValue.getLines().getRanges(verticalRange));
					List<Interval> ranges = leftColumnValue.getRanges().getRanges();
					sleighColumn.removeLines(ranges);
					if(sleighColumn.isEmpty()){
						return;
					}
				}
			} else {
				SleighColumn rightColumnValue = rightColumn.getValue();
				// FIXME maintain invariant in remove and add of column
				sleighColumn.getLines().addAllRanges(rightColumnValue.getLines().getRanges(verticalRange));
				sleighColumn.removeLines(rightColumnValue.getRanges().getRanges(verticalRange));
				if(sleighColumn.isEmpty()){
					return;
				}
			}
			side.put(insertionPoint, sleighColumn);

		}
		sleighColumn.addInterval(verticalRange);
	}

	public Collection<Rectangle> getMaximumRectangles() {
		Collection<Rectangle> rectangles = Lists.newArrayList();
		// Deque<StartLine> leftDeque = Queues.newArrayDeque();
		// for (Entry<Integer, SleighColumn> leftEntry : this.lefts.entrySet())
		// {
		// for (Line line : leftEntry.getValue().getLines()) {
		// Line bounderLine = line.copy();
		// leftDeque.addLast(new StartLine(leftEntry.getKey(),
		// leftEntry.getKey(), bounderLine));
		// }
		// }
		//
		// StartLine leftPair = leftDeque.pollFirst();
		// while (leftPair != null) {
		// Line bounderLine = leftPair.getLine();
		//
		// OUTER: for (Entry<Integer, SleighColumn> rightEntry :
		// this.rights.tailMap(leftPair.getStart(), false).entrySet()) {
		// for (Line rightLine : rightEntry.getValue().getLines()) {
		// if (!bounderLine.getSpan().bound(rightLine.getSpan()).isEmpty()) {
		// if (bounderLine.intersects(rightLine)) {
		// rectangles.add(new Rectangle(new IntRange(leftPair.getLeft(),
		// rightEntry.getKey()), bounderLine
		// .getSpan()));
		// break OUTER;
		// }
		// }
		// }
		// }
		//
		// leftPair = leftDeque.pollFirst();
		// }
		//
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
