package pipi.interval;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

import com.google.common.collect.Iterables;

public class InvervalSetTest {
	@Test
	public void testBounds() {
		IntervalSet intervalSet;
		intervalSet = new IntervalSet(new Interval(0, 10));
		intervalSet.addRange(new Interval(-1, 1));
		intervalSet.addRange(new Interval(9, 11));
		assertTrue(Iterables.elementsEqual(Arrays.asList(new Interval(0, 1), new Interval(9, 10)), intervalSet.getRanges()));
		intervalSet = new IntervalSet(new Interval(0, 10));
		intervalSet.addRange(new Interval(-1, 11));
		assertTrue(Iterables.elementsEqual(Arrays.asList(new Interval(0, 10)), intervalSet.getRanges()));
	}

	@Test
	public void testOverlap() {
		IntervalSet intervalSet;
		intervalSet = new IntervalSet(new Interval(0, 10));
		intervalSet.addRange(new Interval(0, 1));
		intervalSet.addRange(new Interval(1, 2));
		assertTrue(Iterables.elementsEqual(Arrays.asList(new Interval(0, 2)), intervalSet.getRanges()));
		intervalSet = new IntervalSet(new Interval(0, 10));
		intervalSet.addRange(new Interval(1, 2));
		intervalSet.addRange(new Interval(0, 1));
		assertTrue(Iterables.elementsEqual(Arrays.asList(new Interval(0, 2)), intervalSet.getRanges()));
		intervalSet = new IntervalSet(new Interval(0, 10));
		intervalSet.addRange(new Interval(0, 2));
		intervalSet.addRange(new Interval(1, 3));
		assertTrue(Iterables.elementsEqual(Arrays.asList(new Interval(0, 3)), intervalSet.getRanges()));
		intervalSet = new IntervalSet(new Interval(0, 10));
		intervalSet.addRange(new Interval(1, 3));
		intervalSet.addRange(new Interval(0, 2));
		assertTrue(Iterables.elementsEqual(Arrays.asList(new Interval(0, 3)), intervalSet.getRanges()));
		intervalSet = new IntervalSet(new Interval(0, 10));
		intervalSet.addRange(new Interval(0, 2));
		intervalSet.addRange(new Interval(3, 5));
		assertTrue(Iterables.elementsEqual(Arrays.asList(new Interval(0, 2), new Interval(3, 5)), intervalSet.getRanges()));
		intervalSet = new IntervalSet(new Interval(0, 10));
		intervalSet.addRange(new Interval(1, 3));
		intervalSet.addRange(new Interval(5, 7));
		intervalSet.addRange(new Interval(2, 4));
		assertTrue(Iterables.elementsEqual(Arrays.asList(new Interval(1, 4), new Interval(5, 7)), intervalSet.getRanges()));
		intervalSet = new IntervalSet(new Interval(0, 10));
		intervalSet.addRange(new Interval(1, 3));
		intervalSet.addRange(new Interval(5, 7));
		intervalSet.addRange(new Interval(0, 6));
		assertTrue(Iterables.elementsEqual(Arrays.asList(new Interval(0, 7)), intervalSet.getRanges()));
		intervalSet = new IntervalSet(new Interval(0, 10));
		intervalSet.addRange(new Interval(1, 3));
		intervalSet.addRange(new Interval(5, 7));
		intervalSet.addRange(new Interval(2, 8));
		assertTrue(Iterables.elementsEqual(Arrays.asList(new Interval(1, 8)), intervalSet.getRanges()));
		intervalSet = new IntervalSet(new Interval(0, 10));
		intervalSet.addRange(new Interval(1, 3));
		intervalSet.addRange(new Interval(5, 7));
		intervalSet.addRange(new Interval(0, 8));
		assertTrue(Iterables.elementsEqual(Arrays.asList(new Interval(0, 8)), intervalSet.getRanges()));
	}

