package pipi.interval.slice;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import pipi.interval.BitIntervalSet;
import pipi.interval.Interval;

public class CachedSides {
	public BitIntervalSet[] sides;
	public int[][] snapshots;
	private int extension;
	private int columns;

	private static final int[] NATURAL = new int[1001];
	static {
		for (int i = 0; i < NATURAL.length; i++) {
			NATURAL[i] = i;
		}
	}

	private CachedSides(int columns, int extension) {
		this.columns = columns;
		this.extension = extension;
		this.sides = new BitIntervalSet[this.columns + 1];
		this.snapshots = new int[columns + 1][];

	}

	public static CachedSides empty(int columns, int extension, int start) {
		CachedSides cachedSides = new CachedSides(columns, extension);

		BitIntervalSet side = new BitIntervalSet(extension);
		Interval of = Interval.of(0, extension);
		side.addInterval(of);
		cachedSides.sides[start] = side;
		assert cachedSides.perimeter(start, of.getFrom(), of.getTo()) == extension;
		return cachedSides;
	}

	public static CachedSides filled(int columns, int extension) {
		return new CachedSides(columns, extension);
	}

	public void addSide(Interval verticalRange, int insertionPoint) {
		BitIntervalSet sliceColumn = this.sides[insertionPoint];
		if (sliceColumn == null) {
			sliceColumn = new BitIntervalSet(this.extension);
			this.sides[insertionPoint] = sliceColumn;
		}
		this.snapshots[insertionPoint] = null;
		sliceColumn.addInterval(verticalRange);
	}

	public void removeSide(Interval verticalRange, int insertionPoint) {
		BitIntervalSet sliceColumn = this.sides[insertionPoint];
		if (sliceColumn == null) {
			return;
		}
		sliceColumn.removeInterval(verticalRange);
		if (sliceColumn.isEmpty()) {
			this.sides[insertionPoint] = null;
		}
		this.snapshots[insertionPoint] = null;
	}

	public static int[] columnSnapshot(BitIntervalSet bitIntervalSet, int dimension) {
		int[] columnSnapshot = new int[dimension + 1];
		List<Interval> intervals = bitIntervalSet.getIntervals();
		Iterator<Interval> iterator = intervals.iterator();
		if (iterator.hasNext()) {
			Interval interval = iterator.next();
			writeInterval(columnSnapshot, 0, interval);
			int start = interval.length();
			Interval lastInterval = interval;
			while (iterator.hasNext()) {

				interval = iterator.next();
				Arrays.fill(columnSnapshot, lastInterval.getTo() + 1, interval.getFrom(), start);
				writeInterval(columnSnapshot, start, interval);
				start += interval.length();
				lastInterval = interval;
			}
			Arrays.fill(columnSnapshot, lastInterval.getTo() + 1, columnSnapshot.length, start);
		}
		return columnSnapshot;
	}

	public static void writeInterval(int[] columnSnapshot, int start, Interval interval) {
		int length = interval.length();
		int from = interval.getFrom();
		System.arraycopy(NATURAL, start, columnSnapshot, from, length + 1);
	}

	public int getSideIntervalIntSnapshot(BitIntervalSet[] intervalSet, int[][] snapshots, int point, int from, int to) {
		BitIntervalSet sliceColumn = intervalSet[point];
		if (sliceColumn == null) {
			return 0;
		}
		int[] snapshot = snapshots[point];
		if (snapshot == null) {
			snapshot = columnSnapshot(sliceColumn, this.extension);
			snapshots[point] = snapshot;
		}
		return snapshot[to] - snapshot[from];
	}

	public int perimeter(int point, int from, int to) {
		return getSideIntervalIntSnapshot(this.sides, this.snapshots, point, from, to);
	}

	public CachedSides copy() {
		CachedSides cachedSides = new CachedSides(this.columns, this.extension);
		for (int i = 0; i < this.sides.length; i++) {
			BitIntervalSet bitIntervalSet = this.sides[i];
			if (bitIntervalSet != null) {
				cachedSides.sides[i] = bitIntervalSet.copy();
			}
		}
		for (int i = 0; i < this.snapshots.length; i++) {
			int[] snapshot = this.snapshots[i];
			if (snapshot != null) {
				cachedSides.snapshots[i] = Arrays.copyOf(snapshot, snapshot.length);
			}
		}

		return cachedSides;
	}

}