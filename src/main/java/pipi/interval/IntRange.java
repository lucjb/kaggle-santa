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

	public boolean contains(int point) {
		return this.from <= point && point < this.to;
	}
	
	@Override
	public String toString() {
		return "[" + this.from + ", " + this.to + ")";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + from;
		result = prime * result + to;
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
		IntRange other = (IntRange) obj;
		if (from != other.from)
			return false;
		if (to != other.to)
			return false;
		return true;
	}

	public boolean isEmpty() {
		return this.to <= this.from;
	}
	
	

}
