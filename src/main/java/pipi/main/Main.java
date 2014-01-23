package pipi.main;

import java.awt.Color;
import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
import pipi.packer.CompositePacker;
import pipi.packer.IntervalPacker;
import pipi.packer.Packer;
import pipi.sleigh.FloorStructure;
import pipi.sleigh.RectangleFloor;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset.Entry;
import com.google.common.collect.Ordering;
import com.google.common.collect.SortedMultiset;
import com.google.common.collect.TreeMultiset;
import com.google.common.primitives.Doubles;

//FAKETIME_STOP_AFTER_SECONDS=20 faketime '2012-12-15 00:00:00' ./yjp.sh
//FAKETIME_START_AFTER_NUMCALLS???
/*
 * * CompositePacker que ande bien 																		---DONE---
 ** Ponderar diferente los de los bordes?
 ** Ponderar diferente los que "desperdicien" menos? los que dejen espacio para algun bloque que quede?
 ** Ordenar por perímetro?																				---DONE---
 ** Agregar estadisticas al composite packer																---DONE---
 * Probar meter en Z sin que joda
 ** Calcular la cantidad máxima que podrian entrar
 ** ir de arriba para abajo (warning, aca hay que mantener el orden!!!)
 * Empujar en Z
 ** Probar los 4 reflejos
 ** Reordenar paquetes de igual base (antes o despues?)
 * Arreglar el preseleccionado
 ** intentar en un heap hasta que quede el mejor
 ** una vez elegido el maximo, seguir tumbando!
 */

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

