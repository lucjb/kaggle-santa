package first;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Ints;

public class XYZCompactSleigh {

	private static final int floatz = 10;
	int[][] top = new int[1000][1000];
	HeightBitMap space = new HeightBitMap();
	HeightBitMap backup = new HeightBitMap();
	int maxZ = 0;
	int prevMax = 0;
	int lowestRoof = 0;
	int layerCount = 0;

	int currentX = 0;
	int currentY = 0;
	int nextX = -1;

	public void addPresents(List<Present> presents) {
		addPresentsOrdering(presents);

		for (Present present : presents) {
			for (int i = 0; i < 8; i++) {
				present.boundaries.get(i).z = maxZ - present.boundaries.get(i).z + 1;
			}
		}
	}

	int i = 0;
	private int[][] topBackup = cloneTop(top);

	
	private void addPresentsOrdering(List<Present> presents) {
		List<Present> layer = Lists.newArrayList();
		Iterator<Present> iterator = presents.iterator();
		for (; iterator.hasNext();) {
			Present present = (Present) iterator.next();
			if (!add(present)) {
				undoLayer(layer);
				List<Present> sortedLayer = sandongueo(layer, present);
				if (sortedLayer != null) {
					layer = sortedLayer;
				} else {
					undoLayer(layer);
					present.boundaries.clear();
					layer.remove(present);
					if (!reinsert(layer)) {
						System.err.println("Wrong!");
					}
					// present = stuff(present, iterator);
					// pushDown(layer);
					if (present != null) {
						System.out.println("current z: " + (space.currentZ + 1) + ", presents: " + layer.size());
						startLayer();
						layer.clear();
						add(present);
						layer.add(present);
					}
				}
			} else {
				layer.add(present);
			}

			if (i % 10000 == 0) {
				System.out.println(i + "z= " + space.currentZ);
			}
			i++;
		}
		System.out.println(layer.size());
	}

	private List<Present> sandongueo(List<Present> layer, Present present) {
		List<Present> sortedLayer = sortSupY(layer);
		Present blocker = present;
		for (int i = 0; i < 10; i++) {
			sortedLayer.remove(blocker);
			sortedLayer.add(0, blocker);
			blocker = reinsertAndReturn(sortedLayer);
			// System.out.println(blocker);
			if (blocker == null) {
				return sortedLayer;
			}
			undoLayer(sortedLayer);
		}
		return null;
	}

	private Present stuff(Present blocker, Iterator<Present> iterator) {
		int fromZ = space.currentZ + 1;
		Point topLeftInsertionPoint = stuffingInsertionPoint(fromZ, blocker);
		if (topLeftInsertionPoint != null && topLeftInsertionPoint.z + blocker.zSize < maxZ) {
			insert(blocker, topLeftInsertionPoint);
			fromZ = topLeftInsertionPoint.z - 1;
		} else {
			return blocker;

		}

		for (; iterator.hasNext();) {
			Present present = iterator.next();
			topLeftInsertionPoint = stuffingInsertionPoint(fromZ, present);
			if (topLeftInsertionPoint != null && topLeftInsertionPoint.z + present.zSize < maxZ) {
				insert(present, topLeftInsertionPoint);
				fromZ = topLeftInsertionPoint.z - 1;
			} else {
				return present;

			}
		}
		return null;
	}

	private Point stuffingInsertionPoint(int fromZ, Present present) {
		for (int z = fromZ; z < maxZ; z++) {

			present.rotateMaxMedMin();
			Point topLeftInsertionPoint = topLeftInsertionPoint(present, z);

			if (topLeftInsertionPoint == null) {
				present.rotateMedMaxMin();
				topLeftInsertionPoint = topLeftInsertionPoint(present, z);
			}
			if (topLeftInsertionPoint == null) {
				present.rotateMinMaxMed();
				topLeftInsertionPoint = topLeftInsertionPoint(present, z);
			}
			if (topLeftInsertionPoint == null) {
				present.rotateMaxMinMed();
				topLeftInsertionPoint = topLeftInsertionPoint(present, z);
			}
			if (topLeftInsertionPoint == null) {
				present.rotateMinMedMax();
				topLeftInsertionPoint = topLeftInsertionPoint(present, z);
			}
			if (topLeftInsertionPoint == null) {
				present.rotateMedMinMax();
				topLeftInsertionPoint = topLeftInsertionPoint(present, z);
			}
			if (topLeftInsertionPoint != null) {
				return topLeftInsertionPoint;
			}

		}
		return null;
	}

