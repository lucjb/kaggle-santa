package pipi.main;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.PriorityQueue;

import org.apache.commons.lang3.tuple.Pair;
import org.threeten.bp.Duration;
import org.threeten.bp.Instant;

import pipi.Dimension2d;
import pipi.Dimension3d;
import pipi.OutputPresent;
import pipi.PresentBatch;
import pipi.SuperPresent;
import pipi.SuperPresentsParser;
import pipi.interval.ExtendedRectangle;
import pipi.interval.IntervalSleigh;
import pipi.interval.Rectangle;
import pipi.packer.IntervalPacker;
import pipi.packer.Packer;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.util.concurrent.RateLimiter;

//FAKETIME_STOP_AFTER_SECONDS=10 faketime '2012-12-15 00:00:00' ./yjp.sh
public class Main {

	public static void main(String[] args) throws Exception {
		runMain("presents.csv");

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

	public static void runMain(String inputFileName) throws IOException {
		Instant start = Instant.now();
		List<SuperPresent> presents = new SuperPresentsParser().parse(inputFileName);

		IntervalSleigh sleigh = new IntervalSleigh();
		long totalVolume = 0;
		PriorityQueue<ExtendedRectangle> carryRectangles = new PriorityQueue<>();
		RateLimiter rateLimiter = RateLimiter.create(0.1);

		Packer buildPacker = buildPacker();

		for (int currentPresentIndex = 0; currentPresentIndex < presents.size();) {
			// System.out.println("---BATCH START---");
			int initialArea = 1000 * 1000;
			for (ExtendedRectangle extendedRectangle : carryRectangles) {
				initialArea -= extendedRectangle.rectangle.box2d.area();
			}
			PresentBatch presentBatch = new PresentBatch(initialArea);

			for (int j = currentPresentIndex; j < presents.size(); j++) {
				Dimension3d dimension = presents.get(j).getDimension();
				if (!presentBatch.pushPresent(dimension, 0)) {
					break;
				}
			}
			int bestBatchSize = presentBatch.size();
			// double bestBatchUsage = presentBatch.usage();;
			// for (;;) {
			// if (!presentBatch.canChangeMaximumZ()) {
			// break;
			// }
			// while(presentBatch.canChangeMaximumZ() &&
			// presentBatch.rotateMaximumZ()){
			// ;
			// }
			//
			// System.out.println(presentBatch.size() + "->" + presentBatch);
			// if(presentBatch.usage() > bestBatchUsage){
			// bestBatchUsage = presentBatch.usage();
			// bestBatchSize=presentBatch.size();
			// }
			// if (!presentBatch.canChangeMaximumZ()) {
			// break;
			// }
			// presentBatch.popPresent();
			// }
			// System.out.println("Best size: " + bestBatchSize + " usage: " +
			// bestBatchUsage);

			int batchEndIndex = currentPresentIndex + bestBatchSize;

			Pair<List<Rectangle>, Multimap<Dimension2d, SuperPresent>> pair = packNextBatch(presents, currentPresentIndex,
					batchEndIndex, carryRectangles, bestBatchSize, buildPacker);

			List<ExtendedRectangle> emitPresents = sleigh.emitPresents(pair.getLeft(), pair.getRight(), carryRectangles);
			buildPacker.freeAll(prefill(emitPresents));
			if (rateLimiter.tryAcquire()) {
				System.out.printf("Z: %d\n", sleigh.getCurrentZ());
				System.out.printf("Progress: %d\n", currentPresentIndex);
			}

			// totalVolume += emitPresents.getVolume();
			currentPresentIndex += pair.getLeft().size();
		}

		int maximumZ = sleigh.getCurrentZ();
		OutputPresent.outputPresents(sleigh.getOutputPresents(), maximumZ, "intervals.csv");
		System.out.printf("Total volume: %d\n", totalVolume);
		System.out.printf("%%waste: %2.2f\n", (double) totalVolume / (maximumZ * 1000000L));
		System.out.println("Final score: " + maximumZ * 2);
		System.out.println("Total minutes: " + Duration.between(start, Instant.now()).toMinutes());
	}

	private static Pair<List<Rectangle>, Multimap<Dimension2d, SuperPresent>> packNextBatch(List<SuperPresent> presents,
			int startIndex, int endIndex, Collection<ExtendedRectangle> carryRectangles, int bestBatchSize, Packer buildPacker) {
		List<Rectangle> packedPresents;
		List<SuperPresent> subPresents;
		Multimap<Dimension2d, SuperPresent> presentsWithDimension;

		int searchEnd = endIndex;
		int searchStart = endIndex;
		for (;;) {
			subPresents = presents.subList(startIndex, searchStart);
			Packer packer = buildPacker;
			presentsWithDimension = presentsWithDimension(subPresents);

//			Collection<Rectangle> prefill = prefill(carryRectangles);
//			packer.preFill(prefill);

			packedPresents = packer.packPesents(presentsWithDimension.keys());
			if (packedPresents.size() == subPresents.size()) {
				break;
			}
			packer.freeAll(packedPresents);
			searchEnd = searchStart;
			searchStart = searchStart - 1;
			// searchStart = (int) (searchStart * 0.9);
		}
/*		for (;;) {
			if (searchEnd - searchStart <= 1) {
				break;
			}
			if (searchEnd == searchEnd) {
				throw new RuntimeException("Todo mal");
			}
			int searchMid = searchStart + (searchEnd - searchStart) / 2;

			List<SuperPresent> subsubPresents = presents.subList(startIndex, searchMid);
			Packer packer = buildPacker();
			Multimap<Dimension2d, SuperPresent> subpresentsWithDimension = presentsWithDimension(subsubPresents);
			List<Rectangle> subpackedPresents = packer.packPesents(subpresentsWithDimension.keys());
			if (packedPresents.size() == subsubPresents.size()) {
				startIndex = searchMid;
				subPresents = subsubPresents;
				presentsWithDimension = subpresentsWithDimension;
				packedPresents = subpackedPresents;
				searchStart = searchMid;
			} else {
				searchEnd = searchMid;
			}
		}*/

		Pair<List<Rectangle>, Multimap<Dimension2d, SuperPresent>> pair = Pair.of(packedPresents, presentsWithDimension);

		// int maximumEndIndex = startIndex + bestBatchSize;
		// System.out.printf("Original: %d Real: %d Diff: %d n%%: %2.2f\n",
		// maximumEndIndex - startIndex,
		// packedPresents.size(), endIndex - startIndex - packedPresents.size(),
		// (double) packedPresents.size() / (maximumEndIndex - startIndex));
		return pair;
	}

	private static Collection<Rectangle> prefill(Collection<ExtendedRectangle> carryRectangles) {
		List<Rectangle> rectangles = Lists.newArrayList();
		for (ExtendedRectangle extendedRectangle : carryRectangles) {
			rectangles.add(extendedRectangle.rectangle);
		}
		return rectangles;
	}

	public static Packer buildPacker() {
		return new IntervalPacker();
		// return new CompositePacker(new IntervalPacker(), new
		// IntervalPacker(){@Override
		// protected Ordering<Dimension2d> getDimensionsOrdering() {
		// return IntervalPacker.MAXIMUM_DIMENSION_ORDERING;
		// }});
		// return new BrunoPacker();
	}

	private static Multimap<Dimension2d, SuperPresent> presentsWithDimension(List<SuperPresent> subPresents) {
		Multimap<Dimension2d, SuperPresent> presentsWithDimension;
		presentsWithDimension = HashMultimap.create();
		for (SuperPresent superPresent : subPresents) {
			Dimension3d dimension = superPresent.getDimension();
			Dimension2d smallFace = dimension.smallFace();
			presentsWithDimension.put(smallFace, superPresent);
		}
		return presentsWithDimension;
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
