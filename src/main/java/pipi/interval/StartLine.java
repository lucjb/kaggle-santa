package pipi.interval;

public class StartLine {
	private int left;
	private int start;
	private IntervalSet intervalSet;

	public StartLine(int left, int start, IntervalSet intervalSet) {
		this.left = left;
		this.start = start;
		this.intervalSet = intervalSet;
	}

	public int getLeft() {
		return this.left;
	}

	public IntervalSet getLine() {
		return this.intervalSet;
	}

	public int getStart() {
		return this.start;
	}
}
