package pipi.interval;

import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.lang3.tuple.Pair;

import pipi.SleighSlice;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;

public class CapoSleghSlice implements SleighSlice {

	private final int width;
	private final int height;
	private final TreeMap<Integer, SleighColumn> lefts = Maps.newTreeMap();
	private final TreeMap<Integer, SleighColumn> rights = Maps.newTreeMap();

	private CapoSleghSlice(int width, int height) {
		this.width = width;
		this.height = height;
		this.lefts.put(0, new SleighColumn());
		this.rights.put(width, new SleighColumn());
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
		IntRange verticalRange = new IntRange(y, y + dy);
		IntRange horizontalRange = new IntRange(x, x + dx);

		addLeft(horizontalRange, verticalRange);
		addRight(horizontalRange, verticalRange);
		destroyColumns(this.lefts.subMap(horizontalRange.getFrom(), horizontalRange.getTo()).values(), verticalRange);
		destroyColumns(this.rights.subMap(horizontalRange.getFrom(), horizontalRange.getTo()).values(), verticalRange);
	}

	private void destroyColumns(Collection<SleighColumn> columns, IntRange verticalRange) {
		for (SleighColumn sleighColumn : columns) {
			sleighColumn.destroy(verticalRange);
			// TODO remove if empty
		}
	}

	private void addLeft(IntRange horizontalRange, IntRange verticalRange) {
		SleighColumn sleighColumn = this.lefts.get(horizontalRange.getTo());
		if (sleighColumn != null) {
			addToExistingLeft(sleighColumn, horizontalRange, verticalRange);
			return;
		}
		SleighColumn otherColumn = this.rights.get(horizontalRange.getTo());
		if (otherColumn == null || otherColumn.getLine(verticalRange.getFrom()) == null) {
			sleighColumn = addNewAloneLeft(horizontalRange, verticalRange);
		} else {
			sleighColumn = addNewOther(verticalRange, otherColumn);
			if (sleighColumn == null) {
				return;
			}
		}
		this.lefts.put(horizontalRange.getTo(), sleighColumn);
	}

	private void addRight(IntRange horizontalRange, IntRange verticalRange) {
		SleighColumn sleighColumn = this.rights.get(horizontalRange.getFrom());
		if (sleighColumn != null) {
			addToExistingRight(sleighColumn, horizontalRange, verticalRange);
			return;
		}
		SleighColumn otherColumn = this.lefts.get(horizontalRange.getFrom());
		if (otherColumn == null || otherColumn.getLine(verticalRange.getFrom()) == null) {
			sleighColumn = addNewAloneRight(horizontalRange, verticalRange);
		} else {
			sleighColumn = addNewOther(verticalRange, otherColumn);
			if (sleighColumn == null) {
				return;
			}

		}
		this.rights.put(horizontalRange.getFrom(), sleighColumn);
	}

	private void addToExistingLeft(SleighColumn sleighColumn, IntRange horizontalRange, IntRange verticalRange) {
		Line line = sleighColumn.getLine(verticalRange.getFrom());
		if (line == null) {
			line = new Line(closestVerticalLeft(verticalRange.getFrom(), horizontalRange.getTo()));
			sleighColumn.addLine(line);
		}
		line.addRange(verticalRange);
	}

	private void addToExistingRight(SleighColumn sleighColumn, IntRange horizontalRange, IntRange verticalRange) {
		Line line = sleighColumn.getLine(verticalRange.getFrom());
		if (line == null) {
			line = new Line(closestVerticalRight(verticalRange.getFrom(), horizontalRange.getTo()));
			sleighColumn.addLine(line);
		}
		line.addRange(verticalRange);
	}

	private SleighColumn addNewAloneLeft(IntRange horizontalRange, IntRange verticalRange) {
		SleighColumn sleighColumn;
		sleighColumn = new SleighColumn();
		Line closestLine = new Line(closestVerticalLeft(verticalRange.getFrom(), horizontalRange.getTo()));
		sleighColumn.addLine(closestLine);
		closestLine.addRange(verticalRange);
		return sleighColumn;
	}

	private SleighColumn addNewAloneRight(IntRange horizontalRange, IntRange verticalRange) {
		SleighColumn sleighColumn;
		sleighColumn = new SleighColumn();
		Line closestLine = new Line(closestVerticalRight(verticalRange.getFrom(), horizontalRange.getFrom()));
		sleighColumn.addLine(closestLine);
		closestLine.addRange(verticalRange);
		return sleighColumn;
	}

