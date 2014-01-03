package pipi.interval;

import java.util.List;

public class TreeIntervalSetTest extends IntervalSetTest{

	public TreeIntervalSetTest(List<Interval> intervals, List<Interval> expected) {
		super(intervals, expected);
	}

	@Override
	protected IntervalSet createIntervalSet() {
		return new TreeIntervalSet(new Interval(0, 10));
	}

}
