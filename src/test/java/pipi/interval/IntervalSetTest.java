package pipi.interval;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static pipi.interval.Interval.of;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.google.common.collect.Iterables;

@RunWith(Parameterized.class)
public abstract class IntervalSetTest {

	private List<Interval> intervals;
	private List<Interval> expected;

	public static void listEquals(List<Interval> expected, List<Interval> actual) {
		assertTrue(Iterables.elementsEqual(expected, actual));
	}

	public static Object[] oa(Object... rectangles) {
		return rectangles;
	}

	public static Interval[] ia(Interval... intervals) {
		return intervals;
	}

	@Parameters
	public static Collection<Object[]> parameters() {
		return Arrays.<Object[]> asList(oa(Arrays.asList(of(0, 1), of(1, 2)), Arrays.asList(of(0, 2))));
	}

	protected abstract IntervalSet createIntervalSet();
			
	public IntervalSetTest(List<Interval> intervals, List<Interval> expected) {
		this.intervals = intervals;
		this.expected = expected;
	}

	@Test
	public void testAddInterval() {
		IntervalSet treeIntervalSet = this.createIntervalSet();
		for (Interval interval : this.intervals) {
			treeIntervalSet.addInterval(interval);
		}
		listEquals(this.expected, treeIntervalSet.getIntervals());
	}

	// @Test
	// public void testAddInterval() {
	// IntervalSet treeIntervalSet;
	// treeIntervalSet = this.createIntervalSet();
	// treeIntervalSet.addInterval(of(-1, 1));
	// treeIntervalSet.addInterval(of(9, 11));
	// listEquals(Arrays.asList(of(0, 1), of(9, 10)),
	// treeIntervalSet.getIntervals());
	// treeIntervalSet = this.createIntervalSet();
	// treeIntervalSet.addInterval(of(-1, 11));
	// listEquals(Arrays.asList(of(0, 10)), treeIntervalSet.getIntervals());
	// }
	//
	//
	// public void testOverlap() {
	// IntervalSet treeIntervalSet;
	// treeIntervalSet = this.createIntervalSet();
	// treeIntervalSet.addInterval(new Interval(0, 1));
	// treeIntervalSet.addInterval(new Interval(1, 2));
	// assertTrue(Iterables.elementsEqual(Arrays.asList(new Interval(0, 2)),
	// treeIntervalSet.getIntervals()));
	// treeIntervalSet = this.createIntervalSet();
	// treeIntervalSet.addInterval(new Interval(1, 2));
	// treeIntervalSet.addInterval(new Interval(0, 1));
	// assertTrue(Iterables.elementsEqual(Arrays.asList(new Interval(0, 2)),
	// treeIntervalSet.getIntervals()));
	// treeIntervalSet = this.createIntervalSet();
	// treeIntervalSet.addInterval(new Interval(0, 2));
	// treeIntervalSet.addInterval(new Interval(1, 3));
	// assertTrue(Iterables.elementsEqual(Arrays.asList(new Interval(0, 3)),
	// treeIntervalSet.getIntervals()));
	// treeIntervalSet = this.createIntervalSet();
	// treeIntervalSet.addInterval(new Interval(1, 3));
	// treeIntervalSet.addInterval(new Interval(0, 2));
	// assertTrue(Iterables.elementsEqual(Arrays.asList(new Interval(0, 3)),
	// treeIntervalSet.getIntervals()));
	// treeIntervalSet = this.createIntervalSet();
	// treeIntervalSet.addInterval(new Interval(0, 2));
	// treeIntervalSet.addInterval(new Interval(3, 5));
	// assertTrue(Iterables.elementsEqual(Arrays.asList(new Interval(0, 2), new
	// Interval(3, 5)), treeIntervalSet.getIntervals()));
	// treeIntervalSet = this.createIntervalSet();
	// treeIntervalSet.addInterval(new Interval(1, 3));
	// treeIntervalSet.addInterval(new Interval(5, 7));
	// treeIntervalSet.addInterval(new Interval(2, 4));
	// assertTrue(Iterables.elementsEqual(Arrays.asList(new Interval(1, 4), new
	// Interval(5, 7)), treeIntervalSet.getIntervals()));
	// treeIntervalSet = this.createIntervalSet();
	// treeIntervalSet.addInterval(new Interval(1, 3));
	// treeIntervalSet.addInterval(new Interval(5, 7));
	// treeIntervalSet.addInterval(new Interval(0, 6));
	// assertTrue(Iterables.elementsEqual(Arrays.asList(new Interval(0, 7)),
	// treeIntervalSet.getIntervals()));
	// treeIntervalSet = this.createIntervalSet();
	// treeIntervalSet.addInterval(new Interval(1, 3));
	// treeIntervalSet.addInterval(new Interval(5, 7));
	// treeIntervalSet.addInterval(new Interval(2, 8));
	// assertTrue(Iterables.elementsEqual(Arrays.asList(new Interval(1, 8)),
	// treeIntervalSet.getIntervals()));
	// treeIntervalSet = this.createIntervalSet();
	// treeIntervalSet.addInterval(new Interval(1, 3));
	// treeIntervalSet.addInterval(new Interval(5, 7));
	// treeIntervalSet.addInterval(new Interval(0, 8));
	// assertTrue(Iterables.elementsEqual(Arrays.asList(new Interval(0, 8)),
	// treeIntervalSet.getIntervals()));
	// }

