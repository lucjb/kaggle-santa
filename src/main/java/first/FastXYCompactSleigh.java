package first;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import au.com.bytecode.opencsv.CSVWriter;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.google.common.primitives.Ints;

public class FastXYCompactSleigh {

	static final int MAX = 1000;

	interface Rotator {
		public void rotate(Present present, Surface2D surface);
	}

	class DefaultRotator implements Rotator {

		@Override
		public void rotate(Present present, Surface2D surface) {
			present.rotateMedMinMax();
		}
	}

	public List<Present> sortNatural(List<Present> layer) {
		Comparator<Present> comparator = naturalComparator();
		List<Present> sortedCopy = Ordering.from(comparator).sortedCopy(layer);
		return sortedCopy;
	}

	private Comparator<Present> naturalComparator() {
		return new Comparator<Present>() {

			@Override
			public int compare(Present o1, Present o2) {
				return Ints.compare(o1.order, o2.order);
			}
		};
	}

	private Comparator<Present> zSupComparator() {
		return new Comparator<Present>() {

			@Override
			public int compare(Present o1, Present o2) {
				int compare = -Ints.compare(o1.zSize, o2.zSize);
				if (compare != 0) {
					return compare;
				}
				return -Ints.compare(o1.xSize * o1.ySize, o2.xSize * o2.ySize);
			}
		};
	}

	private Comparator<Present> maxSideComparator() {
		return new Comparator<Present>() {

			@Override
			public int compare(Present o1, Present o2) {
				return -Ints.compare(Math.max(o1.xSize, o1.ySize), Math.max(o2.xSize, o2.ySize));
			}
		};
	}

	static class Point2D implements Comparable<Point2D> {
		int x;
		int y;
		Point2D originalInsertionPoint;

		Point2D(int x, int y) {
			this.x = x;
			this.y = y;
		}

		@Override
		public int compareTo(Point2D o) {
			int sumComp = Integer.compare(x + y, o.x + o.y);
			if (sumComp != 0)
				return sumComp;
			return Integer.compare(x, o.x);
		}

		@Override
		public String toString() {
			return "(" + x + "," + y + ")";
		}
	}

	class Surface2D implements Cloneable {
		int zLevel = 0;
		int maxPresentHeight = 0;
		private BitSet surface = new BitSet(MAX * MAX);
		SortedSet<Point2D> blInsertionPoints;
		SortedSet<Point2D> tlInsertionPoints;
		SortedSet<Point2D> brInsertionPoints;
		SortedSet<Point2D> trInsertionPoints;
		int[][] top = new int[1000][1000];
		int[][] topBackup = cloneTop(top);

		private int[][] cloneTop(int[][] top) {
			int[][] m = new int[top.length][];
			for (int i = 0; i < top.length; i++) {
				int[] aMatrix = top[i];
				int aLength = aMatrix.length;
				m[i] = new int[aLength];
				System.arraycopy(aMatrix, 0, m[i], 0, aLength);
			}
			return m;
		}

		private void pushDown(List<Present> layer, Surface2D surface) {
			List<Present> sortNatural = sortNatural(layer);
			int lastPushDown = Integer.MAX_VALUE;
			for (Present p : sortNatural) {
				if (p.order == 237)
					System.out.println("pushed down");
				Point insertPoint = p.location();
				int maxZBelow = maxZBelow(p);
				int floatingSpace = Math.min(insertPoint.z - maxZBelow - 1, lastPushDown);

				if (floatingSpace == 0) {
					break;
				}
				lastPushDown = floatingSpace;

				for (int xi = insertPoint.x - 1; xi < insertPoint.x - 1 + p.xSize; xi++) {
					for (int yi = insertPoint.y - 1; yi < insertPoint.y - 1 + p.ySize; yi++) {
						top[xi][yi] -= floatingSpace;
					}
				}
				for (Point b : p.boundaries) {
					b.z -= floatingSpace;
				}
			}
			System.out.println("-----------");
			computeMaxZFromTop(surface);
		}

		private void computeMaxZFromTop(Surface2D surface) {
			int maxZ = -1;
			for (int xi = 0; xi < 1000; xi++) {
				for (int yi = 0; yi < 1000; yi++) {
					if (top[xi][yi] > maxZ) {
						maxZ = top[xi][yi];
					}
				}
			}

			surface.maxPresentHeight = maxZ - surface.zLevel;
		}

		private int maxZBelow(Present present) {
			Point insertPoint = present.location();
			int maxZBelow = 0;
			for (int xi = insertPoint.x - 1; xi < insertPoint.x - 1 + present.xSize; xi++) {
				for (int yi = insertPoint.y - 1; yi < insertPoint.y - 1 + present.ySize; yi++) {
					int zi = topBackup[xi][yi];
					if (zi > maxZBelow) {
						maxZBelow = zi;
					}
				}
			}
			return maxZBelow;
		}

		public Surface2D() {
			initInsertionPoints();
		}

		private void initInsertionPoints() {
			blInsertionPoints = new TreeSet<Point2D>(new Comparator<FastXYCompactSleigh.Point2D>() {
				@Override
				public int compare(Point2D o1, Point2D o2) {
					int sumComp = Integer.compare(o1.x + o1.y, o2.x + o2.y);
					if (sumComp != 0)
						return sumComp;
					return Integer.compare(o1.x, o2.x);
				}
			});
			blInsertionPoints.add(new Point2D(0, 0));
			brInsertionPoints = new TreeSet<Point2D>(new Comparator<FastXYCompactSleigh.Point2D>() {
				@Override
				public int compare(Point2D o1, Point2D o2) {
					int sumComp = Integer.compare(o1.x + o1.y, o2.x + o2.y);
					if (sumComp != 0)
						return sumComp;
					return -Integer.compare(o1.x, o2.x);
				}
			});
			brInsertionPoints.add(new Point2D(MAX - 1, 0));

			tlInsertionPoints = new TreeSet<Point2D>(new Comparator<FastXYCompactSleigh.Point2D>() {
				@Override
				public int compare(Point2D o1, Point2D o2) {
					int sumComp = -Integer.compare(o1.x + o1.y, o2.x + o2.y);
					if (sumComp != 0)
						return sumComp;
					return Integer.compare(o1.x, o2.x);
				}
			});
			tlInsertionPoints.add(new Point2D(0, MAX - 1));

			trInsertionPoints = new TreeSet<Point2D>(new Comparator<FastXYCompactSleigh.Point2D>() {
				@Override
				public int compare(Point2D o1, Point2D o2) {
					int sumComp = -Integer.compare(o1.x + o1.y, o2.x + o2.y);
					if (sumComp != 0)
						return sumComp;
					return -Integer.compare(o1.x, o2.x);
				}
			});
			trInsertionPoints.add(new Point2D(MAX - 1, MAX - 1));

		}

		public boolean occupied(int x, int y) {
			return surface.get(x * MAX + y);
		}

		public void occupy(int x, int y) {
			surface.set(x * MAX + y);
		}

		public void free(int x, int y) {
			surface.clear(x * MAX + y);
		}

		public void clear() {
			surface.clear();
			maxPresentHeight = 0;
			blInsertionPoints.clear();
			blInsertionPoints.add(new Point2D(0, 0));
			brInsertionPoints.clear();
			brInsertionPoints.add(new Point2D(MAX - 1, 0));
			tlInsertionPoints.clear();
			tlInsertionPoints.add(new Point2D(0, MAX - 1));
			trInsertionPoints.clear();
			trInsertionPoints.add(new Point2D(MAX - 1, MAX - 1));
			top = cloneTop(topBackup);

		}

