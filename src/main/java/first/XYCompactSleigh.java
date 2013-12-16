package first;

import java.util.BitSet;
import java.util.Comparator;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Ints;

public class XYCompactSleigh {

	int currentZ = 1;
	int nextZ = 0;
	int maxZ = 0;
	BitSet topSurface = new BitSet(1000 * 1000);

	private int thereIsRoomFor(int x, int y, int xSize, int ySize) {
		for (int p = x; p < x + xSize; p++) {
			for (int q = y; q < y + ySize; q++) {
				int bitIndex = p * 1000 + q;
				if (topSurface.get(bitIndex)) {
					while (q < 1000 + ySize && topSurface.get(p * 1000 + q)) {
						q++;
					}
					return q;
				}
			}
		}
		return y;
	}

	public void addPresents(List<Present> presents) {
		for (Present present : presents) {
			present.leastSupRotation();
		}
		 addPresentsOrdering(presents);
		// for (Present present : presents) {
		// add(present, true);
		// if (present.order % 1000 == 0)
		// System.out.println(present);
		// }
		int u = 0;
		u = 0;
		for (Present present : presents) {
			for (int i = 0; i < 8; i++) {
				present.boundaries.get(i).z = maxZ - present.boundaries.get(i).z + 1;
			}
		}
	}

	private void addPresentsOrdering(List<Present> presents) {
		List<Present> layer = Lists.newArrayList();

		for (Present present : presents) {
			boolean added = add(present, false);
			if (!added) {
				undoLayer(layer);
				List<Present> sortedLayer = sort(layer);
				boolean reinserted = reinsert(sortedLayer);
				if (!reinserted) {
					undoLayer(layer);
					if (!reinsert(layer)) {
						System.err.println("Wrong! z=" + currentZ);
					}
				} else {
					layer = sortedLayer;
				}
				added = add(present, true);
			}

			boolean sameLayer = added;
			if (!sameLayer) {
				layer.clear();
			}
			layer.add(present);
			if (present.order % 1000 == 0) {
				System.out.println(present);
			}
		}
	}

	private boolean reinsert(List<Present> sortedCopy) {
		for (Present p : sortedCopy) {
			if (!add(p, false)) {
				return false;
			}
		}
		return true;
	}

	private List<Present> sort(List<Present> layer) {
		List<Present> sortedCopy = Ordering.from(new Comparator<Present>() {

			@Override
			public int compare(Present o1, Present o2) {
				return Ints.compare(o1.xSize * o1.ySize, o2.xSize * o2.ySize);
			}
		}).sortedCopy(layer);
		return sortedCopy;
	}

	private void undoLayer(List<Present> layer) {
		for (Present present : layer) {
			present.boundaries.clear();
		}
		topSurface.clear();
		nextZ = 0;
		maxZ = 0;
	}

	private boolean add(Present present, boolean force) {
		Point insertPoint = placeFor(present);
		// if (insertPoint == null) {
		// present.rotate();
		// insertPoint = placeFor(present);
		// }
		if (insertPoint != null) {
			insert(present, insertPoint);
			return true;
		} else {
			if (force) {
				startLayer();
				insert(present, new Point(1, 1, currentZ));
			}
			return false;
		}
	}

	private void startLayer() {
		currentZ = nextZ;
		nextZ = 0;
		topSurface.clear();
	}

	private void insert(Present present, Point insertPoint) {
		this.occupy(insertPoint.x, insertPoint.y, present.xSize, present.ySize);

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
		if (zp >= nextZ) {
			nextZ = zp + 1;
		}
	}

	private Point placeFor(Present present) {
		int xSize = present.xSize;
		int ySize = present.ySize;
		for (int xi = 0; xi <= 1000 - xSize;) {
			for (int yi = 0; yi <= 1000 - ySize; yi++) {
				int skip = this.thereIsRoomFor(xi, yi, xSize, ySize);
				if (skip == yi) {
					return new Point(xi + 1, yi + 1, this.currentZ);
				} else {
					yi = skip - 1;
				}
			}
			xi += 1;
		}
		return null;
	}

	private void occupy(int x, int y, int xSize, int ySize) {
		for (int p = x; p < x + xSize; p++) {
			for (int q = y; q < y + ySize; q++) {
				topSurface.set((p - 1) * 1000 + (q - 1));
			}
		}
	}

}
