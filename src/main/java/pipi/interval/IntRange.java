package pipi.interval;

public class IntRange {
	private final int from;
	private final int to;

	public IntRange(int from, int to) {
		this.from = from;
		this.to = to;
	}

	public int getFrom() {
		return this.from;
	}

	public int getTo() {
		return this.to;
	}

	public IntRange bound(IntRange intRange) {
		return new IntRange(Math.max(this.from, intRange.from), Math.min(
				this.to, intRange.to));
	}

}