		@Override
		protected Object clone() throws CloneNotSupportedException {
			Surface2D clone = new Surface2D();
			clone.surface = (BitSet) surface.clone();
			clone.blInsertionPoints = (SortedSet<Point2D>) ((TreeSet<Point2D>) blInsertionPoints).clone();
			clone.brInsertionPoints = (SortedSet<Point2D>) ((TreeSet<Point2D>) brInsertionPoints).clone();
			clone.tlInsertionPoints = (SortedSet<Point2D>) ((TreeSet<Point2D>) tlInsertionPoints).clone();
			clone.trInsertionPoints = (SortedSet<Point2D>) ((TreeSet<Point2D>) trInsertionPoints).clone();

			clone.zLevel = zLevel;
			clone.maxPresentHeight = maxPresentHeight;
			return clone;
		}
	}

	int layerCount = 1;

	public void addPresents(List<Present> presents) {
		int zeroBasedMaxOccupiedZ = addReordering(presents);
		// int zeroBasedMaxOccupiedZ = addReorderingWithOneHeuristic(presents);

		System.out.println("Total layers: " + layerCount);

		for (Present present : presents) {
			for (int i = 0; i < 8; i++) {
				present.boundaries.get(i).z = zeroBasedMaxOccupiedZ + 1 - present.boundaries.get(i).z;
			}
		}
	}

	private List<Present> clonePresents(List<Present> presents) {
		List<Present> presentsClones = new ArrayList<Present>(MAX * MAX);
		for (Present present : presents) {
			try {
				presentsClones.add((Present) present.clone());
			} catch (CloneNotSupportedException e) {
				throw new RuntimeException(e);
			}
		}
		return presentsClones;
	}

	private int addReorderingWithOneHeuristic(List<Present> presents) {
		Surface2D floor = new Surface2D();
		int i = 0;
		while (i < presents.size()) {
			// int added = fillLayerReordering(presents, i, floor,
			// perimeterTouchCriterion(floor, false));
			// int added = fillLayerReordering(presents, i, floor,
			// totalAreaComparator());
			int added = fillLayerReordering(presents, i, floor, areaComparator());
			// int added = fillLayerReordering(presents, i, floor,
			// areaComparator());

			// List<Present> presentsInLayer = Lists.newLinkedList();
			// for (int j = 0; j < added; j++) {
			// presentsInLayer.add(presents.get(i + j));
			// }
			// pushDown(presentsInLayer, floor);
			int next = i + added;
			if (layerCount % 10 == 0 && next < MAX * MAX) {
				System.out.println("Layer: " + layerCount + ", presents: " + added + ", total: " + next);
				int area = 0;
				for (int j = i; j < next; j++) {
					Present p = presents.get(j);
					area += (p.xSize * p.ySize);
				}
				System.out.println("Free area: " + (MAX * MAX - area) + ", didn't fit: "
						+ (presents.get(next).xSize * presents.get(next).ySize));
			}

			i += added;
			layerCount++;

			if (i < presents.size()) {
				startNewLayer(floor);
			}
		}

		return floor.zLevel + floor.maxPresentHeight;
	}

	private int addReordering(List<Present> presents) {
		int i = 0;

		// Surface2D floor = new Surface2D(new
		// Comparator<FastXYCompactSleigh.Point2D>() {
		// @Override
		// public int compare(Point2D o1, Point2D o2) {
		// int o1Min = Math.min(o1.x, o1.y);
		// int o2Min = Math.min(o2.x, o2.y);
		// int minComp = Integer.compare(o1Min, o2Min);
		// if (minComp != 0)
		// return minComp;
		//
		// int o1Max = Math.max(o1.x, o1.y);
		// int o2Max = Math.max(o2.x, o2.y);
		// int maxComp = Integer.compare(o1Max, o2Max);
		// if (maxComp != 0)
		// return maxComp;
		//
		// return Integer.compare(o1.x, o2.y);
		// }
		// });

		Surface2D floor = new Surface2D();
		Surface2D floor2 = new Surface2D();

		List<Present> presents2 = clonePresents(presents);

		int added1 = 0;
		int added2 = 0;
		while (i < presents.size()) {
			added1 = fillLayerReordering(presents, i, floor, minusAreaComparator());
			added2 = fillLayerReordering(presents2, i, floor2, areaComparator());

			int added;
			if (added1 >= added2) {
				floor2.maxPresentHeight = floor.maxPresentHeight;
				floor2.top = floor.top;
				List<Present> presentsInLayer = Lists.newLinkedList();
				for (int j = 0; j < added1; j++) {
					presentsInLayer.add(presents.get(i + j));
				}
				floor.pushDown(presentsInLayer, floor);

				added = added1;
			} else {
				floor.maxPresentHeight = floor2.maxPresentHeight;
				floor.top = floor2.top;
				for (int j = i; j < i + added2; j++) {
					presents.get(j).boundaries = presents2.get(j).boundaries;
				}
				List<Present> presentsInLayer = Lists.newLinkedList();
				for (int j = 0; j < added2; j++) {
					presentsInLayer.add(presents2.get(i + j));
				}
				floor2.pushDown(presentsInLayer, floor2);

				added = added2;
			}

			int next = i + added;
			if (layerCount % 10 == 0 && next < MAX * MAX) {
				System.out.println("Layer: " + layerCount + ", presents: " + added + ", total: " + next);
				int area = 0;
				for (int j = i; j < next; j++) {
					Present p = presents.get(j);
					area += (p.xSize * p.ySize);
				}
				System.out.println("Free area: " + (MAX * MAX - area) + ", didn't fit: "
						+ (presents.get(next).xSize * presents.get(next).ySize));
			}

			i += added;
			layerCount++;
			if (i < presents.size()) {
				startNewLayer(floor);
				startNewLayer(floor2);
			}
		}

		if (added1 >= added2) {
			return floor.zLevel + floor.maxPresentHeight;
		} else
			return floor2.zLevel + floor2.maxPresentHeight;
	}

	int h = -1;
	int lowestFitH = -1;

