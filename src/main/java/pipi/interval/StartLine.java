package pipi.interval;

public class StartLine {
	private int left;
	private int start;
	private Line line;

	public StartLine(int left, int start, Line line) {
		this.left = left;
		this.start = start;
		this.line = line;
	}

	public int getLeft() {
		return this.left;
	}

	public Line getLine() {
		return this.line;
	}

	public int getStart() {
		return this.start;
	}
}
