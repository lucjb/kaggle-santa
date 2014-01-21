package pipi;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import au.com.bytecode.opencsv.CSVReader;

import com.google.common.collect.Lists;

public class SuperPresentsParser {

	public static void main(String[] args) {
		List<SuperPresent> parse = new SuperPresentsParser().parse("presents.csv");
		for (SuperPresent present : parse) {
			System.out.println(present);
		}
	}

	public List<SuperPresent> parse(String inputFileName) {
		try (CSVReader reader = new CSVReader(new FileReader(new File(inputFileName)), ',', '|', 1)) {
			List<String[]> readAll = reader.readAll();
			ArrayList<SuperPresent> presents = Lists.newArrayList();
			for (String[] line : readAll) {
				String orderString = line[0];
				String xString = line[1];
				String yString = line[2];
				String zString = line[3];
				int order = Integer.parseInt(orderString);
				int x = Integer.parseInt(xString);
				int y = Integer.parseInt(yString);
				int z = Integer.parseInt(zString);
				SuperPresent present = new SuperPresent(order, Dimension3d.create(x, y, z));
				presents.add(present);
			}
			return presents;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
