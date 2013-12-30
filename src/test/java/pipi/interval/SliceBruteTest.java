package pipi.interval;

import static org.junit.Assert.assertEquals;
import static pipi.interval.Rectangle.of;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import pipi.bitmatrix.BitsetSlice;

@RunWith(Parameterized.class)
public class SliceBruteTest {
	private final Rectangle[] rectangles;

	private static Object[] oa(Rectangle... rectangles) {
		return new Object[] { rectangles };
	}

	@Parameters
	public static Collection<Object[]> parameters() {
		return Arrays.<Object[]> asList(oa(of(1, 0, 998, 993), of(5, 1, 991, 995), of(0, 3, 999, 995)),
				oa(of(0, 0, 999, 997), of(2, 2, 998, 996), of(0, 0, 999, 1000)),
				oa(of(0, 0, 999, 997), of(2, 1, 998, 996), of(0, 0, 999, 1000)), oa(of(1, 1, 999, 999), of(0, 1, 998, 998)),

				oa(of(8, 325, 8, 15), of(13, 270, 8, 28), of(13, 298, 4, 4), of(17, 260, 5, 8)),

				oa(of(289, 0, 101, 104), of(271, 104, 31, 32)), oa(of(0, 0, 2, 3), of(0, 210, 78, 160))
				,oa(of(0, 0, 10, 10), of(500, 0, 10, 50))
				);
	}

	public SliceBruteTest(Rectangle... rectangles) {
		this.rectangles = rectangles;
	}

	@Test
	public void testArea() {
		BitsetSlice expectedSlice = BitsetSlice.freed(1000);
		IntervalSlice intervalSlice = IntervalSlice.empty(1000, 1000);
		for (Rectangle rectangle : this.rectangles) {
			expectedSlice.fill(rectangle.getPoint2d().getX(), rectangle.getPoint2d().getY(), rectangle.getBox2d().dx,
					rectangle.getBox2d().dy);
			intervalSlice.fill(rectangle.getPoint2d().getX(), rectangle.getPoint2d().getY(), rectangle.getBox2d().dx,
					rectangle.getBox2d().dy);
		}
		BitsetSlice actualSlice = BitsetSlice.filled(1000);
		Collection<MaximumRectangle> maximumRectangles = intervalSlice.getMaximumRectangles();
		System.out.println(maximumRectangles);
		for (MaximumRectangle maximumRectangle : maximumRectangles) {
			actualSlice.free(maximumRectangle.getHorizontalRange().getFrom(), maximumRectangle.getVerticalRange().getFrom(),
					maximumRectangle.getHorizontalRange().length(), maximumRectangle.getVerticalRange().length());
		}
		assertEquals(expectedSlice, actualSlice);
		for (MaximumRectangle maximumRectangle : maximumRectangles) {
			for (MaximumRectangle other : maximumRectangles) {
				if(!maximumRectangle.equals(other)){
					MaximumRectangle intersection = maximumRectangle.intersect(other);
					Assert.assertNotEquals(maximumRectangle, intersection);
				}
			}
		}
	}

}