	@Test
	public void testEmptyRanges() {
		IntervalSet intervalSet;
		intervalSet = new IntervalSet(new Interval(0, 10));
		assertTrue(Iterables.elementsEqual(Arrays.asList(new Interval(0, 10)), intervalSet.getEmptyRanges(new Interval(0, 10))));
		intervalSet = new IntervalSet(new Interval(0, 10));
		intervalSet.addRange(new Interval(5, 10));
		assertTrue(Iterables.elementsEqual(Arrays.asList(new Interval(0, 5)), intervalSet.getEmptyRanges(new Interval(0, 10))));
		assertTrue(Iterables.elementsEqual(Arrays.asList(new Interval(0, 5)), intervalSet.getEmptyRanges(new Interval(0, 1))));
		assertTrue(Iterables.elementsEqual(Arrays.asList(new Interval(0, 5)), intervalSet.getEmptyRanges(new Interval(4, 9))));
		assertTrue(Iterables.elementsEqual(Arrays.asList(), intervalSet.getEmptyRanges(new Interval(5, 9))));
		intervalSet = new IntervalSet(new Interval(0, 10));
		intervalSet.addRange(new Interval(0, 2));
		intervalSet.addRange(new Interval(8, 10));
		assertTrue(Iterables.elementsEqual(Arrays.asList(), intervalSet.getEmptyRanges(new Interval(0, 2))));
		assertTrue(Iterables.elementsEqual(Arrays.asList(), intervalSet.getEmptyRanges(new Interval(8, 10))));
		assertTrue(Iterables.elementsEqual(Arrays.asList(new Interval(2, 8)), intervalSet.getEmptyRanges(new Interval(0, 3))));
		assertTrue(Iterables.elementsEqual(Arrays.asList(new Interval(2, 8)), intervalSet.getEmptyRanges(new Interval(7, 9))));
		assertTrue(Iterables.elementsEqual(Arrays.asList(new Interval(2, 8)), intervalSet.getEmptyRanges(new Interval(2, 8))));
		assertTrue(Iterables.elementsEqual(Arrays.asList(new Interval(2, 8)), intervalSet.getEmptyRanges(new Interval(3, 8))));
		assertTrue(Iterables.elementsEqual(Arrays.asList(new Interval(2, 8)), intervalSet.getEmptyRanges(new Interval(2, 7))));
		intervalSet = new IntervalSet(new Interval(0, 12));
		intervalSet.addRange(new Interval(1, 3));
		intervalSet.addRange(new Interval(5, 7));
		intervalSet.addRange(new Interval(9, 11));
		assertTrue(Iterables.elementsEqual(
				Arrays.asList(new Interval(0, 1), new Interval(3, 5), new Interval(7, 9), new Interval(11, 12)),
				intervalSet.getEmptyRanges(new Interval(0, 12))));
		assertTrue(Iterables.elementsEqual(Arrays.asList(new Interval(3, 5), new Interval(7, 9), new Interval(11, 12)),
				intervalSet.getEmptyRanges(new Interval(1, 12))));
		assertTrue(Iterables.elementsEqual(Arrays.asList(new Interval(0, 1), new Interval(3, 5), new Interval(7, 9)),
				intervalSet.getEmptyRanges(new Interval(0, 11))));
	}

	@Test
	public void testEmpty() {
		IntervalSet intervalSet;
		intervalSet = new IntervalSet(new Interval(0, 10));
		assertEquals(new Interval(0, 10), intervalSet.getEmptyRange(0));

//		line = new Line(new IntRange(0, 10));
//		line.addRange(new IntRange(5, 10));
//		assertTrue(Iterables.elementsEqual(Arrays.asList(new IntRange(0, 5)), line.getEmptyRanges(new IntRange(0, 10))));
//		assertTrue(Iterables.elementsEqual(Arrays.asList(new IntRange(0, 5)), line.getEmptyRanges(new IntRange(0, 1))));
//		assertTrue(Iterables.elementsEqual(Arrays.asList(new IntRange(0, 5)), line.getEmptyRanges(new IntRange(4, 9))));
//		assertTrue(Iterables.elementsEqual(Arrays.asList(), line.getEmptyRanges(new IntRange(5, 9))));
//		line = new Line(new IntRange(0, 10));
//		line.addRange(new IntRange(0, 2));
//		line.addRange(new IntRange(8, 10));
//		assertTrue(Iterables.elementsEqual(Arrays.asList(), line.getEmptyRanges(new IntRange(0, 2))));
//		assertTrue(Iterables.elementsEqual(Arrays.asList(), line.getEmptyRanges(new IntRange(8, 10))));
//		assertTrue(Iterables.elementsEqual(Arrays.asList(new IntRange(2, 8)), line.getEmptyRanges(new IntRange(0, 3))));
//		assertTrue(Iterables.elementsEqual(Arrays.asList(new IntRange(2, 8)), line.getEmptyRanges(new IntRange(7, 9))));
//		assertTrue(Iterables.elementsEqual(Arrays.asList(new IntRange(2, 8)), line.getEmptyRanges(new IntRange(2, 8))));
//		assertTrue(Iterables.elementsEqual(Arrays.asList(new IntRange(2, 8)), line.getEmptyRanges(new IntRange(3, 8))));
//		assertTrue(Iterables.elementsEqual(Arrays.asList(new IntRange(2, 8)), line.getEmptyRanges(new IntRange(2, 7))));
//		line = new Line(new IntRange(0, 12));
//		line.addRange(new IntRange(1, 3));
//		line.addRange(new IntRange(5, 7));
//		line.addRange(new IntRange(9, 11));
//		assertTrue(Iterables.elementsEqual(
//				Arrays.asList(new IntRange(0, 1), new IntRange(3, 5), new IntRange(7, 9), new IntRange(11, 12)),
//				line.getEmptyRanges(new IntRange(0, 12))));
//		assertTrue(Iterables.elementsEqual(Arrays.asList(new IntRange(3, 5), new IntRange(7, 9), new IntRange(11, 12)),
//				line.getEmptyRanges(new IntRange(1, 12))));
//		assertTrue(Iterables.elementsEqual(Arrays.asList(new IntRange(0, 1), new IntRange(3, 5), new IntRange(7, 9)),
//				line.getEmptyRanges(new IntRange(0, 11))));
	}

}
