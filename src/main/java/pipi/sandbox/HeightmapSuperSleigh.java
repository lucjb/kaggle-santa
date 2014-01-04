package pipi.sandbox;

import pipi.Box3d;
import first.Point;

public class HeightmapSuperSleigh {
	public Point putPesent(Box3d box3d) {
		Point point = putPoint(box3d);
		fillZ(point, box3d);

		return point;
	}

	private Point putPoint(Box3d box3d) {
		Point point = new Point(0, 0, Integer.MAX_VALUE);
		for (int x = 0; x + box3d.dx <= 1000; x++) {
			for (int y = 0; y + box3d.dy <= 1000; y++) {
				Point max = getMaximumZ(x, y, box3d);
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

	private Point getMaximumZ(int sx, int sy, Box3d box3d) {
		Point point = new Point(sx, sy, 0);
		for (int x = sx; x < sx + box3d.dx; x++) {
			for (int y = sy; y < sy + box3d.dy; y++) {
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

	private void fillZ(Point point, Box3d box3d) {
		int z = point.z + box3d.dz;
		for (int x = point.x; x < point.x + box3d.dx; x++) {
			for (int y = point.y; y < point.y + box3d.dy; y++) {
				this.heightMap[x][y] = z;
			}
		}
	}

	private int[][] heightMap = new int[1000][1000];
}