	private boolean encajate(Present present) {
		present.rotateMedMaxMin();
		if (!add(present)) {
			present.rotateMaxMedMin();
			if (!add(present)) {
				present.rotateMinMaxMed();
				if (!add(present)) {
					present.rotateMaxMinMed();
					if (!add(present)) {
						present.rotateMedMinMax();
						if (!add(present)) {
							present.rotateMinMedMax();
							return add(present);
						}
					}
				}
			}
		}
		return true;
	}

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

	private void pushDown(List<Present> layer) {
		List<Present> sortNatural = sortNatural(layer);
		int lastPushDown = Integer.MAX_VALUE;
		for (Present p : sortNatural) {
			Point insertPoint = p.location();
			int maxZBelow = maxZBelow(p);
			int floatingSpace = Math.min(insertPoint.z - maxZBelow - 1, lastPushDown);

			if (floatingSpace == 0) {
				break;
			}
			lastPushDown = floatingSpace;
			space.pushDown(insertPoint.x, insertPoint.y, p.xSize, p.ySize, p.zSize, floatingSpace);
			for (int xi = insertPoint.x - 1; xi < insertPoint.x - 1 + p.xSize; xi++) {
				for (int yi = insertPoint.y - 1; yi < insertPoint.y - 1 + p.ySize; yi++) {
					top[xi][yi] -= floatingSpace;
				}
			}
			for (Point b : p.boundaries) {
				b.z -= floatingSpace;
			}
		}
		computeMaxZFromTop();
	}

