package first;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;

import au.com.bytecode.opencsv.CSVWriter;

import com.google.common.collect.Ordering;
import com.google.common.primitives.Ints;

public class BrunoMain {

	public static void main(String[] args) throws IOException {
		// List<Present> presents = new PresentsParser().parse("f27k.csv");
		List<Present> presents = new PresentsParser().parse("test.csv");
		// List<Present> presents = new PresentsParser().parse("presents.csv");
		FastXYCompactSleigh sleigh = new FastXYCompactSleigh();
		// XYZCompactSleigh sleigh = new XYZCompactSleigh();

		long start = System.currentTimeMillis();
		sleigh.addPresents(presents);
		long end = System.currentTimeMillis();
		System.out.println("Time: " + (end - start));

		generateCSV(presents);
		printEvaluation(presents);
	}

	private static void printEvaluation(List<Present> presents) {
		int maxZ = 0;
		int worstZ = 0;
		for (Present present : presents) {
			int z = present.maxZ();
			worstZ += Ordering.natural().max(present.xSize, present.ySize, present.zSize);
			if (z > maxZ) {
				maxZ = z;
			}
		}

		List<Present> sorted = Ordering.from(new Comparator<Present>() {
			public int compare(Present o1, Present o2) {
				int z1 = o1.maxZ();
				int z2 = o2.maxZ();
				int i = -Ints.compare(z1, z2);
				if (i == 0) {
					i = Ints.compare(o1.order, o2.order);
				}
				return i;
			}
		}).sortedCopy(presents);

		int orderAbsoluteErrorSum = 0;
		int i = 1;
		for (Present present : sorted) {
			int assignedOrder = i;
			int orderAbsoluteError = Math.abs(present.order - assignedOrder);
			// System.out.println("expected order: " + present.order +
			// " assigned order: " + assignedOrder + " z: " + z + ", error: " +
			// orderAbsoluteError);
			orderAbsoluteErrorSum += orderAbsoluteError;
			i++;
		}

		System.out.println("Max Z: " + maxZ + " Worst Z: " + worstZ);
		System.out.println("Sigma: " + orderAbsoluteErrorSum);
		System.out.println("Score: " + (2 * maxZ + orderAbsoluteErrorSum));
	}

	public static void generateCSV(List<Present> presents) throws IOException {
		CSVWriter writer = new CSVWriter(new FileWriter("areavscolumeSIpushdown.csv"), ',', CSVWriter.NO_QUOTE_CHARACTER);
		String[] headers = new String[] { "PresentId", "x1", "y1", "z1", "x2", "y2", "z2", "x3", "y3", "z3", "x4", "y4",
				"z4", "x5", "y5", "z5", "x6", "y6", "z6", "x7", "y7", "z7", "x8", "y8", "z8" };
		writer.writeNext(headers);
		for (Present present : presents) {

			if (present.boundaries.isEmpty())
				break;

			String[] line = new String[25];

			line[0] = String.valueOf(present.order);

			line[1] = String.valueOf(present.boundaries.get(0).x);
			line[2] = String.valueOf(present.boundaries.get(0).y);
			line[3] = String.valueOf(present.boundaries.get(0).z);

			line[4] = String.valueOf(present.boundaries.get(1).x);
			line[5] = String.valueOf(present.boundaries.get(1).y);
			line[6] = String.valueOf(present.boundaries.get(1).z);

			line[7] = String.valueOf(present.boundaries.get(2).x);
			line[8] = String.valueOf(present.boundaries.get(2).y);
			line[9] = String.valueOf(present.boundaries.get(2).z);

			line[10] = String.valueOf(present.boundaries.get(3).x);
			line[11] = String.valueOf(present.boundaries.get(3).y);
			line[12] = String.valueOf(present.boundaries.get(3).z);

			line[13] = String.valueOf(present.boundaries.get(4).x);
			line[14] = String.valueOf(present.boundaries.get(4).y);
			line[15] = String.valueOf(present.boundaries.get(4).z);

			line[16] = String.valueOf(present.boundaries.get(5).x);
			line[17] = String.valueOf(present.boundaries.get(5).y);
			line[18] = String.valueOf(present.boundaries.get(5).z);

			line[19] = String.valueOf(present.boundaries.get(6).x);
			line[20] = String.valueOf(present.boundaries.get(6).y);
			line[21] = String.valueOf(present.boundaries.get(6).z);

			line[22] = String.valueOf(present.boundaries.get(7).x);
			line[23] = String.valueOf(present.boundaries.get(7).y);
			line[24] = String.valueOf(present.boundaries.get(7).z);

			writer.writeNext(line);
		}
		writer.close();
	}

}
