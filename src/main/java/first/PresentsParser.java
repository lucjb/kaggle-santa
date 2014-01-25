package first;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import au.com.bytecode.opencsv.CSVReader;

import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;

public class PresentsParser {

	public static void main(String[] args) throws IOException {
		List<Present> parse = new PresentsParser().parse("presents.csv");
		for (Present present : parse) {
			System.out.println(present);
		}
	}

	public List<Present> parse(String inputFileName) throws IOException {
		CSVReader reader = new CSVReader(new FileReader(new File(inputFileName)), ',', '|', 1);
		List<String[]> readAll = reader.readAll();
		List<Present> presents = new ArrayList<Present>(1000000);
		for (String[] line : readAll) {
			String orderString = line[0];
			String xString = line[1];
			String yString = line[2];
			String zString = line[3];
			int order = Integer.parseInt(orderString);
			int x = Integer.parseInt(xString);
			int y = Integer.parseInt(yString);
			int z = Integer.parseInt(zString);
			Present present = new Present(order, x, y, z);
			presents.add(present);
		}
		return presents;
	}

	public List<Present> parseOutput(String inputFileName) throws IOException {
		CSVReader reader = new CSVReader(new FileReader(new File(inputFileName)), ',', '|', 1);
		List<Present> presents = new ArrayList<Present>(1000000);
		boolean first = true;
		int maxOccupiedZ = -1;
		String[] line = null;
		while ((line = reader.readNext()) != null) {
			String orderString = line[0];

			int x1int = Integer.parseInt(line[1]);
			int y1int = Integer.parseInt(line[2]);
			int z1int = Integer.parseInt(line[3]);
			Point v1 = new Point(x1int, y1int, z1int);

			int x2int = Integer.parseInt(line[4]);
			int y2int = Integer.parseInt(line[5]);
			int z2int = Integer.parseInt(line[6]);
			Point v2 = new Point(x2int, y2int, z2int);

			int x3int = Integer.parseInt(line[7]);
			int y3int = Integer.parseInt(line[8]);
			int z3int = Integer.parseInt(line[9]);
			Point v3 = new Point(x3int, y3int, z3int);

			int x4int = Integer.parseInt(line[10]);
			int y4int = Integer.parseInt(line[11]);
			int z4int = Integer.parseInt(line[12]);
			Point v4 = new Point(x4int, y4int, z4int);

			int x5int = Integer.parseInt(line[13]);
			int y5int = Integer.parseInt(line[14]);
			int z5int = Integer.parseInt(line[15]);
			Point v5 = new Point(x5int, y5int, z5int);

			int x6int = Integer.parseInt(line[16]);
			int y6int = Integer.parseInt(line[17]);
			int z6int = Integer.parseInt(line[18]);
			Point v6 = new Point(x6int, y6int, z6int);

			int x7int = Integer.parseInt(line[19]);
			int y7int = Integer.parseInt(line[20]);
			int z7int = Integer.parseInt(line[21]);
			Point v7 = new Point(x7int, y7int, z7int);

			int x8int = Integer.parseInt(line[22]);
			int y8int = Integer.parseInt(line[23]);
			int z8int = Integer.parseInt(line[24]);
			Point v8 = new Point(x8int, y8int, z8int);

			int minx = Ordering.natural().min(x1int, x2int, x3int, x4int, x5int, x6int, x7int, x8int);
			int maxx = Ordering.natural().max(x1int, x2int, x3int, x4int, x5int, x6int, x7int, x8int);
			int xSize = maxx - minx + 1;

			int miny = Ordering.natural().min(y1int, y2int, y3int, y4int, y5int, y6int, y7int, y8int);
			int maxy = Ordering.natural().max(y1int, y2int, y3int, y4int, y5int, y6int, y7int, y8int);
			int ySize = maxy - miny + 1;

			int minz = Ordering.natural().min(z1int, z2int, z3int, z4int, z5int, z6int, z7int, z8int);
			int maxz = Ordering.natural().max(z1int, z2int, z3int, z4int, z5int, z6int, z7int, z8int);
			int zSize = maxz - minz + 1;

			if (first) {
				maxOccupiedZ = maxz;
				first = false;
			}
			int order = Integer.parseInt(orderString);
			Present present = new Present(order, xSize, ySize, zSize);
			present.boundaries.add(v1);
			present.boundaries.add(v2);
			present.boundaries.add(v3);
			present.boundaries.add(v4);
			present.boundaries.add(v5);
			present.boundaries.add(v6);
			present.boundaries.add(v7);
			present.boundaries.add(v8);
			for (int i = 0; i < 8; i++) {
				present.boundaries.get(i).z = maxOccupiedZ - present.boundaries.get(i).z + 1;
			}
			presents.add(present);
		}
		return presents;
	}
}
