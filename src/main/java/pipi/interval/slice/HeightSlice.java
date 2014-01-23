package pipi.interval.slice;

import pipi.interval.Interval;

public class HeightSlice {

	private final int width;
	private final int height;

	private final HeightSides leftSides;
	private final HeightSides rightSides;
	private final HeightSides upSides;
	private final HeightSides downSides;

	private HeightSlice(int width, int height, HeightSides left, HeightSides right, HeightSides up, HeightSides down) {
		this.width = width;
		this.height = height;

		this.leftSides = left;
		this.rightSides = right;
		this.upSides = up;
		this.downSides = down;

	}

	public void fill(int x, int y, int dx, int dy, int height) {
		Interval verticalRange = new Interval(y, y + dy);
		Interval horizontalRange = new Interval(x, x + dx);

		this.leftSides.addSide(verticalRange, horizontalRange.getTo(), height);
		this.rightSides.addSide(verticalRange, horizontalRange.getFrom(), height);
		this.upSides.addSide(horizontalRange, verticalRange.getTo(), height);
		this.downSides.addSide(horizontalRange, verticalRange.getFrom(), height);

	}

	public void free(int x, int y, int dx, int dy) {
		Interval verticalRange = new Interval(y, y + dy);
		Interval horizontalRange = new Interval(x, x + dx);

		this.leftSides.removeSide(verticalRange, horizontalRange.getTo());
		this.rightSides.removeSide(verticalRange, horizontalRange.getFrom());
		this.upSides.removeSide(horizontalRange, verticalRange.getTo());
		this.downSides.removeSide(horizontalRange, verticalRange.getFrom());
	}

	public HeightSides getDownSides() {
		return this.downSides;
	}

	public HeightSides getLeftSides() {
		return this.leftSides;
	}

	public HeightSides getRightSides() {
		return this.rightSides;
	}

	public HeightSides getUpSides() {
		return this.upSides;
	}

	public static HeightSlice empty(int width, int height) {
		HeightSlice perimeterSlice = new HeightSlice(width, height, HeightSides.empty(width, height), HeightSides.empty(
				width, height), HeightSides.empty(height, width), HeightSides.empty(height, width));

		return perimeterSlice;
	}

	public HeightSlice copy() {
		return new HeightSlice(this.width, this.height, this.leftSides.copy(), this.rightSides.copy(), this.upSides.copy(),
				this.downSides.copy());
	}

}
