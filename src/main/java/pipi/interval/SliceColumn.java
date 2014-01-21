package pipi.interval;

public class SliceColumn {

	private final IntervalSet left;
	private final IntervalSet right;

	public SliceColumn(IntervalSet left, IntervalSet right) {
		this.left = left;
		this.right = right;
	}

	public String toString() {
		return this.left + "|" + this.right;
	}

	public boolean isEmpty() {
		return this.left.isEmpty() && this.right.isEmpty();
	}

	public IntervalSet getLeft() {
		return this.left;
	}

	public IntervalSet getRight() {
		return this.right;
	}

	public boolean isClean() {
		return disjoint(this.left, this.right);
	}

	public static boolean disjoint(IntervalSet left2, IntervalSet right2) {
		for (Interval interval : left2.getIntervals()) {
			if (right2.isAnythingInside(interval)) {
				return false;
			}
		}

		for (Interval interval : right2.getIntervals()) {
			if (left2.isAnythingInside(interval)) {
				return false;
			}
		}

		return true;
	}

}
