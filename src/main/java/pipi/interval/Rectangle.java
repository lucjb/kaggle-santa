package pipi.interval;

import pipi.Box2d;
import pipi.Point2d;

public class Rectangle {
	public final Point2d point2d;
	public final Box2d box2d;

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

	public Rectangle intersection(Rectangle other) {
		int x = Math.max(this.point2d.x, other.point2d.x);
		int y = Math.max(this.point2d.y, other.point2d.y);
		return Rectangle.of(x, y, Math.min(this.point2d.x + this.box2d.dx, other.point2d.x + other.box2d.dx) - x,
				Math.min(this.point2d.y + this.box2d.dy, other.point2d.y + other.box2d.dy) - y);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.box2d == null) ? 0 : this.box2d.hashCode());
		result = prime * result + ((this.point2d == null) ? 0 : this.point2d.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Rectangle other = (Rectangle) obj;
		if (this.box2d == null) {
			if (other.box2d != null)
				return false;
		} else if (!this.box2d.equals(other.box2d))
			return false;
		if (this.point2d == null) {
			if (other.point2d != null)
				return false;
		} else if (!this.point2d.equals(other.point2d))
			return false;
		return true;
	}
	
	

	
}
