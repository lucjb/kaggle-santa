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

	public int area() {
		return this.horizontalRange.length() * this.verticalRange.length();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.horizontalRange == null) ? 0 : this.horizontalRange.hashCode());
		result = prime * result + ((this.verticalRange == null) ? 0 : this.verticalRange.hashCode());
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
		MaximumRectangle other = (MaximumRectangle) obj;
		if (this.horizontalRange == null) {
			if (other.horizontalRange != null)
				return false;
		} else if (!this.horizontalRange.equals(other.horizontalRange))
			return false;
		if (this.verticalRange == null) {
			if (other.verticalRange != null)
				return false;
		} else if (!this.verticalRange.equals(other.verticalRange))
			return false;
		return true;
	}

	public MaximumRectangle intersect(MaximumRectangle other) {
		return new MaximumRectangle(this.horizontalRange.bound(other.horizontalRange),
				this.verticalRange.bound(other.verticalRange));
	}

}
