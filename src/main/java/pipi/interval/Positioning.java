package pipi.interval;

import pipi.Box2d;
import pipi.Point2d;
import pipi.interval.slice.HeightMisfit;
import pipi.packer.IntRectangle;

public class Positioning {

	public final Point2d point2d;
	public final int perimeter;
	public final Box2d orientation;
	public final HeightMisfit heightMisfit;
	public final IntRectangle boundaries;

	public Positioning(Point2d insertionPoint2d, Box2d orientation, int perimeter, HeightMisfit heightMisfit,
			IntRectangle boundaries) {
		this.point2d = insertionPoint2d;
		this.orientation = orientation;
		this.perimeter = perimeter;
		this.heightMisfit = heightMisfit;
		this.boundaries = boundaries;
	}

}