	private int fillLayerReordering(List<Present> presents, int start, final Surface2D surface, Comparator sortingCriterion) {
		List<Present> layerOrder = Lists.newArrayList();
		int i = 0;
		int added = 0;
		DefaultRotator defaultRotator = new DefaultRotator();

		for (i = start; i < presents.size(); i++) {
			Present present = presents.get(i);
			layerOrder.add(present);
			if (!add(present, surface, false, defaultRotator)) {
				h = surface.maxPresentHeight;
				undoLayerWithPresents(layerOrder, surface);
				List<Present> sortedLayer = Ordering.from(sortingCriterion).sortedCopy(layerOrder);
				if (addAll(sortedLayer, surface, false, defaultRotator)) {
					layerOrder = sortedLayer;
				} else {
					undoLayerWithPresents(sortedLayer, surface);
					layerOrder.remove(present);
					//
					// if (!addAll(layerOrder, surface, true, defaultRotator)) {
					// throw new RuntimeException("foo!");
					// }

					lowestFitH = -1;
					boolean fit = true;
					while (fit) {

						Rotator tumbador = new Rotator() {

							@Override
							public void rotate(Present present, Surface2D surface) {
								present.rotateMinMedMax();
								if (present.zSize >= h) {
									present.rotateMinMaxMed();
									if (present.zSize >= h) {
										present.rotateMedMaxMin();
									}

								}
							}
						};

						for (Present present2 : layerOrder) {
							tumbador.rotate(present2, surface);
						}
						sortedLayer = Ordering.from(sortingCriterion).sortedCopy(layerOrder);

						fit = addAll(sortedLayer, surface, true, tumbador);

						if (!fit) {
							undoLayerWithPresents(sortedLayer, surface);
							if (lowestFitH > -1) {
								Rotator rotator = new Rotator() {

									@Override
									public void rotate(Present present, Surface2D surface) {
										present.rotateMinMedMax();
										if (present.zSize >= lowestFitH) {
											present.rotateMinMaxMed();
											if (present.zSize >= lowestFitH) {
												present.rotateMedMaxMin();
											}

										}

									}
								};

								for (Present present2 : sortedLayer) {
									rotator.rotate(present2, surface);
								}
								sortedLayer = Ordering.from(sortingCriterion).sortedCopy(sortedLayer);
								boolean addAll = addAll(sortedLayer, surface, true, rotator);
								if (!addAll) {
									throw new RuntimeException("kernel panic");
								}
							} else {
								if (!addAll(layerOrder, surface, true, defaultRotator)) {
									throw new RuntimeException("foo!");
								}
							}

						} else {
							System.out.println("Improved!");
							lowestFitH = h;
							h = surface.maxPresentHeight;
							if (h == lowestFitH) {
								int jump = completeInTheMiddle(layerOrder, presents, i, surface);
								added += jump;
								break;
							}
							undoLayerWithPresents(sortedLayer, surface);
						}
					}
					// if (layerCount == 1) {
					// try {
					// viewXY(layerOrder);
					// BrunoMain.generateCSV(layerOrder);
					// } catch (IOException e) {
					// e.printStackTrace();
					// }
					// System.exit(0);
					// }
					int jump = completeInTheMiddle(layerOrder, presents, i, surface);
					// surface.pushDown(layerOrder, surface);
					added += jump;
					break;
				}
			}
			added++;
		}

		return added;
	}

	// private int completeInTheMiddle2(List<Present> layerOrder, List<Present>
	// presents, int i, Surface2D floor) {
	//
	// int zLowLimit = 0;
	// int count = 0;
	//
	// for (int j = 0; i + j < presents.size(); j++) {
	// Present present = presents.get(i + j);
	// Point lowestInsertionPoint = zInsertionPoint(present, floor);
	// if (lowestInsertionPoint == null)
	// return count;
	// if (lowestInsertionPoint.z < zLowLimit)
	// lowestInsertionPoint.z = zLowLimit;
	//
	// if (lowestInsertionPoint.z + present.zSize > floor.zLevel +
	// floor.maxPresentHeight)
	// return count;
	// zLowLimit = lowestInsertionPoint.z;
	// for (int p = lowestInsertionPoint.x; p < lowestInsertionPoint.x +
	// present.xSize; p++) {
	// for (int q = lowestInsertionPoint.y; q < lowestInsertionPoint.y +
	// present.ySize; q++) {
	// floor.top[p][q] += present.zSize;
	// }
	// }
	// lowestInsertionPoint.x++;
	// lowestInsertionPoint.y++;
	// setBoundaries(present, lowestInsertionPoint);
	// count++;
	// }
	// return count;
	// }

	// private Point lowestInsertionPoint(Present present) {
	// Point zInsertionPoint = zInsertionPoint(present);
	// present.rotate();
	// Point zInsertionPoint2 = zInsertionPoint(present);
	//
	// if (zInsertionPoint == null && zInsertionPoint2 != null)
	// return zInsertionPoint2;
	// if (zInsertionPoint != null && zInsertionPoint2 == null)
	// return zInsertionPoint;
	// if (zInsertionPoint.z < zInsertionPoint2.z)
	// return zInsertionPoint;
	// return zInsertionPoint2;
	// }
	//
	// private Point zInsertionPoint(Present present, Surface2D surface) {
	// for (int x = 0; x < MAX - present.xSize; x++) {
	// for (int y = 0; y < MAX - present.ySize; y++) {
	// Point insertionPoint = new Point(x, y, -1);
	// insertionPoint.z = z(insertionPoint, present);
	// if (insertionPoint.z + present.zSize <= surface.zLevel +
	// surface.maxPresentHeight)
	// return insertionPoint;
	// }
	// }
	// return null;
	// }

	// private Point zInsertionPoint(Present present) {
	// Point lowestInsertionPoint = null;
	// for (int x = 0; x < MAX - present.xSize; x++) {
	// for (int y = 0; y < MAX - present.ySize; y++) {
	// Point insertionPoint = new Point(x, y, -1);
	// insertionPoint.z = z(insertionPoint, present);
	// if (lowestInsertionPoint == null || insertionPoint.z <
	// lowestInsertionPoint.z) {
	// lowestInsertionPoint = insertionPoint;
	// }
	// }
	// }
	// return lowestInsertionPoint;
	// }

	// private int z(Point insertionPoint, Present present) {
	// int highestOccupiedZ = -1;
	// for (int p = insertionPoint.x; p < insertionPoint.x + present.xSize; p++)
	// {
	// for (int q = insertionPoint.y; q < insertionPoint.y + present.ySize; q++)
	// {
	// if (top[p][q] > highestOccupiedZ)
	// highestOccupiedZ = top[p][q];
	// }
	// }
	// return highestOccupiedZ + 1;
	// }

