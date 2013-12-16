package first;


import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import au.com.bytecode.opencsv.CSVReader;

import com.google.common.collect.Lists;

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
}
