package pipi;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import pipi.bitmatrix.BitsetSlice;
import pipi.interval.IntervalSlice;
import pipi.interval.MaximumRectangle;
import pipi.interval.Rectangle;

import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;

public class BruteForce {
	static int size = 1000;
	public static void main(String[] args) {
		// List<Box2d> boxes = Arrays.asList(new Box2d(3, 2), new Box2d(4, 1),
		// new Box2d(2, 1));
//		 List<Box2d> boxes = Arrays.asList(new Box2d(20, 5), new Box2d(10,
//		 10), new Box2d(5, 20));
		List<Box2d> boxes = Arrays.asList(new Box2d(998, 993), new Box2d(991, 995), new Box2d(999, 995));
		int[] xs = new int[boxes.size()];
		int[] ys = new int[boxes.size()];
		long count=0;
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
			if(!BruteForce.assertRectangles(rectangles)){
				System.out.println("--NEGATIVE--");
				for (int i = 0; i < xs.length; i++) {
					int x = xs[i];
					int y = ys[i];
					Box2d box2d = boxes.get(i);
					System.out.printf("%d,%d,%d,%d\n",x, y, box2d.dx, box2d.dy);
				}
				negative++;
				return;
			}
			count++;
		} while (increment(xs, ys, boxes));
		System.out.println("TOTAL: "+count);
		System.out.println("NEGATIVE: "+negative);

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

	public static boolean assertRectangles(List<Rectangle> rectangles) {
		BitsetSlice expectedSlice = BitsetSlice.freed(1000);
		IntervalSlice intervalSlice = IntervalSlice.empty(1000, 1000);
		for (Rectangle rectangle : rectangles) {
			expectedSlice.fill(rectangle.getPoint2d().getX(), rectangle.getPoint2d().getY(), rectangle.getBox2d().dx,
					rectangle.getBox2d().dy);
			intervalSlice.fill(rectangle.getPoint2d().getX(), rectangle.getPoint2d().getY(), rectangle.getBox2d().dx,
					rectangle.getBox2d().dy);
			Collection<MaximumRectangle> maximumRectangles = intervalSlice.getMaximumRectangles();
			BitsetSlice actualSlice = BitsetSlice.filled(1000);
			for (MaximumRectangle maximumRectangle : maximumRectangles) {
				actualSlice.free(maximumRectangle.getHorizontalRange().getFrom(), maximumRectangle.getVerticalRange().getFrom(),
						maximumRectangle.getHorizontalRange().length(), maximumRectangle.getVerticalRange().length());
			}
			if(!expectedSlice.equals(actualSlice)){
				return false;
			}
		}
		Collection<MaximumRectangle> maximumRectangles = intervalSlice.getMaximumRectangles();
//		System.out.println(maximumRectangles);
		for (MaximumRectangle maximumRectangle : maximumRectangles) {
			for (MaximumRectangle other : maximumRectangles) {
				if(!maximumRectangle.equals(other)){
					MaximumRectangle intersection = maximumRectangle.intersect(other);
					if(maximumRectangle.equals(intersection)){
						return false;
					}
				}
			}
		}
		return true;
	}
}
