package pipi;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import pipi.bitmatrix.BitsetSleighSlice;
import pipi.interval.IntervalSlice;
import pipi.interval.Rectangle;

import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;

public class BruteForce {
	@SuppressWarnings("unchecked")
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
			BitsetSleighSlice bitsetSleighSlice = BitsetSleighSlice.freed();
			IntervalSlice intervalSlice = IntervalSlice.empty(size, size);
			for (int i = 0; i < xs.length; i++) {
				int x = xs[i];
				int y = ys[i];
				Box2d box2d = boxes.get(i);
				bitsetSleighSlice.fill(x, y, box2d.dx, box2d.dy);
				intervalSlice.fill(x, y, box2d.dx, box2d.dy);
			}
			BitsetSleighSlice validationSleighSlice = BitsetSleighSlice.filled();
			Collection<Rectangle> maximumRectangles = intervalSlice.getMaximumRectangles();
			for (Rectangle rectangle : maximumRectangles) {
				validationSleighSlice.free(rectangle.getHorizontalRange().getFrom(), rectangle.getVerticalRange().getFrom(),
						rectangle.getHorizontalRange().length(), rectangle.getVerticalRange().length());
			}
			if(!validationSleighSlice.equals(bitsetSleighSlice)){
				System.out.println("--NEGATIVE--");
				for (int i = 0; i < xs.length; i++) {
					int x = xs[i];
					int y = ys[i];
					Box2d box2d = boxes.get(i);
					System.out.printf("%d,%d,%d,%d\n",x, y, box2d.dx, box2d.dy);
				}

				negative++;
			}
			count++;
		} while (increment(xs, ys, boxes));
		System.out.println("TOTAL: "+count);
		System.out.println("NEGATIVE: "+negative);
		// List<Set<List<Integer>>> coordinatesSet = Lists.newArrayList();
		// for (Box2d box2d : boxes) {
		// ContiguousSet<Integer> xSet = ContiguousSet.create(Range.closed(0,
		// 1000 - box2d.dx), DiscreteDomain.integers());
		// ContiguousSet<Integer> ySet = ContiguousSet.create(Range.closed(0,
		// 1000 - box2d.dy), DiscreteDomain.integers());
		// Set<List<Integer>> coordinate = Sets.cartesianProduct(xSet, ySet);
		// coordinatesSet.add(coordinate);
		// }
		// Set<List<List<Integer>>> coordinatesProduct =
		// Sets.cartesianProduct(coordinatesSet);
		// for (List<List<Integer>> coordinates : coordinatesProduct) {
		// BitsetSleighSlice bitsetSleighSlice = BitsetSleighSlice.freed();
		// for (int i = 0; i < coordinates.size(); i++) {
		// List<Integer> coordinate = coordinates.get(i);
		// Box2d box2d = boxes.get(i);
		// // bitsetSleighSlice.fill(coordinate.get(0), coordinate.get(1),
		// box2d.dx, box2d.dy);
		// }
		// }

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
}
