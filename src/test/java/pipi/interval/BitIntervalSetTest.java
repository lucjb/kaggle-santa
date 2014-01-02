package pipi.interval;

import java.util.List;

public class BitIntervalSetTest extends IntervalSetTest {

	public BitIntervalSetTest(List<Interval> intervals, List<Interval> expected) {
		super(intervals, expected);
	}

	@Override
	protected IntervalSet createIntervalSet() {
		return new BitIntervalSet(10);
	}

}
