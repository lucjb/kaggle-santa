package pipi.interval;

import java.util.List;

public class Perimeter {
	public IntervalSet perimeterLeft;
	public IntervalSet perimeterRight;
	public IntervalSet perimeterUp;
	public IntervalSet perimeterDown;

	public Perimeter(IntervalSet perimeterLeft, IntervalSet perimeterRight, IntervalSet perimeterUp,
			IntervalSet perimeterDown) {
		this.perimeterLeft = perimeterLeft;
		this.perimeterRight = perimeterRight;
		this.perimeterUp = perimeterUp;
		this.perimeterDown = perimeterDown;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.perimeterDown == null) ? 0 : this.perimeterDown.hashCode());
		result = prime * result + ((this.perimeterLeft == null) ? 0 : this.perimeterLeft.hashCode());
		result = prime * result + ((this.perimeterRight == null) ? 0 : this.perimeterRight.hashCode());
		result = prime * result + ((this.perimeterUp == null) ? 0 : this.perimeterUp.hashCode());
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
		Perimeter other = (Perimeter) obj;
		if (this.perimeterDown == null) {
			if (other.perimeterDown != null)
				return false;
		} else if (!this.perimeterDown.equals(other.perimeterDown))
			return false;
		if (this.perimeterLeft == null) {
			if (other.perimeterLeft != null)
				return false;
		} else if (!this.perimeterLeft.equals(other.perimeterLeft))
			return false;
		if (this.perimeterRight == null) {
			if (other.perimeterRight != null)
				return false;
		} else if (!this.perimeterRight.equals(other.perimeterRight))
			return false;
		if (this.perimeterUp == null) {
			if (other.perimeterUp != null)
				return false;
		} else if (!this.perimeterUp.equals(other.perimeterUp))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Perimeter [perimeterLeft=" + this.perimeterLeft + ", perimeterRight=" + this.perimeterRight
				+ ", perimeterUp=" + this.perimeterUp + ", perimeterDown=" + this.perimeterDown + "]";
	}

	public int perimeter() {
		return sumIntervals(this.perimeterLeft.getIntervals()) + sumIntervals(this.perimeterRight.getIntervals())
				+ sumIntervals(this.perimeterUp.getIntervals()) + sumIntervals(this.perimeterDown.getIntervals());
	}

	public static int sumIntervals(List<Interval> intervals) {
		int sum = 0;
		for (Interval interval : intervals) {
			sum += interval.length();
		}
		return sum;
	}


}