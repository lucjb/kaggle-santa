package first;
import java.util.List;

public class ZCompressedSleigh {

	int[][] topSurface = new int[1000][1000];
	int maxZ;

	public void addPresents(List<Present> presents) {
		for (Present present : presents) {
			Point lowestBaseZ = new Point(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
			int xPos = 0;
			int yPos = 0;
			for (int x = 0; x <= 1000 - present.xSize; x++) {
				for (int y = 0; y <= 1000 - present.ySize;) {
					Point baseZ = baseZ(x, y, present);
					if (baseZ.z < lowestBaseZ.z) {
						lowestBaseZ = baseZ;
						xPos = x;
						yPos = y;
					}
					// System.out.println(x + " " + y + " " + x + ">>"
					// + lowestBaseZ);
					y = baseZ.y + 1;
				}
			}
			for (int i = xPos; i < xPos + present.xSize; i++) {
				for (int j = yPos; j < yPos + present.ySize; j++) {
					topSurface[i][j] += present.zSize;
				}
			}

			int z = lowestBaseZ.z;

			present.boundaries.add(new Point(xPos + 1, yPos + 1, z + 1));
			present.boundaries.add(new Point(xPos + 1, yPos + present.ySize, z + 1));
			present.boundaries.add(new Point(xPos + present.xSize, yPos + 1, z + 1));
			present.boundaries.add(new Point(xPos + present.xSize, yPos + present.ySize, z + 1));

			present.boundaries.add(new Point(xPos + 1, yPos + 1, z + present.zSize));
			present.boundaries.add(new Point(xPos + 1, yPos + present.ySize, z + present.zSize));
			present.boundaries.add(new Point(xPos + present.xSize, yPos + 1, z + present.zSize));
			present.boundaries.add(new Point(xPos + present.xSize, yPos + present.ySize, z + present.zSize));

			z = present.maxZ();
			if (z > maxZ) {
				maxZ = z;
			}

		}

		for (Present present : presents) {
			for (int i = 0; i < 8; i++) {
				present.boundaries.get(i).z = maxZ - present.boundaries.get(i).z + 1;
			}
			// if (present.order % 1000 == 0)
			System.out.println(present);
		}

	}

	private Point baseZ(int x, int y, Present present) {
		Point highestPoint = new Point(-1, -1, -1);
		for (int i = x; i < x + present.xSize; i++) {
			for (int j = y; j < y + present.ySize; j++) {
				int z = topSurface[i][j];
				if (z >= highestPoint.z) {
					highestPoint.x = i;
					highestPoint.y = j;
					highestPoint.z = z;
				}
			}
		}
		return highestPoint;
	}

}
