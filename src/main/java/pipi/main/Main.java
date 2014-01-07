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
		 OutputPresent.outputPresents(sleigh.getOutputPresents(), maximumZ,
		 "intervals.csv");
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
			int startIndex, int endIndex) {
		List<Rectangle> packedPresents;
		List<SuperPresent> subPresents;
		Multimap<Dimension2d, SuperPresent> presentsWithDimension;

		int searchEnd = endIndex;
		int searchStart = endIndex;
		for(;;){
			subPresents = presents.subList(startIndex, searchStart);
			IntervalPacker packer = new IntervalPacker();
			presentsWithDimension = presentsWithDimension(subPresents);
			packedPresents = packer.packPesents(presentsWithDimension.keys());
			if(packedPresents.size() == subPresents.size()){
				break;
			}
			searchEnd = searchStart;
			searchStart = searchStart - 1;
//			searchStart = (int) (searchStart * 0.9);
		}
		for(;;){
			if(searchEnd - searchStart <= 1){
				break;
			}
			int searchMid = searchStart + (searchEnd - searchStart) / 2;

			List<SuperPresent> subsubPresents = presents.subList(startIndex, searchMid);
			IntervalPacker packer = new IntervalPacker();
			Multimap<Dimension2d, SuperPresent>  subpresentsWithDimension = presentsWithDimension(subsubPresents);
			List<Rectangle> subpackedPresents = packer.packPesents(subpresentsWithDimension.keys());
			if(packedPresents.size() == subsubPresents.size()){
				startIndex = searchMid;
				subPresents = subsubPresents;
				presentsWithDimension = subpresentsWithDimension;
				packedPresents = subpackedPresents;
				searchStart = searchMid;
			}else{
				searchEnd = searchMid;
			}
		}
		
		
		Pair<List<Rectangle>, Multimap<Dimension2d, SuperPresent>> pair = Pair.of(packedPresents, presentsWithDimension);

		int maximumEndIndex = saturateAreaSmall(presents, startIndex, 1000 * 1000);
		System.out.printf("Original: %d Real: %d Diff: %d n%%: %2.2f\n", maximumEndIndex - startIndex,
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