//		RateLimiter rateLimiter = RateLimiter.create(0.1);

		ExecutorService newFixedThreadPool = Executors.newFixedThreadPool(6);
		Packer buildPacker = buildPacker(newFixedThreadPool);

		int initialArea = 1000 * 1000;
		for (int currentPresentIndex = 0; currentPresentIndex < presents.size();) {
			 System.out.println("---BATCH START---");
			// for (ExtendedRectangle extendedRectangle : carryRectangles) {
			// initialArea -= extendedRectangle.rectangle.box2d.area();
			// }
			PresentBatch presentBatch = new PresentBatch(initialArea);

			for (int j = currentPresentIndex; j < presents.size(); j++) {
				Dimension3d dimension = presents.get(j).getDimension();

//				Dimension2d smallFace = dimension.getFace(0);
//				Dimension2d largeFace = dimension.getFace(2);
				int rotation;
				// if (smallFace.squareness() >= largeFace.squareness()) {
				rotation = 0;
				// } else {
				// rotation = 2;
				// }

				if (!presentBatch.pushPresent(dimension, rotation)) {
					break;
				}
			}
			// List<SuperPresent> inspectedPresents =
			// presents.subList(currentPresentIndex,
			// Math.min(currentPresentIndex + 240, presents.size()));
			// SortedMultiset<Integer> maximumHistogram =
			// createHistogram(inspectedPresents, 2);
			// SortedMultiset<Integer> minimumHistogram =
			// createHistogram(inspectedPresents, 0);
			// System.out.println("MAX");
			// printHistogram(maximumHistogram);
			// System.out.println("MIN");
			// printHistogram(minimumHistogram);

			PackResult pair = packNextBatch(presents,
					presentBatch, currentPresentIndex, buildPacker);

			sleigh.emitPresents(pair.getBestPackedPresents(), pair.getBestPresents(), floorStructure);

			totalVolume += pair.getBestPresentBatch().getVolume();
			// buildPacker.freeAll(prefill(emitPresents));
			// if (rateLimiter.tryAcquire()) {
			System.out.printf("Z: %d\n", floorStructure.getCurrentZ());
			System.out.printf("Progress: %d\n", currentPresentIndex);
			System.out.printf("%%: %1.8f\n", (double) totalVolume / (floorStructure.maxZ() * 1000000L));
			System.out.println(buildPacker);
			// }

			currentPresentIndex += pair.getBestPackedPresents().size();

			initialArea = initialArea - presentBatch.getArea();

			// showFloorStructure(floorStructure);

			initialArea += popFloors(floorStructure, buildPacker);

			// assert initialArea == 1000 * 1000;
			// assert buildPacker.isEmpty();
		}
		while ((floorStructure.popFloor()) != null) {
			System.out.println("FALTABA POPEAR!!!");
		}

		int maximumZ = floorStructure.getCurrentZ();
		OutputPresent.outputPresents(sleigh.getOutputPresents(), maximumZ, "intervals.csv");
		System.out.printf("Total volume: %d\n", totalVolume);
		System.out.printf("%%: %1.8f\n", (double) totalVolume / (maximumZ * 1000000L));
		System.out.println("Final score: " + maximumZ * 2);
		System.out.println("Total minutes: " + Duration.between(start, Instant.now()).toMinutes());
		System.out.println(buildPacker);
		newFixedThreadPool.shutdown();
	}

	public static void printHistogram(SortedMultiset<Integer> maximumHistogram) {
		Set<Entry<Integer>> entrySet = maximumHistogram.entrySet();
		for (Entry<Integer> entry : entrySet) {
			System.out.println(entry);
		}
	}

	public static SortedMultiset<Integer> createHistogram(List<SuperPresent> inspectedPresents, int dimension) {
		SortedMultiset<Integer> maximumHistogram = TreeMultiset.create();
		for (SuperPresent superPresent : inspectedPresents) {
			maximumHistogram.add(superPresent.getDimension().getDimension(dimension));
		}
		return maximumHistogram;
	}

	public static int popFloors(FloorStructure floorStructure, Packer buildPacker) {
		// return lessOnePopFloors(floorStructure, buildPacker,
		// floorStructure.floorCount() - 1);
		// return lessOnePopFloors(floorStructure, buildPacker, (int)
		// (floorStructure.floorCount() * 1.0));
		return shelfPopFloor(floorStructure, buildPacker);
		// return nextPopFloor(floorStructure, buildPacker);
	}

	public static int lessOnePopFloors(FloorStructure floorStructure, Packer buildPacker, int count) {
		RectangleFloor popFloor;
		if (count == 0) {
			count = 1;
		}
		int area = 0;
		while (count > 0 && (popFloor = floorStructure.popFloor()) != null) {
			area += updatePoppedFloor(buildPacker, popFloor);
			count--;
		}
		return area;
	}

	public static int shelfPopFloor(FloorStructure floorStructure, Packer buildPacker) {
		RectangleFloor popFloor;
		int area = 0;
		while ((popFloor = floorStructure.popFloor()) != null) {
			area += updatePoppedFloor(buildPacker, popFloor);
		}
		return area;
	}

	public static int nextPopFloor(FloorStructure floorStructure, Packer buildPacker) {
		return updatePoppedFloor(buildPacker, floorStructure.popFloor());
	}

	public static int updatePoppedFloor(Packer buildPacker, RectangleFloor popFloor) {
		int area = 0;
		Collection<Rectangle> rectangles = popFloor.getRectangles();
		for (Rectangle rectangle : rectangles) {
			area += rectangle.box2d.area();
		}
		buildPacker.freeAll(rectangles);
		return area;
	}

	public static void showFloorStructure(FloorStructure floorStructure) {
		List<RectangleSet> rectangleSets = Lists.newArrayList();
		List<RectangleFloor> rectangleFloors = floorStructure.getRectangleFloors();


		for (RectangleFloor rectangleFloor : rectangleFloors) {
			float rgb = (float) (rectangleFloor.getHeight() - floorStructure.getCurrentZ()) / 250;
			rectangleSets.add(new RectangleSet(colorForHeight(rgb), rectangleFloor.getRectangles()));
		}
		// if(presents.get(currentPresentIndex).getOrder() > 700000) {
		RectangleView.show(rectangleSets);
		// }
	}

	private static Color[] colors = { Color.BLUE, Color.CYAN.darker(), Color.GREEN, Color.YELLOW, Color.RED };

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
		Color color = new Color(r, g, b);

		return color;
	}

	private static PackResult packNextBatch(
			List<SuperPresent> presents, PresentBatch maximumPresentBatch, int startIndex, Packer buildPacker) {

		PriorityQueue<PresentBatch> presentBatchs = new PriorityQueue<>(maximumPresentBatch.size(), new Comparator<PresentBatch>() {

			@Override
			public int compare(PresentBatch o1, PresentBatch o2) {
				return Doubles.compare(o2.usage(), o1.usage());
			}
		});

		for (;;) {
			if (!maximumPresentBatch.canChangeMaximumZ()) {
				break;
			}
			while (maximumPresentBatch.canChangeMaximumZ() && maximumPresentBatch.rotateMaximumZ()) {
				;
			}

			presentBatchs.offer(maximumPresentBatch.copy());
			if (!maximumPresentBatch.canChangeMaximumZ()) {
				break;
			}
			maximumPresentBatch.popPresent();
		}
		maximumPresentBatch = null;
		
		List<PutRectangle> bestPackedPresents;
		PresentBatch bestPresentBatch = presentBatchs.peek();
		double maximumUsage = bestPresentBatch.usage(); 
		Multimap<OrientedDimension3d, SuperPresent> bestPresents;

		for(;;){
			PresentBatch presentBatch = presentBatchs.poll();
			List<SuperPresent> subPresents;
			subPresents = presents.subList(startIndex, startIndex + presentBatch.size());

			Packer packer = buildPacker;
			bestPresents = presentsWithDimension(subPresents, presentBatch.getPresents());
			
			List<PutRectangle> thisPackedPresents = packer.packPesents(bestPresents.keys());

			System.out.printf("Original: %03d Real: %03d Diff: %03d\n", presentBatch.size(), thisPackedPresents.size(), presentBatch.size() - thisPackedPresents.size());

			if (thisPackedPresents.size() == presentBatch.size()) {
				bestPackedPresents = thisPackedPresents;
				break;
			}

			packer.freeAll(prefree(thisPackedPresents));

			presentBatch.popPresent();
			presentBatchs.offer(presentBatch);
		}
		
		System.out.printf("MaxUsage: %1.4f ActualUsage: %1.4f\n", maximumUsage, bestPresentBatch.usage());
		
		return new PackResult(bestPackedPresents, bestPresents, bestPresentBatch);
		//		return pair;
	}

	private static List<Rectangle> prefree(List<PutRectangle> packedPresents) {
		List<Rectangle> rectangles = Lists.newArrayList();
		for (PutRectangle extendedRectangle : packedPresents) {
			rectangles.add(extendedRectangle.rectangle);
		}
		return rectangles;
	}

	public static Packer buildPacker(ExecutorService newFixedThreadPool) {
		// return new IntervalPacker();
		return new CompositePacker(newFixedThreadPool, new IntervalPacker(), new IntervalPacker() {
			@Override
			protected Ordering<OrientedDimension3d> getDimensionsOrdering() {
				return IntervalPacker.MAXIMUM_DIMENSION_ORDERING;
			}

			public String toString() {
				return "IntervalPacker[DIMENSION]";
			}

		}, new IntervalPacker() {
			@Override
			protected Ordering<OrientedDimension3d> getDimensionsOrdering() {
				return IntervalPacker.PERIMETER_ORDERING;
			}

			public String toString() {
				return "IntervalPacker[PERIMETER]";
			}

		}, new IntervalPacker() {
			@Override
			protected Ordering<OrientedDimension3d> getDimensionsOrdering() {
				return IntervalPacker.AREA_ORDERING.compound(IntervalPacker.PERIMETER_ORDERING).compound(
						IntervalPacker.MAXIMUM_DIMENSION_ORDERING);
			}

			public String toString() {
				return "IntervalPacker[AREA|PERIMETER|DIMENSION]";
			}

		});
		// return new BrunoPacker();
	}

	private static Multimap<OrientedDimension3d, SuperPresent> presentsWithDimension(List<SuperPresent> presents,
			List<OrientedDimension3d> orientedPresents) {
		Multimap<OrientedDimension3d, SuperPresent> presentsWithDimension = HashMultimap.create();
		for (int i = 0; i < presents.size(); i++) {
			SuperPresent superPresent = presents.get(i);
			OrientedDimension3d orientedDimension3d = orientedPresents.get(i);
			presentsWithDimension.put(orientedDimension3d, superPresent);
		}
		return presentsWithDimension;
	}
}
