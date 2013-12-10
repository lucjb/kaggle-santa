import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class Present {

	int order;
	int xSize;
	int ySize;
	int zSize;
	List<Point> boundaries = new ArrayList<Point>(8);

	public Present(int order, int xSize, int ySize, int zSize) {
		super();
		this.order = order;
		this.xSize = xSize;
		this.ySize = ySize;
		this.zSize = zSize;
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

}
