package pipi.interval;

import pipi.Box2d;
import pipi.Point2d;

public class PerimeterSnapshot {

	private int[][] leftSnapshot;
	private int[][] rightSnapshot;
	private int[][] upSnapshot;
	private int[][] downSnapshot;

	public PerimeterSnapshot(int[][] leftSnapshot, int[][] rightSnapshot, int[][] upSnapshot, int[][] downSnapshot) {
		this.leftSnapshot = leftSnapshot;
		this.rightSnapshot = rightSnapshot;
		this.upSnapshot = upSnapshot;
		this.downSnapshot = downSnapshot;
	}

	public int getPerimeterInt(Point2d point2d, Box2d box2d) {
		Interval line = Interval.of(point2d.y, point2d.y + box2d.dy);
		Interval hori = Interval.of(point2d.x, point2d.x + box2d.dx);
		
		
		int perimeterLeft = getSideIntervalInt(this.leftSnapshot, hori.getFrom(), line);
		int perimeterRight = getSideIntervalInt(this.rightSnapshot, hori.getTo(), line);
		int perimeterUp = getSideIntervalInt(this.upSnapshot, line.getFrom(), hori);
		int perimeterDown = getSideIntervalInt(this.downSnapshot, line.getTo(), hori);
		return perimeterLeft + perimeterRight + perimeterUp + perimeterDown;
	}
	
	private int getSideIntervalInt(int[][] tree, int point, Interval line) {
		int[] sliceColumn = tree[point];
		if (sliceColumn == null) {
			return 0;
		}
		return sliceColumn[line.getTo()] - sliceColumn[line.getFrom()];
	}


}
