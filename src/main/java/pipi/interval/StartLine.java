package pipi.interval;

public class StartLine {
	private int left;
	private int start;
	private Interval interval;
	private SleighColumn sleighColumn;

	public StartLine(SleighColumn sleighColumn, int left, int start, Interval bounderLine) {
		this.sleighColumn = sleighColumn;
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
	
	public SleighColumn getSleighColumn() {
		return this.sleighColumn;
	}
	@Override
	public String toString() {
		return "(" + this.getLeft() + "->" +this.getStart() + "->" + this.interval + ")";
	}
}
