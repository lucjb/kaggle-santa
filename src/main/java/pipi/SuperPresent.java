package pipi;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import first.Point;

public class SuperPresent {

	private final int order;
	private final PresentDimension dimension;

	public SuperPresent(int order, PresentDimension dimension) {
		this.order = order;
		this.dimension = dimension;
	}
	
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SIMPLE_STYLE);
	}

	public int getOrder() {
		return this.order;
	}
	
	public PresentDimension getDimension() {
		return this.dimension;
	}

	public static int[] ouputPresent(int order, Point point, Box box, int lastZ) {
		int[] result = new int[8 * 3 + 1];
		result[0] = order;
		int[] start = new int[] { point.x + 1, point.y + 1, lastZ - point.z };
		int[] end = new int[] { start[0] + box.dx - 1, start[1] + box.dy - 1, start[2] - box.dz + 1 };
		int j = 1;
		for (int i = 0; i < 8; i++) {
			for (int d = 0; d < 3; d++) {
				if ((i & (1 << d)) != 0) {
					result[j] = end[d];
				} else {
					result[j] = start[d];
				}
				j++;
			}
		}
		return result;
	}
	
}
