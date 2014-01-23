package pipi.interval.slice;

public class HeightMisfit {

	public final int length;
	public final int same;
	public final int higher;
	public final int lower;
	public final int none;

	public HeightMisfit(int length, int same, int higher, int lower, int none) {
		this.length = length;
		this.same = same;
		this.higher = higher;
		this.lower = lower;
		this.none = none;
	}

}
