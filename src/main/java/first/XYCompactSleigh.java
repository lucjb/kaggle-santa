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
		List<Present> layer = Lists.newArrayList();
		boolean secondPass = false;
		for (Present present : presents) {
			if (add(present, false)) {
				System.out.println(true);
				if (firstPass(secondPass)) {
					layer.add(present);
				}
			} else {
				System.out.println(false);
				if (firstPass(secondPass)) {
					undo();
					List<Present> sortedCopy = sort(layer);
					if (reinsert(sortedCopy)) {
						System.err.println(add(present, true));
						layer.clear();
						layer.add(present);
					} else {
						if (!reinsert(layer)) {
							System.err.println("wrong!");
						}
						layer.clear();
						add(present, true);
						layer.add(present);
						// secondPass = true;
						// if (!add(present, true)) {
						// layer.add(present);
						// secondPass = false;
						// }
					}
				} else {
					add(present, true);
					secondPass = false;
				}
			}

		}

		for (Present present : presents) {
			for (int i = 0; i < 8; i++) {
				present.boundaries.get(i).z = maxZ
						- present.boundaries.get(i).z + 1;
			}
		}
	}

	private boolean firstPass(boolean secondPass) {
		return !secondPass;
	}

	private boolean reinsert(List<Present> sortedCopy) {
		for (Present p : sortedCopy) {
			p.boundaries.clear();
			if (!add(p, false)) {
				undo();
				return false;
			}
		}
		return true;
	}

	private List<Present> sort(List<Present> layer) {
		List<Present> sortedCopy = Ordering.from(new Comparator<Present>() {

			@Override
			public int compare(Present o1, Present o2) {
				return Ints.compare(o1.order, o2.order);
			}
		}).sortedCopy(layer);
		return sortedCopy;
	}

	private void undo() {
		topSurface.clear();
		nextZ = 0;
		maxZ = 0;
	}

	private boolean add(Present present, boolean force) {
		present.leastSupRotation();
		Point insertPoint = placeFor(present);
		if (insertPoint == null) {
			present.rotate();
			insertPoint = placeFor(present);
		}
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

		present.boundaries.add(new Point(insertPoint.x, insertPoint.y,
				insertPoint.z));
		present.boundaries.add(new Point(insertPoint.x, insertPoint.y
				+ present.ySize - 1, insertPoint.z));
		present.boundaries.add(new Point(insertPoint.x + present.xSize - 1,
				insertPoint.y, insertPoint.z));
		present.boundaries.add(new Point(insertPoint.x + present.xSize - 1,
				insertPoint.y + present.ySize - 1, insertPoint.z));

		present.boundaries.add(new Point(insertPoint.x, insertPoint.y,
				insertPoint.z + present.zSize - 1));
		present.boundaries.add(new Point(insertPoint.x, insertPoint.y
				+ present.ySize - 1, insertPoint.z + present.zSize - 1));
		present.boundaries.add(new Point(insertPoint.x + present.xSize - 1,
				insertPoint.y, insertPoint.z + present.zSize - 1));
		present.boundaries.add(new Point(insertPoint.x + present.xSize - 1,
				insertPoint.y + present.ySize - 1, insertPoint.z
						+ present.zSize - 1));

		int zp = present.maxZ();
		if (zp > maxZ) {
			maxZ = zp;
		}
		if (zp >= nextZ) {
			nextZ = zp + 1;
		}
		// System.out.println(present);
	}

	private Point placeFor(Present present) {
		int xSize = present.xSize;
		int ySize = present.ySize;
		for (int xi = 0; xi <= 1000 - xSize;) {
			for (int yi = 0; xi <= 1000 - xSize && yi <= 1000 - ySize; yi++) {
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
				topSurface.set(p * 1000 + q);
			}
		}
	}

}
