package pipi.interval;

public class MaximumRectangle {
	private final Interval horizontalRange;
	private final Interval verticalRange;

	public MaximumRectangle(Interval horizontalRange, Interval verticalRange) {
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
