package pipi;

import first.Point;

public class HeightmapSuperSleigh {
	public Point putPesent(Box box) {
		Point point = putPoint(box);
		fillZ(point, box);

		return point;
	}

	private Point putPoint(Box box) {
		Point point = new Point(0, 0, Integer.MAX_VALUE);
		for (int x = 0; x + box.dx <= 1000; x++) {
			for (int y = 0; y + box.dy <= 1000; y++) {
				Point max = getMaximumZ(x, y, box);
				if (max.z < point.z) {
					point.x = x;
					point.y = y;
					point.z = max.z;
				}
				y = max.y;
			}
		}
		return point;
	}

	private Point getMaximumZ(int sx, int sy, Box box) {
		Point point = new Point(sx, sy, 0);
		for (int x = sx; x < sx + box.dx; x++) {
			for (int y = sy; y < sy + box.dy; y++) {
				int height = this.heightMap[x][y];
				if (height > point.z) {
					point.x = x;
					point.y = y;
					point.z = height;
				}
			}
		}
		return point;
	}

	private void fillZ(Point point, Box box) {
		int z = point.z + box.dz;
		for (int x = point.x; x < point.x + box.dx; x++) {
			for (int y = point.y; y < point.y + box.dy; y++) {
				this.heightMap[x][y] = z;
			}
		}
	}

	private int[][] heightMap = new int[1000][1000];
}
