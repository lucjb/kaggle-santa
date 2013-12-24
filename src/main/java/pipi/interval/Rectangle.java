package pipi.interval;

public class Rectangle {
	private final IntRange horizontalRange;
	private final IntRange verticalRange;

	public Rectangle(IntRange horizontalRange, IntRange verticalRange) {
		this.horizontalRange = horizontalRange;
		this.verticalRange = verticalRange;
	}
	public IntRange getHorizontalRange() {
		return this.horizontalRange;
	}
	
	public IntRange getVerticalRange() {
		return this.verticalRange;
	}
	
	@Override
	public String toString() {
		return this.horizontalRange + "x" + this.verticalRange;
	}
}
