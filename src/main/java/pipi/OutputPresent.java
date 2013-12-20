package pipi;

import first.Point;

public class OutputPresent {

	private final int order;
	private final Point point;
	private final Box box;

	public OutputPresent(int order, Point point, Box box) {
		this.order = order;
		this.point = point;
		this.box = box;
	}

	public int getOrder() {
		return order;
	}

	public Point getPoint() {
		return point;
	}

	public Box getBox() {
		return box;
	}

}
