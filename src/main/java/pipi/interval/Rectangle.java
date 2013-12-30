package pipi.interval;

import pipi.Box2d;
import pipi.Point2d;

public class Rectangle {
	private final Point2d point2d;
	private final Box2d box2d;

	public Rectangle(Point2d point2d, Box2d box2d) {
		this.point2d = point2d;
		this.box2d = box2d;
	}

	public static Rectangle of(int x, int y, int dx, int dy) {
		return new Rectangle(new Point2d(x, y), new Box2d(dx, dy));
	}

	public Box2d getBox2d() {
		return this.box2d;
	}

	public Point2d getPoint2d() {
		return this.point2d;
	}

}
