package pipi.main;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Assert;

import pipi.Box2d;
import pipi.Point2d;
import pipi.interval.BitIntervalSet;
import pipi.interval.Interval;
import pipi.interval.IntervalSet;
import pipi.interval.IntervalSlice;
import pipi.interval.MaximumRectangle;
import pipi.interval.Perimeter;
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
		IntervalSlice perimeterSlice = IntervalSlice.empty(1000, 1000);
		for (Rectangle rectangle : rectangles) {
			boolean free = expectedSlice.isFree(rectangle.point2d.x, rectangle.point2d.y, rectangle.getBox2d().dx,
					rectangle.getBox2d().dy);
			boolean actualFree = intervalSlice.isFree(rectangle.point2d.x, rectangle.point2d.y, rectangle.getBox2d().dx,
					rectangle.getBox2d().dy);

			Assert.assertEquals(free, actualFree);
			expectedSlice.fill(rectangle.point2d.x, rectangle.point2d.y, rectangle.getBox2d().dx, rectangle.getBox2d().dy);

			intervalSlice.fill(rectangle.point2d.x, rectangle.point2d.y, rectangle.getBox2d().dx, rectangle.getBox2d().dy);
			perimeterSlice.fill(rectangle.point2d.y, rectangle.point2d.x, rectangle.getBox2d().dy, rectangle.getBox2d().dx);

			Assert.assertFalse(intervalSlice.isFree(rectangle.point2d.x, rectangle.point2d.y, rectangle.getBox2d().dx,
					rectangle.getBox2d().dy));

			Collection<MaximumRectangle> maximumRectangles = intervalSlice.getMaximumRectangles();
			BitsetSlice actualSlice = BitsetSlice.filled(1000);
			for (MaximumRectangle maximumRectangle : maximumRectangles) {
				actualSlice.free(maximumRectangle.rectangle.point2d.x, maximumRectangle.rectangle.point2d.y,
						maximumRectangle.rectangle.getBox2d().dx, maximumRectangle.rectangle.getBox2d().dy);
				Assert.assertTrue(intervalSlice.isFree(maximumRectangle.rectangle.point2d.x,
						maximumRectangle.rectangle.point2d.y, maximumRectangle.rectangle.getBox2d().dx,
						maximumRectangle.rectangle.getBox2d().dy));
				Perimeter expectedPerimeter = expectedPerimeter(maximumRectangle, expectedSlice);
				Perimeter perimeter = intervalSlice.getPerimeter(maximumRectangle.rectangle.point2d, maximumRectangle.rectangle.box2d, perimeterSlice);
				Assert.assertEquals(expectedPerimeter, perimeter);
			}
			Assert.assertEquals(expectedSlice, actualSlice);
		}
		Collection<MaximumRectangle> maximumRectangles = intervalSlice.getMaximumRectangles();
		for (MaximumRectangle rectangle : maximumRectangles) {
			for (MaximumRectangle other : maximumRectangles) {
				if (!rectangle.equals(other)) {
					Rectangle intersection = rectangle.rectangle.intersection(other.rectangle);
					Assert.assertNotEquals(intersection, rectangle);
				}
			}
		}
	}

	private static Perimeter expectedPerimeter(MaximumRectangle maximumRectangle, BitsetSlice expectedSlice) {
		Point2d point2d = maximumRectangle.rectangle.point2d;
		Box2d box2d = maximumRectangle.rectangle.box2d;

		IntervalSet perimeterLeft = new BitIntervalSet(1000);
		IntervalSet perimiterRight = new BitIntervalSet(1000);
		IntervalSet perimeterUp = new BitIntervalSet(1000);
		IntervalSet perimeterDown = new BitIntervalSet(1000);

		if (point2d.x == 0) {
			perimeterLeft.addInterval(new Interval(point2d.y, point2d.y + box2d.dy));
		} else {
			for (int y = point2d.y; y < point2d.y + box2d.dy; y++) {
				if (!expectedSlice.isFree(point2d.x - 1, y, 1, 1)) {
					perimeterLeft.addInterval(Interval.of(y, y + 1));
				}
			}
		}
		if (point2d.y == 0) {
			perimeterUp.addInterval(new Interval(point2d.x, point2d.x + box2d.dx));
		} else {
			for (int x = point2d.x; x < point2d.x + box2d.dx; x++) {
				if (!expectedSlice.isFree(x, point2d.y - 1, 1, 1)) {
					perimeterUp.addInterval(Interval.of(x, x + 1));
				}
			}
		}

		
		if (point2d.x + box2d.dx == 1000) {
			perimiterRight.addInterval(new Interval(point2d.y, point2d.y + box2d.dy));
		} else {
			for (int y = point2d.y; y < point2d.y + box2d.dy; y++) {
				if (!expectedSlice.isFree(point2d.x + box2d.dx, y, 1, 1)) {
					perimiterRight.addInterval(Interval.of(y, y + 1));
				}
			}
		}
		if (point2d.y + box2d.dy == 1000) {
			perimeterDown.addInterval(new Interval(point2d.x, point2d.x + box2d.dx));
		} else {
			for (int x = point2d.x; x < point2d.x + box2d.dx; x++) {
				if (!expectedSlice.isFree(x, point2d.y + box2d.dy, 1, 1)) {
					perimeterDown.addInterval(Interval.of(x, x + 1));
				}
			}
		}
		Perimeter perimeter = new Perimeter(perimeterLeft, perimiterRight, perimeterUp, perimeterDown);
		return perimeter;
	}
}
