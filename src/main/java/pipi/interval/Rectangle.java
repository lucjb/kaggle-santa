package pipi.interval;

public class Rectangle {
	private final Interval horizontalRange;
	private final Interval verticalRange;

	public Rectangle(Interval horizontalRange, Interval verticalRange) {
		this.horizontalRange = horizontalRange;
		this.verticalRange = verticalRange;
	}
	public Interval getHorizontalRange() {
		return this.horizontalRange;
	}
	
	public Interval getVerticalRange() {
		return this.verticalRange;
	}
	
	@Override
	public String toString() {
		return this.horizontalRange + "x" + this.verticalRange;
	}
	
	public int area(){
		return this.horizontalRange.length() * this.verticalRange.length();
	}
}
