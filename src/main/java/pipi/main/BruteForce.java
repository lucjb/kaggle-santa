package pipi.main;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Assert;

import pipi.Box2d;
import pipi.interval.IntervalSlice;
import pipi.interval.Rectangle;
import pipi.sandbox.BitsetSlice;

import com.google.common.collect.Lists;

public class BruteForce {
	static int size = 1000;

	public static void main(String[] args) {
		// List<Box2d> boxes = Arrays.asList(new Box2d(3, 2), new Box2d(4, 1),
		// new Box2d(2, 1));
		// List<Box2d> boxes = Arrays.asList(new Box2d(20, 5), new Box2d(10,
		// 10), new Box2d(5, 20));
		List<Box2d> boxes = Arrays.asList(new Box2d(998, 996), new Box2d(996, 998), new Box2d(997, 997));
		int[] xs = new int[boxes.size()];
		int[] ys = new int[boxes.size()];
		long count = 0;
		long negative = 0;
		do {
			// for (int i = 0; i < boxes.size(); i++) {
			// System.out.printf("(%d, %d) ", xs[i], ys[i]);
			// }
			// System.out.println();
			List<Rectangle> rectangles = Lists.newArrayList();
			for (int i = 0; i < xs.length; i++) {
				int x = xs[i];
				int y = ys[i];
				Box2d box2d = boxes.get(i);
				rectangles.add(Rectangle.of(x, y, box2d.dx, box2d.dy));
			}
			try {
				BruteForce.assertRectangles(rectangles);
			} catch (AssertionError assertionError) {
				System.out.println("--NEGATIVE--");
				for (int i = 0; i < xs.length; i++) {
					int x = xs[i];
					int y = ys[i];
					Box2d box2d = boxes.get(i);
					System.out.printf("%d,%d,%d,%d\n", x, y, box2d.dx, box2d.dy);
				}
				negative++;
				return;
			}
			count++;
		} while (increment(xs, ys, boxes));
		System.out.println("TOTAL: " + count);
		System.out.println("NEGATIVE: " + negative);

	}

	private static boolean increment(int[] xs, int[] ys, List<Box2d> boxes) {
		for (int i = xs.length - 1; i >= 0; i--) {
			ys[i]++;
			if (ys[i] > size - boxes.get(i).dy) {
				ys[i] = 0;
				xs[i]++;
				if (xs[i] > size - boxes.get(i).dx) {
					xs[i] = 0;
					continue;
				}
			}
			return true;
		}
		return false;
	}

	public static void assertRectangles(List<Rectangle> rectangles) {
		BitsetSlice expectedSlice = BitsetSlice.freed(1000);
		IntervalSlice intervalSlice = IntervalSlice.empty(1000, 1000);
		for (Rectangle rectangle : rectangles) {
			boolean free = expectedSlice.isFree(rectangle.point2d.x, rectangle.point2d.y, rectangle.getBox2d().dx,
					rectangle.getBox2d().dy);
			boolean actualFree = intervalSlice.isFree(rectangle.point2d.x, rectangle.point2d.y,
					rectangle.getBox2d().dx, rectangle.getBox2d().dy);
			
			Assert.assertEquals(free,actualFree);
			expectedSlice.fill(rectangle.point2d.x, rectangle.point2d.y, rectangle.getBox2d().dx,
					rectangle.getBox2d().dy);

			intervalSlice.fill(rectangle.point2d.x, rectangle.point2d.y, rectangle.getBox2d().dx,
					rectangle.getBox2d().dy);

			
			
			Assert.assertFalse(intervalSlice.isFree(rectangle.point2d.x, rectangle.point2d.y,
					rectangle.getBox2d().dx, rectangle.getBox2d().dy));

			Collection<Rectangle> maximumRectangles = intervalSlice.getMaximumRectangles();
			BitsetSlice actualSlice = BitsetSlice.filled(1000);
			for (Rectangle maximumRectangle : maximumRectangles) {
				actualSlice.free(maximumRectangle.point2d.x, maximumRectangle.point2d.y,
						maximumRectangle.getBox2d().dx, maximumRectangle.getBox2d().dy);
				Assert.assertTrue(intervalSlice.isFree(maximumRectangle.point2d.x, maximumRectangle.point2d.y, maximumRectangle.getBox2d().dx, maximumRectangle.getBox2d().dy));
			}
			Assert.assertEquals(expectedSlice, actualSlice);
		}
		Collection<Rectangle> maximumRectangles = intervalSlice.getMaximumRectangles();
		for (Rectangle rectangle : maximumRectangles) {
			for (Rectangle other : maximumRectangles) {
				if (!rectangle.equals(other)) {
					Rectangle intersection = rectangle.intersection(other);
					Assert.assertNotEquals(intersection, rectangle);
				}
			}
		}
	}
}
