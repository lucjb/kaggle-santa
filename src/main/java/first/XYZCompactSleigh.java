package first;

import java.util.Comparator;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Ints;

public class XYZCompactSleigh {

	HeightBitMap space = new HeightBitMap();
	int maxZ = 0;
	int lowestRoof = 0;

	public void addPresents(List<Present> presents) {
		for (Present present : presents) {
			present.leastSupRotation();
		}
		for (Present present : presents) {
			add(present, true);
			if (present.order % 1000 == 0) {
				System.out.println(present);
			}
		}

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
						System.err.println("Wrong! z=");
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
		maxZ = 0;
	}

	private boolean add(Present present, boolean force) {
		Point insertPoint = placeFor(present);
		if (insertPoint == null) {
			present.rotate();
			insertPoint = placeFor(present);
			if (insertPoint == null) {
				present.maxSupRotation();
				insertPoint = placeFor(present);
				if (insertPoint == null) {
					present.rotate();
					insertPoint = placeFor(present);
				}
			}
		}
		if (insertPoint != null) {
			insert(present, insertPoint);
			return true;
		} else {
			if (force) {
				startLayer();
				add(present, force);
			}
			return false;
		}
	}

	private void startLayer() {
		space.nextZ(maxZ - 1);
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
		if (zp < lowestRoof) {
			lowestRoof = zp;
		}
	}

	private Point placeFor(Present present) {
		int xSize = present.xSize;
		int ySize = present.ySize;
		for (int xi = 0; xi <= 1000 - xSize;) {
			for (int yi = 0; xi <= 1000 - xSize && yi <= 1000 - ySize; yi++) {
				int skip = this.thereIsRoomFor(xi, yi, xSize, ySize);
				if (skip == yi) {
					return new Point(xi + 1, yi + 1, this.space.currentZ + 1);
				} else {
					yi = skip - 1;
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
