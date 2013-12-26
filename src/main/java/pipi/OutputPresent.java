package pipi;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.primitives.Ints;

import first.Point;

public class OutputPresent {

	private final int order;
	private final Point point;
	private final Box box;

	public OutputPresent(int order, Point point, Box box) {
		this.order = order;
		this.point = point;
		this.box = box;
	}

	public int getOrder() {
		return this.order;
	}

	public Point getPoint() {
		return this.point;
	}

	public Box getBox() {
		return this.box;
	}

	public String outputString(int lastZ) {
		int[] ouputPresent = OutputPresent.ouputPresent(
				this.getOrder(), this.getPoint(),
				this.getBox(), lastZ);
		return Joiner.on(',').join(
				Ints.asList(ouputPresent));
	}

	static void outputPresents(List<OutputPresent> outputPresents, int lastZ, String filename) throws IOException {
		try (BufferedWriter newBufferedWriter = Files.newBufferedWriter(
				Paths.get(".", filename), Charsets.UTF_8)) {
			newBufferedWriter
					.write("PresentId,x1,y1,z1,x2,y2,z2,x3,y3,z3,x4,y4,z4,x5,y5,z5,x6,y6,z6,x7,y7,z7,x8,y8,z8");
			newBufferedWriter.newLine();
			for (OutputPresent outputPresent : outputPresents) {
				newBufferedWriter.write(outputPresent.outputString(lastZ));
				newBufferedWriter.newLine();
			}
		}
	}

	public static int[] ouputPresent(int order, Point point, Box box, int lastZ) {
		int[] result = new int[8 * 3 + 1];
		result[0] = order;
		int[] start = new int[] { point.x + 1, point.y + 1, lastZ - point.z - 1 };
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