	private SleighColumn addNewOther(IntRange verticalRange, SleighColumn otherColumn) {
		SleighColumn sleighColumn;
		Line otherLine = otherColumn.getLine(verticalRange.getFrom());
		List<IntRange> emptyRanges = otherLine.getEmptyRanges(verticalRange);
		sleighColumn = new SleighColumn();
		if (emptyRanges.isEmpty()) {
			return null;
		}
		for (IntRange lineRange : emptyRanges) {
			Line sleighLine = new Line(lineRange);
			sleighLine.addRange(verticalRange);
			sleighColumn.addLine(sleighLine);
		}
		return sleighColumn;
	}

	private IntRange closestVerticalRight(int y, int x) {
//		return verticalClosestRange(y, closestRightColumn(y, x), closestLeftColumn(y, x));
		return verticalClosestRange(y, closestLeftColumn(y, x), closestRightColumn(y, x));
	}

	private IntRange closestVerticalLeft(int y, int x) {
		return verticalClosestRange(y, closestLeftColumn(y, x), closestRightColumn(y, x));
	}

	private IntRange verticalClosestRange(int y, Entry<Integer, SleighColumn> mainEntry,
			Entry<Integer, SleighColumn> secondary) {
		IntRange range;
		if (mainEntry != null) {
			if (secondary == null || mainEntry.getKey() >= secondary.getKey()) {
				Line line = mainEntry.getValue().getLine(y);
				range = line.getSpan();
			} else {
				Line line = secondary.getValue().getLine(y);
				range = line.getEmptyRange(y);
			}
		} else {
			Line line = secondary.getValue().getLine(y);
			range = line.getEmptyRange(y);
		}
		return range;
	}

	private Entry<Integer, SleighColumn> closestLeftColumn(int y, int leftPosition) {
		return closestColumn(y, leftPosition, this.lefts);
	}

	private Entry<Integer, SleighColumn> closestRightColumn(int y, int leftPosition) {
		return closestColumn(y, leftPosition, this.rights);
	}

	private Entry<Integer, SleighColumn> closestColumn(int y, int position, TreeMap<Integer, SleighColumn> map) {
		Entry<Integer, SleighColumn> floorEntry;
		for (;;) {
			floorEntry = map.floorEntry(position);
			if (floorEntry == null) {
				return null;
			}
			SleighColumn column = floorEntry.getValue();
			Line line = column.getLine(y);
			if (line != null) {
				break;
			}
			position = floorEntry.getKey() - 1;
		}
		return floorEntry;
	}

	public static CapoSleghSlice empty(int width, int height) {
		CapoSleghSlice capoSleghSlice = new CapoSleghSlice(width, height);
		IntRange initialRange = new IntRange(0, height);
		Line leftLine = new Line(initialRange);
		leftLine.addRange(initialRange);
		capoSleghSlice.lefts.firstEntry().getValue().addLine(leftLine);

		Line rightLine = new Line(initialRange);
		rightLine.addRange(initialRange);
		capoSleghSlice.rights.lastEntry().getValue().addLine(leftLine);

		return capoSleghSlice;
	}

	public Collection<Rectangle> getMaximumRectangles() {
		Collection<Rectangle> rectangles = Lists.newArrayList();
		Deque<StartLine> leftDeque = Queues.newArrayDeque();
		for (Entry<Integer, SleighColumn> leftEntry : this.lefts.entrySet()) {
			for (Line line : leftEntry.getValue().getLines()) {
				Line bounderLine = line.copy();
				leftDeque.addLast(new StartLine(leftEntry.getKey(), leftEntry.getKey(), bounderLine));
			}
		}

		StartLine leftPair = leftDeque.pollFirst();
		while(leftPair!= null){
			Line bounderLine = leftPair.getLine(); 

			OUTER:
			for (Entry<Integer, SleighColumn> rightEntry : this.rights.tailMap(leftPair.getStart(), false).entrySet()) {
				for (Line rightLine : rightEntry.getValue().getLines()) {
					if(!bounderLine.getSpan().bound(rightLine.getSpan()).isEmpty()){
						if(bounderLine.intersects(rightLine)){
							rectangles.add(new Rectangle(new IntRange(leftPair.getLeft(), rightEntry.getKey()), bounderLine.getSpan()));
							break OUTER;
						}
					}
				}
			}
			
			leftPair = leftDeque.pollFirst();
		}
		

		return rectangles;
	}

}