	private int completeInTheMiddle(List<Present> layerOrder, List<Present> presents, int i, Surface2D floor) {

		Map<Integer, Set<Present>> heights = Maps.newLinkedHashMap();
		for (Present present : layerOrder) {
			int height = present.zSize;
			if (!heights.containsKey(height)) {
				heights.put(height, Sets.<Present> newLinkedHashSet());
			}
			heights.get(height).add(present);
		}

		List<Integer> heightsLowToHigh = Lists.newArrayList(heights.keySet());
		Collections.sort(heightsLowToHigh, Ordering.natural());

		// Create freeAtHeightMap bottom-up (highest level should be empty)
		Surface2D lowerLevel = floor;
		Map<Integer, Surface2D> freeAtHeight = Maps.newLinkedHashMap();
		for (Integer height : heightsLowToHigh) {
			Surface2D free;
			try {
				free = (Surface2D) lowerLevel.clone();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			free.zLevel = height;

			for (Present present : heights.get(height)) {
				Point oneBasedInsertionPoint = present.boundaries.get(0);
				Point2D zeroBasedInsertionPoint = new Point2D(oneBasedInsertionPoint.x - 1, oneBasedInsertionPoint.y - 1);
				free.blInsertionPoints.add(zeroBasedInsertionPoint);
				clear(free, zeroBasedInsertionPoint, present.xSize, present.ySize);
			}
			freeAtHeight.put(height, free);
			lowerLevel = free;
		}

		int inserted = 0;
		while (true) {
			Present present = presents.get(i + inserted);
			Point insertionPoint3D = fitsIn3D(heightsLowToHigh, freeAtHeight, present, floor);
			if (insertionPoint3D != null) {
				// System.out.println("iupiiiii! " + insertionPoint3D + " " +
				// present);
				inserted++;
				setBoundaries(present, new Point(insertionPoint3D.x + 1, insertionPoint3D.y + 1, insertionPoint3D.z + 1));
				for (int p = insertionPoint3D.x; p < insertionPoint3D.x + present.xSize; p++) {
					for (int q = insertionPoint3D.y; q < insertionPoint3D.y + present.ySize; q++) {
						floor.top[p][q] = Math.max(present.maxZ(), floor.top[p][q]);
					}
				}

				int relativeZ = insertionPoint3D.z - floor.zLevel;
				int insertionHeightIndex = Collections.binarySearch(heightsLowToHigh, relativeZ);
				if (insertionHeightIndex < 0)
					throw new RuntimeException("foo");
				while (heightsLowToHigh.get(insertionHeightIndex) < relativeZ + present.zSize) {
					int height = heightsLowToHigh.get(insertionHeightIndex);
					Surface2D surface = freeAtHeight.get(height);
					occupy(surface, insertionPoint3D.x, insertionPoint3D.y, present.xSize, present.ySize, true);
					Iterator<Point2D> it = surface.blInsertionPoints.iterator();
					while (it.hasNext()) {
						Point2D next = it.next();
						if (insertionPoint3D.x <= next.x && next.x <= insertionPoint3D.x + present.xSize - 1) {
							if (insertionPoint3D.y <= next.y && next.y <= insertionPoint3D.y + present.ySize - 1) {
								it.remove();
							}
						}
					}
					insertionHeightIndex++;
				}

				if (!freeAtHeight.containsKey(relativeZ + present.zSize)) {
					insertionHeightIndex--; // First one lower than the current
											// present
					int height = heightsLowToHigh.get(insertionHeightIndex);
					Surface2D newLevel;
					try {
						newLevel = (Surface2D) freeAtHeight.get(height).clone();
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
					newLevel.zLevel = relativeZ + present.zSize;
					clear(newLevel, new Point2D(insertionPoint3D.x, insertionPoint3D.y), present.xSize, present.ySize);
				}

				// Remove lower levels, we don't want to disorder presents
				Iterator<Integer> hi = heightsLowToHigh.iterator();
				while (hi.hasNext()) {
					Integer next = hi.next();
					if (next < relativeZ) {
						hi.remove();
						freeAtHeight.remove(next);
					} else
						break;
				}
			} else
				break;
		}

		return inserted;
	}

	private void clear(Surface2D surface, Point2D point, int xSize, int ySize) {
		for (int p = point.x + xSize - 1; p >= point.x; p--) {
			for (int q = point.y + ySize - 1; q >= point.y; q--) {
				surface.free(p, q);
			}
		}
	}

	public Comparator<Present> perimeterTouchCriterion(final Surface2D surface, final boolean solidFill) {
		return new Comparator<Present>() {

			@Override
			public int compare(Present o1, Present o2) {
				int i = -Double
						.compare(maxPerimeterTouch(surface, solidFill, o1), maxPerimeterTouch(surface, solidFill, o2));
				if (i != 0)
					return i;
				int areaComp = -Ints.compare(o1.xSize * o1.ySize, o2.xSize * o2.ySize);
				if (areaComp != 0)
					return areaComp;
				return -Ints.compare(o1.xSize, o2.xSize);
			}

			private double maxPerimeterTouch(final Surface2D surface, final boolean solidFill, Present present) {
				bestRotation(present, surface);
				Point2D bl = findBLInsertionPoint(present, surface, solidFill);
				Point2D br = findBRInsertionPoint(present, surface, solidFill);
				Point2D tr = findTRInsertionPoint(present, surface, solidFill);
				Point2D tl = findTLInsertionPoint(present, surface, solidFill);

				double blpt = perimeterTouch(present, bl, surface, solidFill);
				double brpt = perimeterTouch(present, br, surface, solidFill);
				double trpt = perimeterTouch(present, tr, surface, solidFill);
				double tlpt = perimeterTouch(present, tl, surface, solidFill);

				present.rotateMedMinMax();
				Point2D rbl = findBLInsertionPoint(present, surface, solidFill);
				Point2D rbr = findBRInsertionPoint(present, surface, solidFill);
				Point2D rtr = findTRInsertionPoint(present, surface, solidFill);
				Point2D rtl = findTLInsertionPoint(present, surface, solidFill);

				double rblpt = perimeterTouch(present, rbl, surface, solidFill);
				double rbrpt = perimeterTouch(present, rbr, surface, solidFill);
				double rtrpt = perimeterTouch(present, rtr, surface, solidFill);
				double rtlpt = perimeterTouch(present, rtl, surface, solidFill);

				double maxpt = Ordering.natural().max(brpt, blpt, trpt, tlpt, rbrpt, rblpt, rtrpt, rtlpt);
				return maxpt;
			}
		};
	}

	private Point fitsIn3D(List<Integer> heightsLowToHigh, Map<Integer, Surface2D> freeAtHeight, Present nextPresent,
			Surface2D floor) {
		for (Integer height : heightsLowToHigh) {
			Surface2D surfaceAtHeight = freeAtHeight.get(height);
			nextPresent.rotateMaxMedMin();
			Point fitsIn3DAtLevel = fitsIn3DAtLevel(nextPresent, floor.zLevel, surfaceAtHeight);
			if (fitsIn3DAtLevel != null)
				return fitsIn3DAtLevel;
			nextPresent.rotateMinMaxMed();
			fitsIn3DAtLevel = fitsIn3DAtLevel(nextPresent, floor.zLevel, surfaceAtHeight);
			if (fitsIn3DAtLevel != null)
				return fitsIn3DAtLevel;
			bestRotation(nextPresent, surfaceAtHeight);
			fitsIn3DAtLevel = fitsIn3DAtLevel(nextPresent, floor.zLevel, surfaceAtHeight);
			if (fitsIn3DAtLevel != null)
				return fitsIn3DAtLevel;
		}

		return null;
	}

	private Point fitsIn3DAtLevel(Present present, int floorZLevel, Surface2D surface) {
		if (surface.zLevel + present.zSize <= surface.maxPresentHeight) {
			Point2D fuck = fitsIn2D(present, surface);
			if (fuck != null)
				return new Point(fuck.x, fuck.y, floorZLevel + surface.zLevel);
		}
		return null;
	}

	private Point2D fitsIn2D(Present present, Surface2D surface) {
		Point2D insertionPoint = findBLInsertionPoint(present, surface, true);
		if (insertionPoint != null) {
			return insertionPoint;
		}
		present.rotate();
		return findBLInsertionPoint(present, surface, true);
	}

	private boolean addAll(List<Present> sortedCopy, Surface2D surface, boolean solidFill, Rotator rotator) {
		for (Present p : sortedCopy) {
			// if (p.order == 55484) {
			// exportInsertionPoints(surface);
			// export(surface);
			// }
			if (!add(p, surface, solidFill, rotator)) {
				return false;
			}
		}
		return true;
	}

	private void undoLayerWithPresents(List<Present> layer, Surface2D surface) {
		for (Present present : layer) {
			present.boundaries.clear();
			// present.rotateMedMinMax();
		}
		surface.clear();
		flag = true;
	}

	private List<Present> sortByArea(List<Present> layer) {
		List<Present> sortedCopy = new ArrayList<>(layer);
		Collections.sort(sortedCopy, areaComparator());
		return sortedCopy;
	}

	private Comparator<Present> areaComparator() {
		return new Comparator<Present>() {
			@Override
			public int compare(Present o1, Present o2) {
				int areaComp = -Ints.compare(o1.xSize * o1.ySize, o2.xSize * o2.ySize);
				if (areaComp != 0)
					return areaComp;
				return -Ints.compare(o1.xSize, o2.xSize);
			}
		};
	}

	private Comparator<Present> minusAreaComparator() {
		return new Comparator<Present>() {
			@Override
			public int compare(Present o1, Present o2) {
				int areaComp = -Ints.compare(o1.xSize * o1.ySize * o1.zSize, o2.xSize * o2.ySize * o2.zSize);
				if (areaComp != 0)
					return areaComp;
				return -Ints.compare(o1.xSize, o2.xSize);
			}
		};
	}

	private Comparator<Present> totalAreaComparator() {
		return new Comparator<Present>() {
			@Override
			public int compare(Present o1, Present o2) {
				int areaComp = -Ints.compare(o1.xSize * o1.ySize * 2 + o1.xSize * o1.zSize * 2 + o1.ySize * o1.zSize * 2,
						o2.xSize * o2.ySize * 2 + o2.xSize * o2.zSize * 2 + o2.ySize * o2.zSize * 2);
				if (areaComp != 0)
					return areaComp;
				return -Ints.compare(o1.xSize, o2.xSize);
			}
		};
	}

	private List<Present> sortByXY(List<Present> layer) {
		List<Present> sortedCopy = new ArrayList<>(layer);
		Collections.sort(sortedCopy, xyComparator());
		return sortedCopy;
	}

	private Comparator<Present> xyComparator() {
		return new Comparator<Present>() {
			@Override
			public int compare(Present o1, Present o2) {
				int xComp = -Ints.compare(o1.xSize, o2.xSize);
				if (xComp != 0)
					return xComp;

				return -Ints.compare(o1.ySize, o2.ySize);
			}
		};
	}

	private List<Present> sortByPerimeter(List<Present> layer) {
		List<Present> sortedCopy = new ArrayList<>(layer);
		Collections.sort(sortedCopy, perimeterComparator());
		return sortedCopy;
	}

	private Comparator<Present> perimeterComparator() {
		return new Comparator<Present>() {
			@Override
			public int compare(Present o1, Present o2) {
				int xComp = -Ints.compare(o1.xSize * 2 + o1.ySize * 2, o2.xSize * 2 + o2.ySize * 2);
				if (xComp != 0)
					return xComp;

				return -Ints.compare(o1.ySize, o2.ySize);
			}
		};
	}

	private boolean add(Present present, Surface2D surface, boolean solidFill, Rotator rotator) {
		return select(present, surface, solidFill, rotator);
		// return addBL(present, surface, solidFill);
	}

	private boolean addBL(Present present, Surface2D surface, boolean solidFill) {
		bestRotation(present, surface);
		Point2D blInsertPoint = findBLInsertionPoint(present, surface, solidFill);
		if (blInsertPoint == null) {
			present.rotate();
			blInsertPoint = findBLInsertionPoint(present, surface, solidFill);
		}
		if (blInsertPoint != null) {
			insert(present, blInsertPoint, surface, solidFill);
			addBLInsertionPoint(present, surface, blInsertPoint);
			return true;
		}
		return false;
	}

	private boolean addBR(Present present, Surface2D surface, boolean solidFill) {
		bestRotation(present, surface);
		Point2D brInsertPoint = findBRInsertionPoint(present, surface, solidFill);
		if (brInsertPoint == null) {
			present.rotate();
			brInsertPoint = findBRInsertionPoint(present, surface, solidFill);
		}
		if (brInsertPoint != null) {
			insert(present, brInsertPoint, surface, solidFill);
			addBRInsertionPoint(present, surface, brInsertPoint);
			return true;
		}
		return false;
	}

	private boolean addTR(Present present, Surface2D surface, boolean solidFill) {
		bestRotation(present, surface);
		Point2D trInsertPoint = findTRInsertionPoint(present, surface, solidFill);
		if (trInsertPoint == null) {
			present.rotate();
			trInsertPoint = findTRInsertionPoint(present, surface, solidFill);
		}
		if (trInsertPoint != null) {
			insert(present, trInsertPoint, surface, solidFill);
			addTRInsertionPoint(present, surface, trInsertPoint);
			return true;
		}
		return false;
	}

	private boolean addTL(Present present, Surface2D surface, boolean solidFill) {
		bestRotation(present, surface);
		Point2D tlInsertPoint = findTLInsertionPoint(present, surface, solidFill);
		if (tlInsertPoint == null) {
			present.rotate();
			tlInsertPoint = findTLInsertionPoint(present, surface, solidFill);
		}
		if (tlInsertPoint != null) {
			insert(present, tlInsertPoint, surface, solidFill);
			addTLInsertionPoint(present, surface, tlInsertPoint);
			return true;
		}
		return false;
	}

	boolean flag = true;

	private boolean select(Present present, Surface2D surface, boolean solidFill, Rotator rotator) {
		rotator.rotate(present, surface);
		Point2D bl = findBLInsertionPoint(present, surface, solidFill);
		Point2D br = findBRInsertionPoint(present, surface, solidFill);
		Point2D tr = findTRInsertionPoint(present, surface, solidFill);
		Point2D tl = findTLInsertionPoint(present, surface, solidFill);

		double blpt = perimeterTouch(present, bl, surface, solidFill);
		double brpt = perimeterTouch(present, br, surface, solidFill);
		double trpt = perimeterTouch(present, tr, surface, solidFill);
		double tlpt = perimeterTouch(present, tl, surface, solidFill);

		present.rotate();
		Point2D rbl = findBLInsertionPoint(present, surface, solidFill);
		Point2D rbr = findBRInsertionPoint(present, surface, solidFill);
		Point2D rtr = findTRInsertionPoint(present, surface, solidFill);
		Point2D rtl = findTLInsertionPoint(present, surface, solidFill);

		double rblpt = perimeterTouch(present, rbl, surface, solidFill);
		double rbrpt = perimeterTouch(present, rbr, surface, solidFill);
		double rtrpt = perimeterTouch(present, rtr, surface, solidFill);
		double rtlpt = perimeterTouch(present, rtl, surface, solidFill);

		double maxpt = Ordering.natural().max(brpt, blpt, trpt, tlpt, rbrpt, rblpt, rtrpt, rtlpt);

		if (maxpt == 0)
			return false;

		flag = !flag;
		if (flag) {
			present.rotate();
			if (maxpt == tlpt) {
				insert(present, tl, surface, solidFill);
				addTLInsertionPoint(present, surface, tl);
				return true;
			}
			if (maxpt == brpt) {
				insert(present, br, surface, solidFill);
				addBRInsertionPoint(present, surface, br);
				return true;
			}

			if (maxpt == trpt) {
				insert(present, tr, surface, solidFill);
				addTRInsertionPoint(present, surface, tr);
				return true;
			}

			if (maxpt == blpt) {
				insert(present, bl, surface, solidFill);
				addBLInsertionPoint(present, surface, bl);
				return true;

			}

			present.rotate();
			if (maxpt == rtlpt) {
				insert(present, rtl, surface, solidFill);
				addTLInsertionPoint(present, surface, rtl);
				return true;
			}

			if (maxpt == rbrpt) {
				insert(present, rbr, surface, solidFill);
				addBRInsertionPoint(present, surface, rbr);
				return true;
			}

			if (maxpt == rtrpt) {
				insert(present, rtr, surface, solidFill);
				addTRInsertionPoint(present, surface, rtr);
				return true;
			}

			if (maxpt == rblpt) {
				insert(present, rbl, surface, solidFill);
				addBLInsertionPoint(present, surface, rbl);
				return true;

			}

			throw new IllegalStateException("uaaaaaaaat");
		} else {
			present.rotate();
			if (maxpt == blpt) {
				insert(present, bl, surface, solidFill);
				addBLInsertionPoint(present, surface, bl);
				return true;

			}
			if (maxpt == brpt) {
				insert(present, br, surface, solidFill);
				addBRInsertionPoint(present, surface, br);
				return true;
			}

			if (maxpt == trpt) {
				insert(present, tr, surface, solidFill);
				addTRInsertionPoint(present, surface, tr);
				return true;
			}

			if (maxpt == tlpt) {
				insert(present, tl, surface, solidFill);
				addTLInsertionPoint(present, surface, tl);
				return true;
			}

			present.rotate();
			if (maxpt == rblpt) {
				insert(present, rbl, surface, solidFill);
				addBLInsertionPoint(present, surface, rbl);
				return true;

			}
			if (maxpt == rbrpt) {
				insert(present, rbr, surface, solidFill);
				addBRInsertionPoint(present, surface, rbr);
				return true;
			}

			if (maxpt == rtrpt) {
				insert(present, rtr, surface, solidFill);
				addTRInsertionPoint(present, surface, rtr);
				return true;
			}

			if (maxpt == rtlpt) {
				insert(present, rtl, surface, solidFill);
				addTLInsertionPoint(present, surface, rtl);
				return true;
			}

			throw new IllegalStateException("uaaaaaaaat");

		}

	}

	private void bestRotation(Present present, Surface2D surface) {
		present.rotateMinMedMax();
		if (present.max > surface.maxPresentHeight) {
			present.rotateMinMaxMed();
			if (present.med > surface.maxPresentHeight) {
				present.rotateMedMaxMin();
			}
		}
	}

	private boolean selectWithLazyRotation(Present present, Surface2D surface, boolean solidFill) {
		boolean selectSingleRotation = selectSingleRotation(present, surface, solidFill);
		if (!selectSingleRotation) {
			present.rotate();
			return selectSingleRotation(present, surface, solidFill);
		}
		return selectSingleRotation;
	}

	private boolean selectSingleRotation(Present present, Surface2D surface, boolean solidFill) {
		bestRotation(present, surface);
		Point2D bl = findBLInsertionPoint(present, surface, solidFill);
		Point2D br = findBRInsertionPoint(present, surface, solidFill);
		Point2D tr = findTRInsertionPoint(present, surface, solidFill);
		Point2D tl = findTLInsertionPoint(present, surface, solidFill);

		double blpt = perimeterTouch(present, bl, surface, solidFill);
		double brpt = perimeterTouch(present, br, surface, solidFill);
		double trpt = perimeterTouch(present, tr, surface, solidFill);
		double tlpt = perimeterTouch(present, tl, surface, solidFill);

		double maxpt = Ordering.natural().max(brpt, blpt, trpt, tlpt);

		if (maxpt == 0)
			return false;

		bestRotation(present, surface);
		if (maxpt == blpt) {
			insert(present, bl, surface, solidFill);
			addBLInsertionPoint(present, surface, bl);
			return true;

		}
		if (maxpt == brpt) {
			insert(present, br, surface, solidFill);
			addBRInsertionPoint(present, surface, br);
			return true;
		}

		if (maxpt == trpt) {
			insert(present, tr, surface, solidFill);
			addTRInsertionPoint(present, surface, tr);
			return true;
		}

		if (maxpt == tlpt) {
			insert(present, tl, surface, solidFill);
			addTLInsertionPoint(present, surface, tl);
			return true;
		}

		throw new IllegalStateException("uaaaaaaaat");
	}

	private void addTRInsertionPoint(Present present, Surface2D surface, Point2D insertPoint) {
		int xNewTopRight = insertPoint.x - 1;
		if (xNewTopRight >= 0 && !surface.occupied(xNewTopRight, insertPoint.y + present.ySize - 1)) {
			surface.trInsertionPoints.add(new Point2D(xNewTopRight, insertPoint.y + present.ySize - 1));
		}
		int yNewBottomRIght = insertPoint.y - 1;
		if (yNewBottomRIght >= 0 && !surface.occupied(insertPoint.x + present.xSize - 1, yNewBottomRIght)) {
			surface.trInsertionPoints.add(new Point2D(insertPoint.x + present.xSize - 1, yNewBottomRIght));
		}
		surface.trInsertionPoints.remove(insertPoint.originalInsertionPoint);

	}

	private void addTLInsertionPoint(Present present, Surface2D surface, Point2D insertPoint) {
		int xNewTopRight = insertPoint.x + present.xSize;
		if (xNewTopRight < MAX && !surface.occupied(xNewTopRight, insertPoint.y + present.ySize - 1)) {
			surface.tlInsertionPoints.add(new Point2D(xNewTopRight, insertPoint.y + present.ySize - 1));
		}
		int yNewBottomLeft = insertPoint.y - 1;
		if (yNewBottomLeft >= 0 && !surface.occupied(insertPoint.x, yNewBottomLeft)) {
			surface.tlInsertionPoints.add(new Point2D(insertPoint.x, yNewBottomLeft));
		}
		surface.tlInsertionPoints.remove(insertPoint.originalInsertionPoint);

	}

	private void addBLInsertionPoint(Present present, Surface2D surface, Point2D insertPoint) {

		int xNewBottomRight = insertPoint.x + present.xSize;
		if (xNewBottomRight < MAX && !surface.occupied(xNewBottomRight, insertPoint.y)) {
			surface.blInsertionPoints.add(new Point2D(xNewBottomRight, insertPoint.y));
		}
		int yNewTopLeft = insertPoint.y + present.ySize;
		if (yNewTopLeft < MAX && !surface.occupied(insertPoint.x, yNewTopLeft)) {
			surface.blInsertionPoints.add(new Point2D(insertPoint.x, yNewTopLeft));
		}
		surface.blInsertionPoints.remove(insertPoint.originalInsertionPoint);
	}

	private void addBRInsertionPoint(Present present, Surface2D surface, Point2D insertPoint) {
		int xNewBottomLeft = insertPoint.x - 1;
		if (xNewBottomLeft >= 0 && !surface.occupied(xNewBottomLeft, insertPoint.y)) {
			surface.brInsertionPoints.add(new Point2D(xNewBottomLeft, insertPoint.y));
		}
		int yNewTopRight = insertPoint.y + present.ySize;
		if (yNewTopRight < MAX && !surface.occupied(insertPoint.x + present.xSize - 1, yNewTopRight)) {
			surface.brInsertionPoints.add(new Point2D(insertPoint.x + present.xSize - 1, yNewTopRight));
		}
		surface.brInsertionPoints.remove(insertPoint.originalInsertionPoint);

	}

	private Point2D findBLInsertionPoint(Present present, Surface2D surface, boolean solidFill) {
		double maxP = -1;
		Point2D bestPoint = null;
		for (Point2D point : surface.blInsertionPoints) {
			// try to move down and try to move left (at most only one of those
			// is going to move)
			int newY = point.y;
			while (previousHorizontalLineIsFeasible(surface, point.x, newY, present.xSize)) {
				newY--;
			}
			int newX = point.x;
			while (previousVerticalLineIsFeasible(surface, newX, newY, present.ySize)) {
				newX--;
			}
			Point2D newPoint = new Point2D(newX, newY);
			if (fits(surface, newPoint, present.xSize, present.ySize, solidFill)) {
				double perimeterTouch = perimeterTouch(present, newPoint, surface, solidFill);
				if (perimeterTouch > maxP) {
					bestPoint = newPoint;
					newPoint.originalInsertionPoint = point;
					maxP = perimeterTouch;
				}
				// return newPoint;
			}
		}

		return bestPoint;
	}

	private Point2D findTRInsertionPoint(Present present, Surface2D surface, boolean solidFill) {
		double maxP = 0;
		Point2D bestPoint = null;

		for (Point2D point : surface.trInsertionPoints) {
			// try to move down and try to move left (at most only one of those
			// is going to move)
			int newY = point.y;
			while (nextHorizontalLineIsFeasible(surface, point.x - present.xSize + 1, newY, present.xSize)) {
				newY++;
			}
			int newX = point.x;
			while (nextVerticalLineIsFeasible(surface, newX, newY - present.ySize + 1, present.ySize)) {
				newX++;
			}
			Point2D newPoint = new Point2D(newX - present.xSize + 1, newY - present.ySize + 1);
			if (fits(surface, newPoint, present.xSize, present.ySize, solidFill)) {
				double perimeterTouch = perimeterTouch(present, newPoint, surface, solidFill);
				if (perimeterTouch > maxP) {
					bestPoint = newPoint;
					newPoint.originalInsertionPoint = point;
					maxP = perimeterTouch;
				}
				// return newPoint;
			}
		}

		return bestPoint;
	}

	private Point2D findTLInsertionPoint(Present present, Surface2D surface, boolean solidFill) {
		double maxP = 0;
		Point2D bestPoint = null;

		for (Point2D point : surface.tlInsertionPoints) {
			// try to move down and try to move left (at most only one of those
			// is going to move)
			int newY = point.y;
			while (nextHorizontalLineIsFeasible(surface, point.x, newY, present.xSize)) {
				newY++;
			}
			int newX = point.x;
			while (previousVerticalLineIsFeasible(surface, newX, newY - present.ySize + 1, present.ySize)) {
				newX--;
			}
			Point2D newPoint = new Point2D(newX, newY - present.ySize + 1);
			if (fits(surface, newPoint, present.xSize, present.ySize, solidFill)) {
				double perimeterTouch = perimeterTouch(present, newPoint, surface, solidFill);
				if (perimeterTouch > maxP) {
					bestPoint = newPoint;
					newPoint.originalInsertionPoint = point;
					maxP = perimeterTouch;
				}
				// return newPoint;
			}
		}

		return bestPoint;
	}

	private Point2D findBRInsertionPoint(Present present, Surface2D surface, boolean solidFill) {
		double maxP = 0;
		Point2D bestPoint = null;

		for (Point2D point : surface.brInsertionPoints) {
			// try to move down and try to move left (at most only one of those
			// is going to move)
			int newY = point.y;
			while (previousHorizontalLineIsFeasible(surface, point.x - present.xSize + 1, newY, present.xSize)) {
				newY--;
			}
			int newX = point.x;
			while (nextVerticalLineIsFeasible(surface, newX, newY, present.ySize)) {
				newX++;
			}
			Point2D newPoint = new Point2D(newX - present.xSize + 1, newY);
			if (fits(surface, newPoint, present.xSize, present.ySize, solidFill)) {
				double perimeterTouch = perimeterTouch(present, newPoint, surface, solidFill);
				if (perimeterTouch > maxP) {
					bestPoint = newPoint;
					newPoint.originalInsertionPoint = point;
					maxP = perimeterTouch;
				}
				// return newPoint;
			}
		}

		return bestPoint;
	}

	private boolean add2(Present present, Surface2D surface, boolean solidFill) {
		Point2D insertPoint = findMaxPerimeterTouchInsertionPoint(present, surface, solidFill);
		if (insertPoint != null) {
			insert(present, insertPoint, surface, solidFill);
			return true;
		}
		return false;
	}

	private Point2D findMaxPerimeterTouchInsertionPoint(Present present, Surface2D surface, boolean solidFill) {
		Point2D bestPoint = null;
		Point2D bestPointOriginal = null;
		double bestTouch = 0;
		boolean rotation = false;

		for (Point2D point : surface.blInsertionPoints) {
			// try to move down and try to move left (at most only one of those
			// is going to move)
			int newY = point.y;
			while (previousHorizontalLineIsFeasible(surface, point.x, newY, present.xSize)) {
				newY--;
			}
			int newX = point.x;
			while (previousVerticalLineIsFeasible(surface, newX, newY, present.ySize)) {
				newX--;
			}
			Point2D newPoint = new Point2D(newX, newY);

			present.rotateMedMinMax();
			double touch = perimeterTouch(present, newPoint, surface, solidFill);
			if (touch != -1 && touch > bestTouch) {
				bestTouch = touch;
				bestPoint = newPoint;
				bestPointOriginal = point;
				rotation = true;
			}
			bestRotation(present, surface);
			touch = perimeterTouch(present, newPoint, surface, solidFill);
			if (touch != -1 && touch > bestTouch) {
				bestTouch = touch;
				bestPoint = newPoint;
				bestPointOriginal = point;
				rotation = false;
			}
		}

		if (bestPointOriginal != null)
			surface.blInsertionPoints.remove(bestPointOriginal);

		if (rotation)
			present.rotateMedMinMax();
		else
			bestRotation(present, surface);
		return bestPoint;
	}

	private double perimeterTouch(Present present, Point2D ip, Surface2D surface, boolean solidFill) {
		if (ip == null)
			return 0;
		int maxOccupiedX = ip.x + present.xSize - 1;
		int maxOccupiedY = ip.y + present.ySize - 1;
		if (maxOccupiedX >= MAX || maxOccupiedY >= MAX)
			return -1;

		// Check perimeter!
		int perimeterTouch = 0;
		for (int p = maxOccupiedX; p >= ip.x; p--) {
			if (surface.occupied(p, ip.y) || surface.occupied(p, maxOccupiedY)) {
				return -1;
			}
			if (ip.y == 0 || surface.occupied(p, ip.y - 1)) {
				// if (ip.y > 0)
				// perimeterTouch += surface.top[p][ip.y - 1] - surface.zLevel;
				// else
				// perimeterTouch += present.zSize;
				perimeterTouch++;

			}
			if (maxOccupiedY == MAX - 1 || surface.occupied(p, maxOccupiedY + 1)) {
				// if (maxOccupiedY < MAX - 1)
				// perimeterTouch += surface.top[p][maxOccupiedY + 1] -
				// surface.zLevel;
				// else
				// perimeterTouch += present.zSize;
				perimeterTouch++;

			}
		}
		for (int q = maxOccupiedY; q >= ip.y; q--) {
			if (surface.occupied(ip.x, q) || surface.occupied(maxOccupiedX, q)) {
				return -1;
			}
			if (ip.x == 0 || surface.occupied(ip.x - 1, q)) {
				// if (ip.x > 0)
				// perimeterTouch += surface.top[ip.x - 1][q] - surface.zLevel;
				// else
				// perimeterTouch += present.zSize;
				perimeterTouch++;
			}
			if (maxOccupiedX == MAX - 1 || surface.occupied(maxOccupiedX + 1, q)) {
				// if (maxOccupiedX < MAX - 1)
				// perimeterTouch += surface.top[maxOccupiedX + 1][q] -
				// surface.zLevel;
				// else {
				// perimeterTouch += present.zSize;
				// }
				perimeterTouch++;
			}
		}

		// TODO: no chequear nuevamente el perimetro
		if (solidFill) {
			for (int p = maxOccupiedX; p >= ip.x; p--) {
				for (int q = maxOccupiedY; q >= ip.y; q--) {
					if (surface.occupied(p, q)) {
						return -1;
					}
				}
			}
		}

		return -((double) perimeterTouch - (present.xSize * 2 + present.ySize * 2));
		// return (double) perimeterTouch / (present.xSize * 2 + present.ySize *
		// 2);
	}

	private boolean previousVerticalLineIsFeasible(Surface2D surface, int x, int y, int height) {
		int prevX = x - 1;
		if (prevX < 0 || y + height - 1 >= MAX)
			return false;

		for (int q = y + height - 1; q >= y; q--) {
			if (surface.occupied(prevX, q)) {
				return false;
			}
		}

		return true;
	}

	private boolean nextVerticalLineIsFeasible(Surface2D surface, int x, int y, int height) {
		int nextX = x + 1;
		if (nextX > MAX - 1 || y + height - 1 >= MAX)
			return false;

		for (int q = y + height - 1; q >= y; q--) {
			if (surface.occupied(nextX, q)) {
				return false;
			}
		}

		return true;
	}

	private boolean previousHorizontalLineIsFeasible(Surface2D surface, int x, int y, int width) {
		int nextY = y - 1;
		if (x < 0 || nextY < 0 || x + width - 1 >= MAX)
			return false;

		for (int p = x + width - 1; p >= x; p--) {
			if (surface.occupied(p, nextY)) {
				return false;
			}
		}

		return true;
	}

	private boolean nextHorizontalLineIsFeasible(Surface2D surface, int x, int y, int width) {
		int nextY = y + 1;
		if (nextY > MAX - 1 || x + width - 1 >= MAX || x < 0)
			return false;

		for (int p = x + width - 1; p >= x; p--) {
			if (surface.occupied(p, nextY)) {
				return false;
			}
		}

		return true;
	}

	private boolean fits(Surface2D surface, Point2D point, int xSize, int ySize, boolean solidFill) {
		int maxOccupiedX = point.x + xSize - 1;
		int maxOccupiedY = point.y + ySize - 1;
		if (maxOccupiedX >= MAX || maxOccupiedY >= MAX || point.x < 0 || point.y < 0)
			return false;

		if (solidFill) {
			for (int p = maxOccupiedX; p >= point.x; p--) {
				for (int q = maxOccupiedY; q >= point.y; q--) {
					if (surface.occupied(p, q)) {
						return false;
					}
				}
			}
		} else {
			// Only check perimeter!
			for (int p = maxOccupiedX; p >= point.x; p--) {
				if (surface.occupied(p, maxOccupiedY) || surface.occupied(p, point.y)) {
					return false;
				}
			}
			for (int q = maxOccupiedY; q >= point.y; q--) {
				if (surface.occupied(maxOccupiedX, q) || surface.occupied(point.x, q)) {
					return false;
				}
			}
		}
		return true;
	}

	private void startNewLayer(Surface2D surface) {
		surface.zLevel += surface.maxPresentHeight;
		surface.topBackup = surface.cloneTop(surface.top);
		flag = true;
	}

	private void insert(Present present, Point2D insertionPoint, Surface2D surface, boolean solidFill) {
		this.occupy(surface, insertionPoint.x, insertionPoint.y, present.xSize, present.ySize, solidFill);

		// Might not be present if we moved down or left, so we have to find the
		// one to remove
		// if (!insertionPoints.remove(insertionPoint)) {
		// Iterator<Point2D> i = insertionPoints.iterator();
		// while(i.hasNext()) {
		// Point2D next = i.next();
		// if (insertionPoint.x <= next.x && next.x <= insertionPoint.x +
		// present.xSize - 1) {
		// if (insertionPoint.y <= next.y && next.y <= insertionPoint.y +
		// present.ySize - 1) {
		// i.remove();
		// break;
		// }
		// }
		// }
		// }

		Point oneBasedInsertionPoint = new Point(insertionPoint.x + 1, insertionPoint.y + 1, surface.zLevel + 1);
		setBoundaries(present, oneBasedInsertionPoint);

		if (present.zSize > surface.maxPresentHeight) {
			surface.maxPresentHeight = present.zSize;
		}

		for (int p = oneBasedInsertionPoint.x - 1; p < oneBasedInsertionPoint.x - 1 + present.xSize; p++) {
			for (int q = oneBasedInsertionPoint.y - 1; q < oneBasedInsertionPoint.y - 1 + present.ySize; q++) {
				surface.top[p][q] = present.maxZ();
			}
		}

		// System.out.println(present.order + " [" + present.xSize + ","+
		// present.ySize +","+ present.zSize +"] "+ oneBasedInsertionPoint);
	}

	private void setBoundaries(Present present, Point insertPoint) {
		present.boundaries.add(new Point(insertPoint.x, insertPoint.y, insertPoint.z));
		present.boundaries.add(new Point(insertPoint.x, insertPoint.y + present.ySize - 1, insertPoint.z));
		present.boundaries.add(new Point(insertPoint.x + present.xSize - 1, insertPoint.y, insertPoint.z));
		present.boundaries
				.add(new Point(insertPoint.x + present.xSize - 1, insertPoint.y + present.ySize - 1, insertPoint.z));

		present.boundaries.add(new Point(insertPoint.x, insertPoint.y, insertPoint.z + present.zSize - 1));
		present.boundaries
				.add(new Point(insertPoint.x, insertPoint.y + present.ySize - 1, insertPoint.z + present.zSize - 1));
		present.boundaries
				.add(new Point(insertPoint.x + present.xSize - 1, insertPoint.y, insertPoint.z + present.zSize - 1));
		present.boundaries.add(new Point(insertPoint.x + present.xSize - 1, insertPoint.y + present.ySize - 1, insertPoint.z
				+ present.zSize - 1));
	}

	private void occupy(Surface2D surface, int x, int y, int xSize, int ySize, boolean solidFill) {
		if (solidFill) {
			for (int p = x; p < x + xSize; p++) {
				for (int q = y; q < y + ySize; q++) {
					surface.occupy(p, q);
				}
			}
		} else {
			// Only occupy perimeter!
			for (int p = x; p < x + xSize; p++) {
				surface.occupy(p, y);
				surface.occupy(p, y + ySize - 1);
			}
			for (int q = y; q < y + ySize; q++) {
				surface.occupy(x, q);
				surface.occupy(x + xSize - 1, q);
			}
		}
	}

	/* **************** DEBUG STUF ****************** */

	private void viewXY(List<Present> presents) {
		int min = Integer.MAX_VALUE;
		int max = Integer.MIN_VALUE;
		for (Present p : presents) {
			System.out.println(p.order + " (" + p.xSize + "," + p.ySize + "): " + p.boundaries.get(0).x + " "
					+ p.boundaries.get(0).y);
			if (p.order < min)
				min = p.order;
			if (p.order > max)
				max = p.order;
		}
		System.out.println("min " + min + " max " + max);
	}

	public void exportInsertionPoints(Surface2D surface) {
		System.out.println("exporting insertion points...");
		try {
			CSVWriter w = new CSVWriter(new FileWriter(new File("brunoLayerIP.csv")), ',', CSVWriter.NO_QUOTE_CHARACTER);
			for (Point2D p : surface.blInsertionPoints) {
				String[] line = new String[2];
				line[0] = String.valueOf(p.x);
				line[1] = String.valueOf(p.y);
				w.writeNext(line);
			}
			w.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("done.");
	}

	public void export(Surface2D surface) {
		System.out.println("exporting...");
		try {
			CSVWriter w = new CSVWriter(new FileWriter(new File("brunoLayer.csv")), ',', CSVWriter.NO_QUOTE_CHARACTER);
			for (int xi = 0; xi < MAX; xi++) {
				for (int yi = 0; yi < MAX; yi++) {
					if (surface.occupied(xi, yi)) {
						String[] line = new String[2];
						line[0] = String.valueOf(xi);
						line[1] = String.valueOf(yi);
						w.writeNext(line);
					}
				}
			}
			w.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("done.");
	}

}
