package pipi.main;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.threeten.bp.Duration;
import org.threeten.bp.Instant;

import pipi.Dimension2d;
import pipi.Dimension3d;
import pipi.OutputPresent;
import pipi.PresentBatch;
import pipi.SuperPresent;
import pipi.SuperPresentsParser;
import pipi.interval.IntervalPacker;
import pipi.interval.IntervalSleigh;
import pipi.interval.Rectangle;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

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
		for (int currentPresentIndex = 0; currentPresentIndex < presents.size();) {
			System.out.println("---BATCH START---");
			PresentBatch presentBatch = new PresentBatch();

			for (int j = currentPresentIndex; j < presents.size(); j++) {
				Dimension3d dimension = presents.get(j).getDimension();
				if (!presentBatch.pushPresent(dimension, 0)) {
					break;
				}
			}
			int bestBatchSize=presentBatch.size();
//			double bestBatchUsage = presentBatch.usage();;
//			for (;;) {
//				if (!presentBatch.canChangeMaximumZ()) {
//					break;
//				}
//				while(presentBatch.canChangeMaximumZ() && presentBatch.rotateMaximumZ()){
//					;
//				}
//				
//				System.out.println(presentBatch.size() + "->" + presentBatch);
//				if(presentBatch.usage() > bestBatchUsage){
//					bestBatchUsage = presentBatch.usage();
//					bestBatchSize=presentBatch.size();
//				}
//				if (!presentBatch.canChangeMaximumZ()) {
//					break;
//				}
//				presentBatch.popPresent();
//			}
//			System.out.println("Best size: " + bestBatchSize + " usage: " + bestBatchUsage);

			int batchEndIndex = currentPresentIndex + bestBatchSize;

			Pair<List<Rectangle>, Multimap<Dimension2d, SuperPresent>> pair = packNextBatch(presents, currentPresentIndex,
					batchEndIndex);

			PresentBatch emitPresents = sleigh.emitPresents(pair.getLeft(), pair.getRight());
			totalVolume += emitPresents.getVolume();
			System.out.println("After-->" + emitPresents);
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
			int startIndex, int endIndex) {
		List<Rectangle> packedPresents;
		List<SuperPresent> subPresents;
		Multimap<Dimension2d, SuperPresent> presentsWithDimension;

		int searchEnd = endIndex;
		int searchStart = endIndex;
		for (;;) {
			subPresents = presents.subList(startIndex, searchStart);
			IntervalPacker packer = new IntervalPacker();
			presentsWithDimension = presentsWithDimension(subPresents);
			packedPresents = packer.packPesents(presentsWithDimension.keys());
			if (packedPresents.size() == subPresents.size()) {
				break;
			}
			searchEnd = searchStart;
			searchStart = searchStart - 1;
			// searchStart = (int) (searchStart * 0.9);
		}
		for (;;) {
			if (searchEnd - searchStart <= 1) {
				break;
			}
			int searchMid = searchStart + (searchEnd - searchStart) / 2;

			List<SuperPresent> subsubPresents = presents.subList(startIndex, searchMid);
			IntervalPacker packer = new IntervalPacker();
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
		}

		Pair<List<Rectangle>, Multimap<Dimension2d, SuperPresent>> pair = Pair.of(packedPresents, presentsWithDimension);

		int maximumEndIndex = saturateAreaSmall(presents, startIndex, 1000 * 1000);
		 System.out.printf("Original: %d Real: %d Diff: %d n%%: %2.2f\n",
		 maximumEndIndex - startIndex,
		 packedPresents.size(), endIndex - startIndex - packedPresents.size(),
		 (double) packedPresents.size() / (maximumEndIndex - startIndex));
		return pair;
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
