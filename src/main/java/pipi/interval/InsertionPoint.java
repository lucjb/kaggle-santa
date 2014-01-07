package pipi.interval;

import pipi.Box2d;
import pipi.Point2d;

public class InsertionPoint {

	public final Point2d point2d;
	public final int perimeter;
	public final Box2d orientation;

	public InsertionPoint(Point2d insertionPoint2d, Box2d orientation, int perimeter) {
		this.point2d = insertionPoint2d;
		this.orientation = orientation;
		this.perimeter = perimeter;
	}

}
