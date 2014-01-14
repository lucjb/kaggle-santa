package pipi.interval;

public class StartLine {
	private int left;
	private int start;
	private Interval interval;
	private IntervalSet sliceColumn;

	public StartLine(IntervalSet sliceColumn, int left, int start, Interval bounderLine) {
		this.sliceColumn = sliceColumn;
		this.left = left;
		this.start = start;
		this.interval = bounderLine;
	}

	public int getLeft() {
		return this.left;
	}

	public Interval getLine() {
		return this.interval;
	}

	public int getStart() {
		return this.start;
	}
	
	public IntervalSet getSleighColumn() {
		return this.sliceColumn;
	}
	@Override
	public String toString() {
		return "(" + this.getLeft() + "->" +this.getStart() + "->" + this.interval + ")";
	}
}
