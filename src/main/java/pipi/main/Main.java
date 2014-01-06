package pipi.main;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import pipi.Dimension2d;
import pipi.Dimension3d;
import pipi.OutputPresent;
import pipi.SuperPresent;
import pipi.SuperPresentsParser;
import pipi.interval.IntervalPacker;
import pipi.interval.IntervalSleigh;
import pipi.interval.Rectangle;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public class Main {

	public static void main(String[] args) throws Exception {
		List<SuperPresent> presents = new SuperPresentsParser().parse("presents.csv");

		IntervalSleigh sleigh = new IntervalSleigh();
		for (int currentPresentIndex = 0; currentPresentIndex < presents.size();) {
			int batchEndIndex = estimateNextBatchEndIndex(presents, currentPresentIndex, 1.00);

			Pair<List<Rectangle>, Multimap<Dimension2d, SuperPresent>> pair = packNextBatch(presents, currentPresentIndex,
					batchEndIndex);

			sleigh.emitPresents(pair.getLeft(), pair.getRight());
			currentPresentIndex += pair.getLeft().size();
		}

		int maximumZ = sleigh.getCurrentZ();
		// OutputPresent.outputPresents(sleigh.getOutputPresents(), maximumZ,
		// "intervals.csv");
		System.out.println(maximumZ * 2);

		// RateLimiter rateLimiter = RateLimiter.create(0.1);
		// int count = 0;
		// for (SuperPresent superPresent : presents) {
		// packer.putPesent(superPresent);
		// // System.out.printf("%d, %d, %d, %d\n", point.x, point.y, box3d.dx,
		// // box3d.dy);
		// count++;
		// if (rateLimiter.tryAcquire()) {
		// System.out.printf("Z: %d\n", packer.getCurrentZ());
		// System.out.printf("Progress: %d\n", count);
		// }
		// }
		// int maximumZ = packer.getLastZ();
		// System.out.println(maximumZ * 2);
	}

	private static int estimateNextBatchEndIndex(List<SuperPresent> presents, int currentPresentIndex, double efficiency) {
		return saturateAreaSmall(presents, currentPresentIndex, (int) ((1000 * 1000) * efficiency));
	}

	private static Pair<List<Rectangle>, Multimap<Dimension2d, SuperPresent>> packNextBatch(List<SuperPresent> presents,
			int currentPresentIndex, int originalEndIndex) {
		List<Rectangle> packedPresents;
		List<SuperPresent> subPresents;
		Multimap<Dimension2d, SuperPresent> presentsWithDimension;
		int endIndex = originalEndIndex + 1;
		do {
			endIndex--;
			subPresents = presents.subList(currentPresentIndex, endIndex);
			IntervalPacker packer = new IntervalPacker();
			presentsWithDimension = HashMultimap.create();

			for (SuperPresent superPresent : subPresents) {
				Dimension3d dimension = superPresent.getDimension();
				Dimension2d smallFace = dimension.smallFace();
				presentsWithDimension.put(smallFace, superPresent);
			}
			packedPresents = packer.packPesents(presentsWithDimension.keys());
		} while (packedPresents.size() != subPresents.size());
		Pair<List<Rectangle>, Multimap<Dimension2d, SuperPresent>> pair = Pair.of(packedPresents, presentsWithDimension);

		int maximumEndIndex = saturateAreaSmall(presents, currentPresentIndex, 1000 * 1000);
		System.out.printf("Original: %d Real: %d Diff: %d n%%: %2.2f\n", maximumEndIndex - currentPresentIndex,
				packedPresents.size(), originalEndIndex - currentPresentIndex - packedPresents.size(),
				(double) packedPresents.size() / (maximumEndIndex - currentPresentIndex));
		return pair;
	}

	private static int saturateAreaSmall(List<SuperPresent> presents, int start, int leftArea) {
		int j;
		for (j = start; j < presents.size(); j++) {
			SuperPresent superPresent = presents.get(j);
			int area = superPresent.getDimension().smallFace().area();
			int newArea = leftArea - area;
			if (newArea < 0) {
				break;
			}
			leftArea = newArea;
		}
		return j;
	}
}
