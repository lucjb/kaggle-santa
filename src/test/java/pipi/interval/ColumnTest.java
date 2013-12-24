package pipi.interval;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Iterables;

public class ColumnTest {
	@Test
	public void testBounds() {
		Line line;
		line = new Line(new IntRange(0, 10));
		line.addRange(new IntRange(-1, 1));
		line.addRange(new IntRange(9, 11));
		assertTrue(Iterables.elementsEqual(Arrays.asList(new IntRange(0, 1), new IntRange(9, 10)), line.getRanges()));
		line = new Line(new IntRange(0, 10));
		line.addRange(new IntRange(-1, 11));
		assertTrue(Iterables.elementsEqual(Arrays.asList(new IntRange(0, 10)), line.getRanges()));
	}

	@Test
	public void testOverlap() {
		Line line;
		line = new Line(new IntRange(0, 10));
		line.addRange(new IntRange(0, 1));
		line.addRange(new IntRange(1, 2));
		assertTrue(Iterables.elementsEqual(Arrays.asList(new IntRange(0, 2)), line.getRanges()));
		line = new Line(new IntRange(0, 10));
		line.addRange(new IntRange(1, 2));
		line.addRange(new IntRange(0, 1));
		assertTrue(Iterables.elementsEqual(Arrays.asList(new IntRange(0, 2)), line.getRanges()));
		line = new Line(new IntRange(0, 10));
		line.addRange(new IntRange(0, 2));
		line.addRange(new IntRange(1, 3));
		assertTrue(Iterables.elementsEqual(Arrays.asList(new IntRange(0, 3)), line.getRanges()));
		line = new Line(new IntRange(0, 10));
		line.addRange(new IntRange(1, 3));
		line.addRange(new IntRange(0, 2));
		assertTrue(Iterables.elementsEqual(Arrays.asList(new IntRange(0, 3)), line.getRanges()));
		line = new Line(new IntRange(0, 10));
		line.addRange(new IntRange(0, 2));
		line.addRange(new IntRange(3, 5));
		assertTrue(Iterables.elementsEqual(Arrays.asList(new IntRange(0, 2), new IntRange(3, 5)), line.getRanges()));
		line = new Line(new IntRange(0, 10));
		line.addRange(new IntRange(1, 3));
		line.addRange(new IntRange(5, 7));
		line.addRange(new IntRange(2, 4));
		assertTrue(Iterables.elementsEqual(Arrays.asList(new IntRange(1, 4), new IntRange(5, 7)), line.getRanges()));
		line = new Line(new IntRange(0, 10));
		line.addRange(new IntRange(1, 3));
		line.addRange(new IntRange(5, 7));
		line.addRange(new IntRange(0, 6));
		assertTrue(Iterables.elementsEqual(Arrays.asList(new IntRange(0, 7)), line.getRanges()));
		line = new Line(new IntRange(0, 10));
		line.addRange(new IntRange(1, 3));
		line.addRange(new IntRange(5, 7));
		line.addRange(new IntRange(2, 8));
		assertTrue(Iterables.elementsEqual(Arrays.asList(new IntRange(1, 8)), line.getRanges()));
		line = new Line(new IntRange(0, 10));
		line.addRange(new IntRange(1, 3));
		line.addRange(new IntRange(5, 7));
		line.addRange(new IntRange(0, 8));
		assertTrue(Iterables.elementsEqual(Arrays.asList(new IntRange(0, 8)), line.getRanges()));
	}

	@Test
	public void testEmptyRanges() {
		Line line;
		line = new Line(new IntRange(0, 10));
		assertTrue(Iterables.elementsEqual(Arrays.asList(new IntRange(0, 10)), line.getEmptyRanges(new IntRange(0, 10))));
		line = new Line(new IntRange(0, 10));
		line.addRange(new IntRange(5, 10));
		assertTrue(Iterables.elementsEqual(Arrays.asList(new IntRange(0, 5)), line.getEmptyRanges(new IntRange(0, 10))));
		assertTrue(Iterables.elementsEqual(Arrays.asList(new IntRange(0, 5)), line.getEmptyRanges(new IntRange(0, 1))));
		assertTrue(Iterables.elementsEqual(Arrays.asList(new IntRange(0, 5)), line.getEmptyRanges(new IntRange(4, 9))));
		assertTrue(Iterables.elementsEqual(Arrays.asList(), line.getEmptyRanges(new IntRange(5, 9))));
		line = new Line(new IntRange(0, 10));
		line.addRange(new IntRange(0, 2));
		line.addRange(new IntRange(8, 10));
		assertTrue(Iterables.elementsEqual(Arrays.asList(), line.getEmptyRanges(new IntRange(0, 2))));
		assertTrue(Iterables.elementsEqual(Arrays.asList(), line.getEmptyRanges(new IntRange(8, 10))));
		assertTrue(Iterables.elementsEqual(Arrays.asList(new IntRange(2, 8)), line.getEmptyRanges(new IntRange(0, 3))));
		assertTrue(Iterables.elementsEqual(Arrays.asList(new IntRange(2, 8)), line.getEmptyRanges(new IntRange(7, 9))));
		assertTrue(Iterables.elementsEqual(Arrays.asList(new IntRange(2, 8)), line.getEmptyRanges(new IntRange(2, 8))));
		assertTrue(Iterables.elementsEqual(Arrays.asList(new IntRange(2, 8)), line.getEmptyRanges(new IntRange(3, 8))));
		assertTrue(Iterables.elementsEqual(Arrays.asList(new IntRange(2, 8)), line.getEmptyRanges(new IntRange(2, 7))));
		line = new Line(new IntRange(0, 12));
		line.addRange(new IntRange(1, 3));
		line.addRange(new IntRange(5, 7));
		line.addRange(new IntRange(9, 11));
		assertTrue(Iterables.elementsEqual(
				Arrays.asList(new IntRange(0, 1), new IntRange(3, 5), new IntRange(7, 9), new IntRange(11, 12)),
				line.getEmptyRanges(new IntRange(0, 12))));
		assertTrue(Iterables.elementsEqual(Arrays.asList(new IntRange(3, 5), new IntRange(7, 9), new IntRange(11, 12)),
				line.getEmptyRanges(new IntRange(1, 12))));
		assertTrue(Iterables.elementsEqual(Arrays.asList(new IntRange(0, 1), new IntRange(3, 5), new IntRange(7, 9)),
				line.getEmptyRanges(new IntRange(0, 11))));
	}

	@Test
	public void testEmpty() {
		Line line;
		line = new Line(new IntRange(0, 10));
		assertEquals(new IntRange(0, 10), line.getEmptyRange(0));

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