	private void computeMaxZFromTop() {
		int pm = maxZ;
		maxZ = -1;
		for (int xi = 0; xi < 1000; xi++) {
			for (int yi = 0; yi < 1000; yi++) {
				if (top[xi][yi] > maxZ) {
					maxZ = top[xi][yi];
				}
			}
		}
		int pushedDown = pm - maxZ;
		if (pushedDown > 0) {
			System.out.println("pushedDown: " + pushedDown);
		}
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

	private boolean reinsert(List<Present> presents) {
		for (Present p : presents) {
			if (!add(p)) {
				return false;
			}
		}
		return true;
	}

	private Present reinsertAndReturn(List<Present> presents) {
		for (Present p : presents) {
			if (!add(p)) {
				return p;
			}
		}
		return null;
	}

	private List<Present> sort(List<Present> layer) {
		List<Present> sortedCopy = Ordering.from(new Comparator<Present>() {

			@Override
			public int compare(Present o1, Present o2) {
				return -Ints.compare(o1.xSize * o1.ySize, o2.xSize * o2.ySize);
			}
		}).sortedCopy(layer);
		return sortedCopy;
	}

	private List<Present> sortNatural(List<Present> layer) {
		List<Present> sortedCopy = Ordering.from(new Comparator<Present>() {

			@Override
			public int compare(Present o1, Present o2) {
				return Ints.compare(o1.order, o2.order);
			}
		}).sortedCopy(layer);
		return sortedCopy;
	}

	private List<Present> sortDiagonal(List<Present> layer) {
		List<Present> sortedCopy = Ordering.from(new Comparator<Present>() {

			@Override
			public int compare(Present o1, Present o2) {
				return -Ints.compare(o1.xSize * o1.xSize + o1.ySize * o1.ySize, o2.xSize * o2.xSize + o2.ySize * o2.ySize);
			}
		}).sortedCopy(layer);
		return sortedCopy;
	}

	private List<Present> sortX(List<Present> layer) {
		List<Present> sortedCopy = Ordering.from(new Comparator<Present>() {

			@Override
			public int compare(Present o1, Present o2) {
				int compare = -Ints.compare(o1.xSize, o2.xSize);
				if (compare == 0) {
					return -Ints.compare(o1.ySize, o2.ySize);
				}
				return compare;
			}
		}).sortedCopy(layer);
		return sortedCopy;
	}

	private List<Present> sortY(List<Present> layer) {
		List<Present> sortedCopy = Ordering.from(new Comparator<Present>() {

			@Override
			public int compare(Present o1, Present o2) {
				int compare = -Ints.compare(o1.ySize, o2.ySize);
				if (compare == 0) {
					return -Ints.compare(o1.xSize, o2.xSize);
				}
				return compare;
			}
		}).sortedCopy(layer);
		return sortedCopy;
	}

	private List<Present> sortZSupX(List<Present> layer) {
		List<Present> sortedCopy = Ordering.from(new Comparator<Present>() {

			@Override
			public int compare(Present o1, Present o2) {
				int compare = -Ints.compare(o1.zSize, o2.zSize);
				if (compare == 0) {
					compare = -Ints.compare(o1.xSize * o1.ySize, o2.xSize * o2.ySize);
					if (compare == 0)
						compare = -Ints.compare(o1.xSize, o2.xSize);
				}

				return compare;
			}
		}).sortedCopy(layer);
		return sortedCopy;
	}

	private List<Present> sortXYRatio(List<Present> layer) {
		List<Present> sortedCopy = Ordering.from(new Comparator<Present>() {

			@Override
			public int compare(Present o1, Present o2) {
				int compare = -Double.compare(o1.xSize / (double) o1.ySize, o2.xSize / (double) o2.ySize);
				if (compare == 0)
					return -Ints.compare(o1.xSize, o2.xSize);
				return compare;
			}
		}).sortedCopy(layer);
		return sortedCopy;
	}

	private List<Present> sortMinMed(List<Present> layer) {
		List<Present> sortedCopy = Ordering.from(new Comparator<Present>() {

			@Override
			public int compare(Present o1, Present o2) {
				int compare = -Ints.compare(o1.min, o2.min);
				if (compare == 0)
					return -Ints.compare(o1.med, o2.med);
				return compare;
			}
		}).sortedCopy(layer);
		return sortedCopy;
	}

	private List<Present> sortSupX(List<Present> layer) {
		List<Present> sortedCopy = Ordering.from(new Comparator<Present>() {

			@Override
			public int compare(Present o1, Present o2) {
				int compare = -Ints.compare(o1.xSize * o1.ySize, o2.xSize * o2.ySize);
				if (compare == 0)
					return -Ints.compare(o1.xSize, o2.xSize);
				return compare;
			}
		}).sortedCopy(layer);
		return sortedCopy;
	}

	private List<Present> sortSupSquarness(List<Present> layer) {
		List<Present> sortedCopy = Ordering.from(new Comparator<Present>() {

			@Override
			public int compare(Present o1, Present o2) {
				int compare = -Ints.compare(o1.xSize * o1.ySize, o2.xSize * o2.ySize);
				if (compare == 0)
					return -Ints.compare(o1.xSize / o1.ySize, o2.xSize / o2.ySize);
				return compare;
			}
		}).sortedCopy(layer);
		return sortedCopy;
	}

	Random rnd = new Random(0);

	private List<Present> shiffle(List<Present> layer) {
		ArrayList<Present> copy = new ArrayList<Present>(layer);
		java.util.Collections.shuffle(copy, rnd);
		return copy;
	}

	private List<Present> sortSupY(List<Present> layer) {
		List<Present> sortedCopy = Ordering.from(new Comparator<Present>() {

			@Override
			public int compare(Present o1, Present o2) {
				int compare = -Ints.compare(o1.xSize * o1.ySize, o2.xSize * o2.ySize);
				if (compare == 0)
					return -Ints.compare(o1.ySize, o2.ySize);
				return compare;
			}
		}).sortedCopy(layer);
		return sortedCopy;
	}

	private void undoLayer(List<Present> layer) {
		for (Present present : layer) {
			present.rotateMinMedMax();
			present.boundaries.clear();
		}
		maxZ = prevMax;
		space = backup;
		backup = (HeightBitMap) space.clone();
		currentX = 0;
		currentY = 0;
		nextX = -1;
		top = cloneTop(topBackup);
		reverse = !reverse;
		b = false;
	}

	private boolean add(Present present) {
		present.rotateMinMedMax();
		Point insertPoint = placeFor(present);
		if (insertPoint == null) {
			present.rotateMedMinMax();
			insertPoint = placeFor(present);
		}

		if (insertPoint != null) {
			insert(present, insertPoint);
			layerCount++;
			return true;
		} else {
			return false;
		}
	}

	private void startLayer() {
		// System.out.println(space.currentZ + " " + layerCount);
		// if (i > 700000) {
		// nextMax(15);
		// } else {
		// nextMax(0);
		// }

		// space.export();
		nextMax(0);
		// space.nextZ(space.currentZ + 10);

		reverse = !reverse;
		layerCount = 0;
		backup = (HeightBitMap) space.clone();
		prevMax = maxZ;
		currentX = 0;
		currentY = 0;
		nextX = -1;
		topBackup = cloneTop(top);
		b = false;
	}

	private void nextMax(int delta) {
		if (maxZ - delta > 0 && maxZ - delta > space.currentZ) {
			space.nextZ(maxZ - delta);
		} else {
			space.nextZ(space.currentZ + 1);
		}
	}

	boolean b = false;

	private void insert(Present present, Point insertPoint) {
		this.space.put(insertPoint.x - 1, insertPoint.y - 1, insertPoint.z - 1, present.xSize, present.ySize, present.zSize);

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

		int zp = present.maxZ();
		if (zp > maxZ) {
			maxZ = zp;
		}

		if (insertPoint.x - 1 == currentX && insertPoint.y - 1 == currentY) {
			currentY += present.ySize;
			if (currentX + present.xSize > nextX) {
				nextX = currentX + present.xSize;
			}
		} else {
			currentY = present.ySize;
			currentX = nextX;
			nextX = currentX + present.xSize;
		}
		for (int p = insertPoint.x - 1; p < insertPoint.x - 1 + present.xSize; p++) {
			for (int q = insertPoint.y - 1; q < insertPoint.y - 1 + present.ySize; q++) {
				top[p][q] = present.maxZ();
			}
		}

	}

	private Point placeFor(Present present) {
		// return alternate(present);
		return maxAdjPerPoint(present);
		// return firstFittingPoint(present);
	}

	private Point naiveLocation(Present present) {
		if (currentX + present.xSize <= 1000) {
			if (currentY + present.ySize <= 1000) {
				return new Point(currentX + 1, currentY + 1, space.currentZ + 1);
			} else {
				if (nextX + present.xSize <= 1000) {
					return new Point(nextX + 1, 1, space.currentZ + 1);
				}
			}
		}
		return firstFittingPoint(present);
	}

	boolean reverse = false;

	private Point alternate(Present present) {
		if (b) {
			return topLeftInsertionPoint(present);
		} else {
			return bottomRightInsertionPoint(present);
		}

	}

	private Point firstFittingPoint(Present present) {
		present.rotateMinMedMax();
		Point topLeftInsertionPoint = topLeftInsertionPoint(present);
		Point bottomUpInsertionPoint = null;
		if (topLeftInsertionPoint != null) {
			bottomUpInsertionPoint = bottomRightInsertionPoint(present);
		}
		present.rotateMedMinMax();

		Point rTopLeftInsertionPoint = topLeftInsertionPoint(present);
		Point rBottomUpInsertionPoint = null;
		if (rTopLeftInsertionPoint != null) {
			rBottomUpInsertionPoint = bottomRightInsertionPoint(present);
		}

		double tlAdjacentPerimeter = space.adjacentPerimeter(present, topLeftInsertionPoint);
		double buAdjacentPerimeter = space.adjacentPerimeter(present, bottomUpInsertionPoint);
		double rtlAdjacentPerimeter = space.adjacentPerimeter(present, rTopLeftInsertionPoint);
		double rbuAdjacentPerimeter = space.adjacentPerimeter(present, rBottomUpInsertionPoint);
		Double max = Ordering.natural().max(tlAdjacentPerimeter, buAdjacentPerimeter, rtlAdjacentPerimeter,
				rbuAdjacentPerimeter);

		if (max == tlAdjacentPerimeter) {
			present.rotateMinMedMax();
			return topLeftInsertionPoint;
		}
		if (max == buAdjacentPerimeter) {
			present.rotateMinMedMax();
			return bottomUpInsertionPoint;
		}
		if (max == rtlAdjacentPerimeter)
			return rTopLeftInsertionPoint;
		if (max == rbuAdjacentPerimeter)
			return rBottomUpInsertionPoint;
		return null;
	}

	private Point maxAdjPerPoint(Present present) {
		Point topLeftInsertionPoint = topLeftInsertionPoint(present);
		if (topLeftInsertionPoint == null)
			return null;
		Point topRightInsertionPoint = topRightInsertionPoint(present);
		Point bottomLeftInsertionPoint = bottomLeftInsertionPoint(present);
		Point bottomRightInsertionPoint = bottomRightInsertionPoint(present);

		double tlAdjacentPerimeter = space.adjacentPerimeter(present, topLeftInsertionPoint);
		double trAdjacentPerimeter = space.adjacentPerimeter(present, topRightInsertionPoint);
		double blAdjacentPerimeter = space.adjacentPerimeter(present, bottomLeftInsertionPoint);
		double brAdjacentPerimeter = space.adjacentPerimeter(present, bottomRightInsertionPoint);

		Double max = Ordering.natural().max(tlAdjacentPerimeter, trAdjacentPerimeter, blAdjacentPerimeter,
				brAdjacentPerimeter);

		if (max == tlAdjacentPerimeter)
			return topLeftInsertionPoint;
		if (max == trAdjacentPerimeter)
			return topRightInsertionPoint;
		if (max == blAdjacentPerimeter)
			return bottomLeftInsertionPoint;
		return bottomRightInsertionPoint;

	}

	private Point bottomLeftInsertionPoint(Present present) {
		for (int xi = 1000 - present.xSize; xi >= 0;) {
			for (int yi = 0; yi <= 1000 - present.ySize;) {
				int skip = space.fit(xi, yi, present.xSize, present.ySize);
				if (skip == yi) {
					return new Point(xi + 1, yi + 1, this.space.currentZ + 1);
				} else {
					yi = skip;
				}
			}
			xi -= 1;
		}
		return null;
	}

	private Point bottomRightInsertionPoint(Present present) {
		for (int xi = 1000 - present.xSize; xi >= 0;) {
			for (int yi = 1000 - present.ySize; yi >= 0;) {
				int skip = space.reverseFit(xi, yi, present.xSize, present.ySize);
				if (skip == yi) {
					return new Point(xi + 1, yi + 1, this.space.currentZ + 1);
				} else {
					yi = skip;
				}
			}
			xi -= 1;
		}
		return null;
	}

	private Point topRightInsertionPoint(Present present) {
		for (int xi = 0; xi <= 1000 - present.xSize;) {
			for (int yi = 1000 - present.ySize; yi >= 0;) {
				int skip = space.reverseFit(xi, yi, present.xSize, present.ySize);
				if (skip == yi) {
					return new Point(xi + 1, yi + 1, this.space.currentZ + 1);
				} else {
					yi = skip;
				}
			}
			xi += 1;
		}
		return null;
	}

	private Point topLeftInsertionPoint(Present present) {
		for (int xi = 0; xi <= 1000 - present.xSize;) {
			for (int yi = 0; yi <= 1000 - present.ySize;) {
				int skip = space.fit(xi, yi, present.xSize, present.ySize);
				if (skip == yi) {
					return new Point(xi + 1, yi + 1, this.space.currentZ + 1);
				} else {
					yi = skip;
				}
			}
			xi += 1;
		}
		return null;
	}

	private Point topLeftInsertionPoint(Present present, int z) {
		for (int xi = 0; xi <= 1000 - present.xSize;) {
			for (int yi = 0; yi <= 1000 - present.ySize;) {
				int skip = space.fit(xi, yi, present.xSize, present.ySize, z);
				if (skip == yi) {
					return new Point(xi + 1, yi + 1, z + 1);
				} else {
					yi = skip;
				}
			}
			xi += 1;
		}
		return null;
	}

	private Point firstSpiralFittingPoint(Present present) {
		int xSize = present.xSize;
		int ySize = present.ySize;

		for (int i = 0; i <= 500 - xSize; i++) {

			int xi = 0 + i;
			for (int yi = 0; yi <= 1000 - ySize;) {
				int skip = this.thereIsRoomFor(xi, yi, present);
				if (skip == yi) {
					return new Point(xi + 1, yi + 1, this.space.currentZ + 1);
				} else {
					yi = skip;
				}
			}

			xi = 1000 - xSize - i;
			for (int yi = 0; yi <= 1000 - ySize;) {
				int skip = this.thereIsRoomFor(xi, yi, present);
				if (skip == yi) {
					return new Point(xi + 1, yi + 1, this.space.currentZ + 1);
				} else {
					yi = skip;
				}
			}

		}
		return null;
	}

	private int thereIsRoomFor(int x, int y, Present present) {
		return space.fit(x, y, present.xSize, present.ySize);
	}

	private int checkBothOrientations(int x, int y, Present present) {
		int fit = this.space.fit(x, y, present.xSize, present.ySize);
		if (fit != y) {
			int sfit = this.space.fit(x, y, present.ySize, present.xSize);
			if (sfit != y) {
				return fit;
			} else {
				int aux = present.xSize;
				present.xSize = present.ySize;
				present.ySize = aux;
				present.computeMinMedMax();
				return y;
			}
		}
		return fit;
	}
}
