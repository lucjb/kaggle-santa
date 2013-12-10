package pipi;
import static java.lang.Math.max;
import static java.lang.Math.min;

import java.io.File;
import java.io.FileReader;
import java.util.List;

import au.com.bytecode.opencsv.CSVReader;

import com.google.common.collect.Lists;

import first.Point;
import first.Present;


public class Optimizer {

	private static class Presento {

	}

	public static void main(String[] args) throws Exception {
		CSVReader reader = new CSVReader(new FileReader(new File("submission.csv")), ',', '|', 1);
		List<String[]> readAll = reader.readAll();
		List<Present> presents = Lists.newLinkedList();
		for (String[] line : readAll) {
			String orderString = line[0];
			int order = Integer.parseInt(orderString);
			Point min = new Point(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
			Point max = new Point(0, 0, 0);
			for (int i = 0; i < 8; i++) {
				Point point = extractPoint(i * 3, line);
				min.x = min(min.x, point.x);
				min.y = min(min.y, point.y);
				min.z = min(min.z, point.z);
				max.x = max(max.x, point.x);
				max.y = max(max.y, point.y);
				max.z = max(max.z, point.z);
			}
			max.dimensionDifference(min);
		}
	}

	public static Point extractPoint(int i, String[] line) {
		String xString = line[1 + i];
		String yString = line[2 + i];
		String zString = line[3 + i];
		int x = Integer.parseInt(xString);
		int y = Integer.parseInt(yString);
		int z = Integer.parseInt(zString);
		Point point = new Point(x, y, z);
		return point;
	}

}
