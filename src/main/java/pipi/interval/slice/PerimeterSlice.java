package pipi.interval.slice;

import pipi.Box2d;
import pipi.Point2d;
import pipi.interval.Interval;

public class PerimeterSlice {

	private final int width;
	private final int height;

	private final CachedSides leftSides;
	private final CachedSides rightSides;
	private final CachedSides upSides;
	private final CachedSides downSides;

	private PerimeterSlice(int width, int height, CachedSides left, CachedSides right, CachedSides up, CachedSides down) {
		this.width = width;
		this.height = height;

		this.leftSides = left;
		this.rightSides = right;
		this.upSides = up;
		this.downSides = down;

	}

	public void fill(int x, int y, int dx, int dy) {
		Interval verticalRange = new Interval(y, y + dy);
		Interval horizontalRange = new Interval(x, x + dx);

		this.leftSides.addSide(verticalRange, horizontalRange.getTo());
		this.rightSides.addSide(verticalRange, horizontalRange.getFrom());
		this.upSides.addSide(horizontalRange, verticalRange.getTo());
		this.downSides.addSide(horizontalRange, verticalRange.getFrom());

	}

	public void free(int x, int y, int dx, int dy) {
		Interval verticalRange = new Interval(y, y + dy);
		Interval horizontalRange = new Interval(x, x + dx);

		this.leftSides.removeSide(verticalRange, horizontalRange.getTo());
		this.rightSides.removeSide(verticalRange, horizontalRange.getFrom());
		this.upSides.removeSide(horizontalRange, verticalRange.getTo());
		this.downSides.removeSide(horizontalRange, verticalRange.getFrom());
	}

	public int getPerimeterInt(Point2d point2d, Box2d box2d) {
		int perimeterLeft =  getPerimeterLeft(point2d.x, point2d.y, point2d.y + box2d.dy);
		int perimeterRight = getPerimeterRight(point2d.x + box2d.dx, point2d.y, point2d.y + box2d.dy);
		int perimeterUp = getPerimeterUp(point2d.y, point2d.x, point2d.x + box2d.dx);
		int perimeterDown = getPerimeterDown(point2d.y + box2d.dy, point2d.x, point2d.x + box2d.dx);

		return perimeterLeft + perimeterRight + perimeterUp + perimeterDown;
	}

	public int getPerimeterDown(int point, int from, int to) {
		return this.downSides.perimeter(point, from, to);
	}

	public int getPerimeterUp(int point, int from, int to) {
		return this.upSides.perimeter(point, from, to);
	}

	public int getPerimeterRight(int point, int from, int to) {
		return this.rightSides.perimeter(point, from, to);
	}

	public int getPerimeterLeft(int point, int from, int to) {
		return this.leftSides.perimeter(point, from, to);
	}

	public static PerimeterSlice filled(int width, int height) {
		PerimeterSlice perimeterSlice = new PerimeterSlice(width, height, CachedSides.filled(width, height),
				CachedSides.filled(width, height), CachedSides.filled(height, width), CachedSides.filled(height, width));
		return perimeterSlice;
	}

	public static PerimeterSlice empty(int width, int height) {
		PerimeterSlice perimeterSlice = new PerimeterSlice(width, height, CachedSides.empty(width, height, 0),
				CachedSides.empty(width, height, width), CachedSides.empty(height, width, 0), CachedSides.empty(height,
						width, height));

		return perimeterSlice;
	}

	public PerimeterSlice copy() {
		return new PerimeterSlice(this.width, this.height, this.leftSides.copy(), this.rightSides.copy(), this.upSides.copy(),
				this.downSides.copy());
	}

}
