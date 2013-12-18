package first;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Ints;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils.Collections;

public class XYZCompactSleigh {

	private static final int floatz = 3;
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
		for (Present present : presents) {
			boolean added = add(present, true);
			if (!added) {
				undoLayer(layer);
				List<Present> sortedLayer = shiffle(layer);
				if (reinsert(sortedLayer)) {
					layer = sortedLayer;
				} else {
					undoLayer(sortedLayer);
					sortedLayer = sortSupX(layer);
					if (reinsert(sortedLayer)) {
						layer = sortedLayer;
					} else {
						undoLayer(sortedLayer);
						if (!reinsert(layer)) {
							System.err.println("Wrong! z");
						}
					}
				}
				if (!add(present, true)) {
					layer.clear();
					startLayer();
					add(present, true);
				}
			}
			if (i % 10000 == 0) {
				System.out.println(i + "z= " + space.currentZ);
			}
			i++;
			layer.add(present);
		}

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
		for (Present p : layer) {
			if (floats(p)) {
				for (Point b : p.boundaries) {
					b.z -= floatz;
				}
				Point insertPoint = p.location();
				for (int xi = insertPoint.x - 1; xi < insertPoint.x - 1 + p.xSize; xi++) {
					for (int yi = insertPoint.y - 1; yi < insertPoint.y - 1 + p.ySize; yi++) {
						top[xi][yi] -= floatz;
					}
				}
			}
		}
		computeMazZFromTop();
	}

	private void computeMazZFromTop() {
		System.out.println(maxZ + " " + prevMax);
		maxZ = -1;
		for (int xi = 0; xi < 1000; xi++) {
			for (int yi = 0; yi < 1000; yi++) {
				if (top[xi][yi] > maxZ) {
					maxZ = top[xi][yi];
				}
			}
		}
		System.out.println("recomputed: " + maxZ);
	}

	private boolean floats(Present present) {
		Point insertPoint = present.location();
		for (int xi = insertPoint.x - 1; xi < insertPoint.x - 1 + present.xSize; xi++) {
			for (int yi = insertPoint.y - 1; yi < insertPoint.y - 1 + present.ySize; yi++) {
				if (insertPoint.z - topBackup[xi][yi] <= floatz) {
					return false;
				}
			}
		}
		return true;
	}

	private boolean reinsert(List<Present> sortedCopy) {
		for (Present p : sortedCopy) {
			if (!add(p, true)) {
				return false;
			}
		}
		return true;
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
			present.boundaries.clear();
		}
		maxZ = prevMax;
		space = backup;
		backup = (HeightBitMap) space.clone();
		currentX = 0;
		currentY = 0;
		nextX = -1;
		top = cloneTop(topBackup);

	}

	private boolean add(Present present, boolean minMedMax) {
		present.rotateMinMedMax();
		Point insertPoint = placeFor(present);
		// if (insertPoint == null) {
		// present.rotate();
		// insertPoint = placeFor(present);
		// if (insertPoint == null) {
		// present.rotateMaxMedMin();
		// insertPoint = placeFor(present);
		// if (insertPoint == null) {
		// present.rotate();
		// insertPoint = placeFor(present);
		// }
		// }
		// }
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

		nextMax(0);
		// space.nextZ(space.currentZ + 10);

		layerCount = 0;
		backup = (HeightBitMap) space.clone();
		prevMax = maxZ;
		currentX = 0;
		currentY = 0;
		nextX = -1;
		topBackup = cloneTop(top);

	}

	private void nextMax(int delta) {
		if (maxZ - delta > 0 && maxZ - delta > space.currentZ) {
			space.nextZ(maxZ - delta);
		} else {
			space.nextZ(space.currentZ + 1);
		}
	}

	private void insert(Present present, Point insertPoint) {
		this.space.put(insertPoint.x - 1, insertPoint.y - 1, present.xSize, present.ySize, present.zSize);

		present.boundaries.add(new Point(insertPoint.x, insertPoint.y, insertPoint.z));
		present.boundaries.add(new Point(insertPoint.x, insertPoint.y + present.ySize - 1, insertPoint.z));
		present.boundaries.add(new Point(insertPoint.x + present.xSize - 1, insertPoint.y, insertPoint.z));
		present.boundaries.add(new Point(insertPoint.x + present.xSize - 1, insertPoint.y + present.ySize - 1, insertPoint.z));

		present.boundaries.add(new Point(insertPoint.x, insertPoint.y, insertPoint.z + present.zSize - 1));
		present.boundaries.add(new Point(insertPoint.x, insertPoint.y + present.ySize - 1, insertPoint.z + present.zSize - 1));
		present.boundaries.add(new Point(insertPoint.x + present.xSize - 1, insertPoint.y, insertPoint.z + present.zSize - 1));
		present.boundaries.add(new Point(insertPoint.x + present.xSize - 1, insertPoint.y + present.ySize - 1, insertPoint.z + present.zSize - 1));

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
		return firstFittingPoint(present);
	}

	private Point naiveLocation(Present present) {
		if (currentX + present.xSize <= 1000) {
			if (currentY + present.ySize <= 1000) {
				return new Point(currentX + 1, currentY + 1, space.currentZ + 1);
			} else {
				return new Point(nextX + 1, 1, space.currentZ + 1);
			}
		} else {
			return null;
			// return firstFittingPoint(present);
		}
	}

	private Point firstFittingPoint(Present present) {
		int xSize = present.xSize;
		int ySize = present.ySize;
		for (int xi = 0; xi <= 1000 - xSize;) {
			for (int yi = 0; xi <= 1000 - xSize && yi <= 1000 - ySize;) {
				int skip = this.thereIsRoomFor(xi, yi, xSize, ySize);
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

	private int thereIsRoomFor(int x, int y, int xSize, int ySize) {
		return this.space.fit(x, y, xSize, ySize);
	}
}
