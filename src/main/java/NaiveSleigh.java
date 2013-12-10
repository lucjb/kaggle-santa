import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Lists;

/**
 * 5,270,836
 * 
 * @author lbernardi
 * 
 */
public class NaiveSleigh {

	int currentZ = 1;
	int currentX = 1;
	int currentY = 1;
	int nextY = 0;
	int nextZ = 0;
	int maxZ = 0;

	List<Present> line = Lists.newArrayList();
	int lineSize = 0;

	public void addPresents(List<Present> presents) {
		for (Present present : presents) {
			for (Iterator iterator = line.iterator(); iterator.hasNext();) {
				Present linePresent = (Present) iterator.next();
				if (add(linePresent, true, false)) {
					iterator.remove();
				}
			}
			add(present, false, false);
			if (line.size() == lineSize)
				emptyLine();
		}

		emptyLine();
		for (Present present : presents) {
			for (int i = 0; i < 8; i++) {
				present.boundaries.get(i).z = maxZ
						- present.boundaries.get(i).z + 1;
			}
		}
	}

	private void emptyLine() {
		for (Iterator iterator = line.iterator(); iterator.hasNext();) {
			Present linePresent = (Present) iterator.next();
			if (add(linePresent, true, true)) {
				iterator.remove();
			}
		}
	}

	private boolean add(Present present, boolean fromLine, boolean force) {
		present.leastSupRotation();
		if (!fitsX(present)) {
			present.rotate();
			if (!fitsX(present)) {
				if (!force) {
					if (!fromLine && line.size() < lineSize) {
						line.add(present);
						return false;
					}
					if (fromLine)
						return false;
				}

				pushCurrentY();
			}
		}
		if (!fitsY(present)) {
			present.rotate();
			if (!fitsY(present) || !fitsX(present)) {
				if (!force) {
					if (!fromLine && line.size() < lineSize) {
						line.add(present);
						return false;
					}
					if (fromLine)
						return false;
				}
				pushCurrentZ();
			}
		}

		present.boundaries.add(new Point(currentX, currentY, currentZ));
		present.boundaries.add(new Point(currentX,
				currentY + present.ySize - 1, currentZ));
		present.boundaries.add(new Point(currentX + present.xSize - 1,
				currentY, currentZ));
		present.boundaries.add(new Point(currentX + present.xSize - 1, currentY
				+ present.ySize - 1, currentZ));

		present.boundaries.add(new Point(currentX, currentY, currentZ
				+ present.zSize - 1));
		present.boundaries.add(new Point(currentX,
				currentY + present.ySize - 1, currentZ + present.zSize - 1));
		present.boundaries.add(new Point(currentX + present.xSize - 1,
				currentY, currentZ + present.zSize - 1));
		present.boundaries.add(new Point(currentX + present.xSize - 1, currentY
				+ present.ySize - 1, currentZ + present.zSize - 1));

		currentX = currentX + present.xSize;
		pushNextY(present.ySize);
		pushNextZ(present.zSize);
		int z = present.maxZ();
		if (z > maxZ) {
			maxZ = z;
		}
		return true;
	}

	private boolean fitsY(Present present) {
		return !(currentY + present.ySize > 1000 + 1);
	}

	private boolean fitsX(Present present) {
		return !(currentX + present.xSize > 1000 + 1);
	}

	private void pushCurrentZ() {
		currentX = 1;
		currentY = 1;
		currentZ = nextZ;
		nextZ = 0;
		nextY = 0;
	}

	private void pushCurrentY() {
		currentX = 1;
		currentY = nextY;
		nextY = 0;
	}

	private void pushNextZ(int zSize) {
		if (currentZ + zSize > nextZ) {
			nextZ = currentZ + zSize;
		}
	}

	private void pushNextY(int ySize) {
		if (currentY + ySize > nextY) {
			nextY = currentY + ySize;
		}
	}
}
