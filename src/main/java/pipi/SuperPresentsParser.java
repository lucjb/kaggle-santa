package pipi;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.SerializationUtils;

import au.com.bytecode.opencsv.CSVReader;

import com.google.common.collect.Lists;


public class SuperPresentsParser {

	public static void main(String[] args) {
		List<SuperPresent> parse = new SuperPresentsParser()
				.parse("presents.csv");
		for (SuperPresent present : parse) {
			System.out.println(present);
		}
	}

	public List<SuperPresent> parse(String inputFileName) {
		Path cachePath = Paths.get(inputFileName + ".bin");
		try(InputStream inputStream = Files.newInputStream(cachePath, StandardOpenOption.READ)){
			return SerializationUtils.deserialize(inputStream);
		} catch (IOException e1) {
		}
		
		try (CSVReader reader = new CSVReader(new FileReader(new File(
				inputFileName)), ',', '|', 1)) {
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
				SuperPresent present = new SuperPresent(order,
						Dimension3d.create(x, y, z));
				presents.add(present);
			}
			try(OutputStream outputStream = Files.newOutputStream(cachePath, StandardOpenOption.READ)){
				SerializationUtils.serialize(presents, outputStream);
			} 
			
			return presents;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
