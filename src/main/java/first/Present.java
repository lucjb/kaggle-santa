package first;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.google.common.collect.Lists;

public class Present {

	int order;
	int xSize;
	int ySize;
	int zSize;
	List<Point> boundaries = new ArrayList<Point>(8);
	int min;
	int med;
	int max;

	public Present(int order, int xSize, int ySize, int zSize) {
		super();
		this.order = order;
		this.xSize = xSize;
		this.ySize = ySize;
		this.zSize = zSize;
		ArrayList<Integer> sizes = Lists.newArrayList(xSize, ySize, zSize);
		Collections.sort(sizes);
		min = sizes.get(0);
		med = sizes.get(1);
		max = sizes.get(2);
	}

	public int maxZ() {
		int maxZ = -1;
		for (Point point : this.boundaries) {
			if (point.z > maxZ)
				maxZ = point.z;
		}
		return maxZ;
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	public int volume() {
		return xSize * ySize * zSize;
	}

	public void rotateMinMedMax() {
		xSize = min;
		ySize = med;
		zSize = max;
	}

	public void rotateMedMinMax() {
		xSize = med;
		ySize = min;
		zSize = max;
	}

	public void rotateMedMaxMin() {
		xSize = med;
		ySize = max;
		zSize = min;
	}

	public void rotateMinMaxMed() {
		xSize = min;
		ySize = max;
		zSize = med;
	}

	public void rotateMaxMedMin() {
		xSize = max;
		ySize = med;
		zSize = min;
	}

	public void flatestRotation() {
		if (zSize < xSize && zSize < ySize)
			return;

		if (xSize < zSize && xSize < ySize) {
			swapXZ();
			return;
		}

		if (ySize < zSize && ySize < xSize) {
			swapZY();
			return;
		}
	}

	public void leastSupRotation() {
		thinestRotation();
		if (ySize < zSize)
			return;
		swapZY();
	}

	public void maxSupRotation() {
		flatestRotation();
		if (ySize < zSize)
			return;
		swapZY();
	}

	public void thinestRotation() {
		if (xSize < zSize && xSize < ySize)
			return;

		if (zSize < xSize && zSize < ySize) {
			swapXZ();
			return;
		}

		if (ySize < xSize && ySize < zSize) {
			swapXY();
			return;
		}
	}

	public void tallestRotation() {
		if (zSize > xSize && zSize > ySize)
			return;

		if (xSize > zSize && xSize > ySize) {
			swapXZ();
			return;
		}

		if (ySize > zSize && ySize > xSize) {
			swapZY();
			return;
		}
	}

	public void rotate() {
		swapXY();
	}

	private void swapXZ() {
		int aux = zSize;
		zSize = xSize;
		xSize = aux;
		return;
	}

	private void swapZY() {
		int aux = zSize;
		zSize = ySize;
		ySize = aux;
	}

	private void swapXY() {
		int aux = xSize;
		xSize = ySize;
		ySize = aux;
	}

	public int minZ() {
		int minZ = Integer.MAX_VALUE;
		for (Point point : this.boundaries) {
			if (point.z < minZ)
				minZ = point.z;
		}
		return minZ;
	}

	public int minX() {
		int minX = Integer.MAX_VALUE;
		for (Point point : this.boundaries) {
			if (point.x < minX)
				minX = point.x;
		}
		return minX;
	}

	public int minY() {
		int minY = Integer.MAX_VALUE;
		for (Point point : this.boundaries) {
			if (point.y < minY)
				minY = point.y;
		}
		return minY;
	}

	public Point location() {
		return new Point(minX(), minY(), minZ());
	}

}
