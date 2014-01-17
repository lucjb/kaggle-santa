package pipi.main;

import java.awt.Color;
import java.io.IOException;
import java.sql.BatchUpdateException;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.threeten.bp.Duration;
import org.threeten.bp.Instant;

import pipi.Dimension3d;
import pipi.OrientedDimension3d;
import pipi.OutputPresent;
import pipi.PresentBatch;
import pipi.SuperPresent;
import pipi.SuperPresentsParser;
import pipi.gui.RectangleSet;
import pipi.gui.RectangleView;
import pipi.interval.IntervalSleigh;
import pipi.interval.PutRectangle;
import pipi.interval.Rectangle;
import pipi.packer.IntervalPacker;
import pipi.packer.Packer;
import pipi.sleigh.FloorStructure;
import pipi.sleigh.RectangleFloor;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.util.concurrent.RateLimiter;

//FAKETIME_STOP_AFTER_SECONDS=10 faketime '2012-12-15 00:00:00' ./yjp.sh
public class Main {

	public static void main(String[] args) throws Exception {
		runMain("presents.csv");
	}

	public static void runMain(String inputFileName) throws IOException {
		Instant start = Instant.now();
		List<SuperPresent> presents = new SuperPresentsParser().parse(inputFileName);

		IntervalSleigh sleigh = new IntervalSleigh();
		long totalVolume = 0;
		FloorStructure floorStructure = new FloorStructure();

		RateLimiter rateLimiter = RateLimiter.create(0.1);

		Packer buildPacker = buildPacker();

		int initialArea = 1000 * 1000;
		for (int currentPresentIndex = 0; currentPresentIndex < presents.size();) {
			System.out.println("---BATCH START---");
			// for (ExtendedRectangle extendedRectangle : carryRectangles) {
			// initialArea -= extendedRectangle.rectangle.box2d.area();
			// }
			PresentBatch presentBatch = new PresentBatch(initialArea);

			for (int j = currentPresentIndex; j < presents.size(); j++) {
				Dimension3d dimension = presents.get(j).getDimension();
				if (!presentBatch.pushPresent(dimension, 2)) {
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
//			 System.out.println("Best size: " + bestBatchSize + " usage: " +
//			 bestBatchUsage);

			int batchEndIndex = currentPresentIndex + bestBatchSize;

			Pair<List<PutRectangle>, Multimap<OrientedDimension3d, SuperPresent>> pair = packNextBatch(presents, presentBatch, currentPresentIndex,
					batchEndIndex, bestBatchSize, buildPacker);

			sleigh.emitPresents(pair.getLeft(), pair.getRight(), floorStructure);

			// buildPacker.freeAll(prefill(emitPresents));
			// if (rateLimiter.tryAcquire()) {
			// System.out.printf("Z: %d\n", sleigh.getCurrentZ());
			// System.out.printf("Progress: %d\n", currentPresentIndex);
			// }

			// totalVolume += emitPresents.getVolume();
			List<Rectangle> left = prefree(pair.getLeft());
			currentPresentIndex += left.size();
			// List<Rectangle> left = pair.getLeft();
			initialArea = 1000 * 1000;
			// for (Rectangle rectangle : left) {
			// initialArea -= rectangle.box2d.area();
			// }
			// for (ExtendedRectangle extendedRectangle : emitPresents) {
			// initialArea+=extendedRectangle.rectangle.box2d.area();
			// }
			List<RectangleSet> rectangleSets = Lists.newArrayList();
			RectangleFloor floor = null;
			List<RectangleFloor> rectangleFloors = Lists.newArrayList();
			while ((floor = floorStructure.popFloor()) != null) {
				rectangleFloors.add(floor);
			}

			int min = rectangleFloors.get(0).getHeight();
			int max = rectangleFloors.get(rectangleFloors.size() - 1).getHeight();
			int span = max - min;

			for (RectangleFloor rectangleFloor : rectangleFloors) {
				float rgb = (float) (rectangleFloor.getHeight() - min) / span;
				rectangleSets.add(new RectangleSet(colorForHeight(rgb), rectangleFloor.getRectangles()));
			}
//			if(presents.get(currentPresentIndex).getOrder() > 700000) {
				RectangleView.show(rectangleSets);
//			}

			buildPacker.freeAll((left));
			assert buildPacker.isEmpty();
		}

		int maximumZ = floorStructure.getCurrentZ();
		OutputPresent.outputPresents(sleigh.getOutputPresents(), maximumZ, "intervals.csv");
		System.out.printf("Total volume: %d\n", totalVolume);
		System.out.printf("%%waste: %2.2f\n", (double) totalVolume / (maximumZ * 1000000L));
		System.out.println("Final score: " + maximumZ * 2);
		System.out.println("Total minutes: " + Duration.between(start, Instant.now()).toMinutes());
	}

	private static Color[] colors = { Color.BLUE, Color.GREEN, Color.YELLOW, Color.RED };

	public static Color colorForHeight(float heightRatio) {

		float delta = 1.0f / (colors.length - 1);
		int index = (int) ((colors.length - 1) * heightRatio);
		int nextIndex = index + 1;
		if (nextIndex == colors.length) {
			nextIndex = index;
		}
		float ratio = (heightRatio - index * delta) / delta;
		int r = (int) (colors[index].getRed() * (1 - ratio) + colors[nextIndex].getRed() * ratio);
		int g = (int) (colors[index].getGreen() * (1 - ratio) + colors[nextIndex].getGreen() * ratio);
		int b = (int) (colors[index].getBlue() * (1 - ratio) + colors[nextIndex].getBlue() * ratio);
		Color color = new Color(r,g,b);

		
		return color;
	}

	private static Pair<List<PutRectangle>, Multimap<OrientedDimension3d, SuperPresent>> packNextBatch(List<SuperPresent> presents, PresentBatch presentBatch,
			int startIndex, int endIndex, int bestBatchSize, Packer buildPacker) {
		List<PutRectangle> packedPresents;
		List<SuperPresent> subPresents;
		Multimap<OrientedDimension3d, SuperPresent> presentsWithDimension;

		int searchEnd = endIndex;
		int searchStart = endIndex;
		for (;;) {
			subPresents = presents.subList(startIndex, searchStart);
			Packer packer = buildPacker;
			presentsWithDimension = presentsWithDimension(subPresents, presentBatch.getPresents());

			// Collection<Rectangle> prefill = prefill(carryRectangles);
			// packer.preFill(prefill);

			packedPresents = packer.packPesents(presentsWithDimension.keys());
			if (packedPresents.size() == subPresents.size()) {
				break;
			}
			packer.freeAll(prefree(packedPresents));
			searchEnd = searchStart;
			searchStart = searchStart - 1;
			presentBatch.popPresent();
			// searchStart = (int) (searchStart * 0.9);
		}
		/*
		 * for (;;) { if (searchEnd - searchStart <= 1) { break; } if (searchEnd
		 * == searchEnd) { throw new RuntimeException("Todo mal"); } int
		 * searchMid = searchStart + (searchEnd - searchStart) / 2;
		 * 
		 * List<SuperPresent> subsubPresents = presents.subList(startIndex,
		 * searchMid); Packer packer = buildPacker(); Multimap<Dimension2d,
		 * SuperPresent> subpresentsWithDimension =
		 * presentsWithDimension(subsubPresents); List<Rectangle>
		 * subpackedPresents =
		 * packer.packPesents(subpresentsWithDimension.keys()); if
		 * (packedPresents.size() == subsubPresents.size()) { startIndex =
		 * searchMid; subPresents = subsubPresents; presentsWithDimension =
		 * subpresentsWithDimension; packedPresents = subpackedPresents;
		 * searchStart = searchMid; } else { searchEnd = searchMid; } }
		 */

		Pair<List<PutRectangle>, Multimap<OrientedDimension3d, SuperPresent>> pair = Pair.of(packedPresents, presentsWithDimension);

		int maximumEndIndex = startIndex + bestBatchSize;
		System.out.printf("Original: %d Real: %d Diff: %d n%%: %2.2f\n", maximumEndIndex - startIndex,
				packedPresents.size(), endIndex - startIndex - packedPresents.size(), (double) packedPresents.size()
						/ (maximumEndIndex - startIndex));
		System.out.println(presentBatch);
		return pair;
	}



	private static List<Rectangle> prefree(List<PutRectangle> packedPresents) {
		List<Rectangle> rectangles = Lists.newArrayList();
		for (PutRectangle extendedRectangle : packedPresents) {
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

	private static Multimap<OrientedDimension3d, SuperPresent> presentsWithDimension(List<SuperPresent> presents, List<OrientedDimension3d> orientedPresents) {
		Multimap<OrientedDimension3d, SuperPresent> presentsWithDimension = HashMultimap.create();
		for(int i=0; i< presents.size(); i++){
			SuperPresent superPresent = presents.get(i);
			OrientedDimension3d orientedDimension3d = orientedPresents.get(i);
			presentsWithDimension.put(orientedDimension3d, superPresent);
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
