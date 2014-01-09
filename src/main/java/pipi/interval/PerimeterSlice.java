package pipi.interval;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import pipi.Box2d;
import pipi.Point2d;

public class PerimeterSlice {

	private final int width;
	private final int height;

	private final BitIntervalSet[] lefts;
	private final BitIntervalSet[] rights;
	private final BitIntervalSet[] ups;
	private final BitIntervalSet[] downs;

	private int[][] leftSnapshot;
	private int[][] rightSnapshot;
	private int[][] upSnapshot;
	private int[][] downSnapshot;
	
	
	private static final int[] NATURAL = new int[1001];
	static{
		for (int i = 0; i < NATURAL.length; i++) {
			NATURAL[i]=i;
		}
	}
	
	public PerimeterSlice(int width, int height) {
		this.width = width;
		this.height = height;
		Interval verticalInterval = Interval.of(0, height);
		Interval horizontalInterval = Interval.of(0, width);
		this.lefts = new BitIntervalSet[height + 1];
		this.rights = new BitIntervalSet[height + 1];
		this.ups = new BitIntervalSet[width + 1];
		this.downs = new BitIntervalSet[width + 1];

		BitIntervalSet left = new BitIntervalSet(height);
		left.addInterval(verticalInterval);
		this.lefts[0] = left;

		BitIntervalSet right = new BitIntervalSet(height);
		right.addInterval(verticalInterval);
		this.rights[width] = right;

		BitIntervalSet up = new BitIntervalSet(width);
		up.addInterval(horizontalInterval);
		this.ups[0] = up;

		BitIntervalSet down = new BitIntervalSet(width);
		down.addInterval(horizontalInterval);
		this.downs[height] = down;
		
		
		this.leftSnapshot = getSideSnapshot(this.lefts, this.height);
		this.rightSnapshot = getSideSnapshot(this.rights, this.height);
		this.upSnapshot = getSideSnapshot(this.ups, this.width);
		this.downSnapshot = getSideSnapshot(this.downs, this.width);

	}

	public void fill(int x, int y, int dx, int dy) {
		Interval verticalRange = new Interval(y, y + dy);
		Interval horizontalRange = new Interval(x, x + dx);

		addColumn(this.lefts, this.leftSnapshot, horizontalRange.getTo(), verticalRange, this.height);
		addColumn(this.rights, this.rightSnapshot, horizontalRange.getFrom(), verticalRange, this.height);

		addColumn(this.ups, this.upSnapshot, verticalRange.getTo(), horizontalRange, this.width);
		addColumn(this.downs, this.downSnapshot, verticalRange.getFrom(), horizontalRange, this.width);

	}

	private void addColumn(BitIntervalSet[] side, int[][] snapshot, int insertionPoint, Interval verticalRange, int extension) {
		BitIntervalSet sliceColumn = side[insertionPoint];
		if (sliceColumn == null) {
			sliceColumn = new BitIntervalSet(extension);
			side[insertionPoint] = sliceColumn;
		}
		snapshot[insertionPoint] = null;
		sliceColumn.addInterval(verticalRange);
	}

	public int getPerimeterInt(Point2d point2d, Box2d box2d) {
		return getPerimeterIntOld(point2d, box2d);
	}

	public int getPerimeterIntOld(Point2d point2d, Box2d box2d) {
		Interval line = Interval.of(point2d.y, point2d.y + box2d.dy);
		Interval hori = Interval.of(point2d.x, point2d.x + box2d.dx);

		int perimeterLeft = getLeftPerimeter(point2d.x, line);
		int perimeterRight = getRightPerimeter(point2d.x + box2d.dx, line);
		int perimeterUp = getSideIntervalIntSnapshot(this.ups, this.upSnapshot, point2d.y, hori);
		int perimeterDown = getSideIntervalIntSnapshot(this.downs, this.downSnapshot, point2d.y + box2d.dy, hori);
		return perimeterLeft + perimeterRight + perimeterUp + perimeterDown;
	}

	public int getRightPerimeter(int point2, Interval line) {
		return getSideIntervalIntSnapshot(this.rights, this.rightSnapshot, point2, line);
	}

	public int getLeftPerimeter(int point, Interval line) {
		return getSideIntervalIntSnapshot(this.lefts, this.leftSnapshot,point, line);
	}

	private int getSideIntervalInt(BitIntervalSet[] tree, int point, Interval line) {
		BitIntervalSet sliceColumn = tree[point];
		if (sliceColumn == null) {
			return 0;
		}
		return sliceColumn.count(line);
	}

	private int getSideIntervalIntSnapshot(BitIntervalSet[] tree, int[][] snapshot, int point, Interval line) {
		BitIntervalSet sliceColumn = tree[point];
		if (sliceColumn == null) {
			return 0;
		}
		if(snapshot[point] == null){
			snapshot[point] = columnSnapshot(sliceColumn, 1000);
		}
		return snapshot[point][line.getTo()] - snapshot[point][line.getFrom()];
	}

	
	public PerimeterSnapshot perimeterSnapshot() {
		int[][] leftSnapshot = getSideSnapshot(this.lefts, this.height);
		int[][] rightSnapshot = getSideSnapshot(this.rights, this.height);
		int[][] upSnapshot = getSideSnapshot(this.ups, this.width);
		int[][] downSnapshot = getSideSnapshot(this.downs, this.width);
		return new PerimeterSnapshot(leftSnapshot, rightSnapshot, upSnapshot, downSnapshot);
	}

	private int[][] getSideSnapshot(BitIntervalSet[] side, int dimension) {
		int[][] sideSnapshot = new int[side.length][];
		for (int i = 0; i < side.length; i++) {
			BitIntervalSet bitIntervalSet = side[i];
			if (bitIntervalSet != null) {
				int[] columnSnapshot = columnSnapshot(bitIntervalSet, dimension);
				sideSnapshot[i] = columnSnapshot;
			}
		}
		return sideSnapshot;
	}

	private int[] columnSnapshot(BitIntervalSet bitIntervalSet, int dimension) {
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

	public void writeInterval(int[] columnSnapshot, int start, Interval interval) {
		int length = interval.length();
		int from = interval.getFrom();
		System.arraycopy(NATURAL, start, columnSnapshot, from, length + 1);
	}

	public static PerimeterSlice filled(int width, int height) {
		PerimeterSlice perimeterSlice = new PerimeterSlice(width, height);
		perimeterSlice.lefts[0] = null;
		perimeterSlice.rights[width] = null;
		perimeterSlice.ups[0] = null;
		perimeterSlice.downs[height] = null;

		perimeterSlice.leftSnapshot = perimeterSlice.getSideSnapshot(perimeterSlice.lefts, perimeterSlice.height);
		perimeterSlice.rightSnapshot = perimeterSlice.getSideSnapshot(perimeterSlice.rights, perimeterSlice.height);
		perimeterSlice.upSnapshot = perimeterSlice.getSideSnapshot(perimeterSlice.ups, perimeterSlice.width);
		perimeterSlice.downSnapshot = perimeterSlice.getSideSnapshot(perimeterSlice.downs, perimeterSlice.width);

		return perimeterSlice;
	}

	public void free(int x, int y, int dx, int dy) {
		throw new RuntimeException("implement!!!");
	}
}