	// @Test
	// public void testComplement() {
	// IntervalSet treeIntervalSet;
	// treeIntervalSet = new TreeIntervalSet(new Interval(0, 10));
	// assertTrue(Iterables.elementsEqual(Arrays.asList(new Interval(0, 10)),
	// treeIntervalSet.complement()(new Interval(0, 10))));
	// treeIntervalSet = new TreeIntervalSet(new Interval(0, 10));
	// treeIntervalSet.addInterval(new Interval(5, 10));
	// assertTrue(Iterables.elementsEqual(Arrays.asList(new Interval(0, 5)),
	// treeIntervalSet.getEmptyRanges(new Interval(0, 10))));
	// assertTrue(Iterables.elementsEqual(Arrays.asList(new Interval(0, 5)),
	// treeIntervalSet.getEmptyRanges(new Interval(0, 1))));
	// assertTrue(Iterables.elementsEqual(Arrays.asList(new Interval(0, 5)),
	// treeIntervalSet.getEmptyRanges(new Interval(4, 9))));
	// assertTrue(Iterables.elementsEqual(Arrays.asList(),
	// treeIntervalSet.getEmptyRanges(new Interval(5, 9))));
	// treeIntervalSet = new TreeIntervalSet(new Interval(0, 10));
	// treeIntervalSet.addInterval(new Interval(0, 2));
	// treeIntervalSet.addInterval(new Interval(8, 10));
	// assertTrue(Iterables.elementsEqual(Arrays.asList(),
	// treeIntervalSet.getEmptyRanges(new Interval(0, 2))));
	// assertTrue(Iterables.elementsEqual(Arrays.asList(),
	// treeIntervalSet.getEmptyRanges(new Interval(8, 10))));
	// assertTrue(Iterables.elementsEqual(Arrays.asList(new Interval(2, 8)),
	// treeIntervalSet.getEmptyRanges(new Interval(0, 3))));
	// assertTrue(Iterables.elementsEqual(Arrays.asList(new Interval(2, 8)),
	// treeIntervalSet.getEmptyRanges(new Interval(7, 9))));
	// assertTrue(Iterables.elementsEqual(Arrays.asList(new Interval(2, 8)),
	// treeIntervalSet.getEmptyRanges(new Interval(2, 8))));
	// assertTrue(Iterables.elementsEqual(Arrays.asList(new Interval(2, 8)),
	// treeIntervalSet.getEmptyRanges(new Interval(3, 8))));
	// assertTrue(Iterables.elementsEqual(Arrays.asList(new Interval(2, 8)),
	// treeIntervalSet.getEmptyRanges(new Interval(2, 7))));
	// treeIntervalSet = new TreeIntervalSet(new Interval(0, 12));
	// treeIntervalSet.addInterval(new Interval(1, 3));
	// treeIntervalSet.addInterval(new Interval(5, 7));
	// treeIntervalSet.addInterval(new Interval(9, 11));
	// assertTrue(Iterables.elementsEqual(
	// Arrays.asList(new Interval(0, 1), new Interval(3, 5), new Interval(7, 9),
	// new Interval(11, 12)),
	// treeIntervalSet.getEmptyRanges(new Interval(0, 12))));
	// assertTrue(Iterables.elementsEqual(Arrays.asList(new Interval(3, 5), new
	// Interval(7, 9), new Interval(11, 12)),
	// treeIntervalSet.getEmptyRanges(new Interval(1, 12))));
	// assertTrue(Iterables.elementsEqual(Arrays.asList(new Interval(0, 1), new
	// Interval(3, 5), new Interval(7, 9)),
	// treeIntervalSet.getEmptyRanges(new Interval(0, 11))));
	// }

}
