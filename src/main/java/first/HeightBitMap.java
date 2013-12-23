package first;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.BitSet;
import java.util.List;

import au.com.bytecode.opencsv.CSVWriter;

import com.google.common.collect.Lists;

public class HeightBitMap implements Cloneable {

	List<BitSet> layers = Lists.newArrayList();

	int currentZ = 0;

	public HeightBitMap() {
		BitSet bitSet = new BitSet();
		bitSet.set(0, 1000 * 1000);
		this.layers.add(bitSet);
	}

	@Override
	public Object clone() {
		HeightBitMap clone;
		try {
			clone = (HeightBitMap) super.clone();
			clone.currentZ = this.currentZ;
			clone.layers = Lists.newArrayList();
			for (BitSet eachLayer : this.layers) {
				clone.layers.add((BitSet) eachLayer.clone());
			}
			return clone;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	public BitSet getLayer(int z) {
		int index = indexFor(z);
		if (index >= layers.size()) {
			for (int i = layers.size(); i < index + 1; i++) {
				layers.add(new BitSet(1000 * 1000));
			}
		}
		return this.layers.get(index);
	}

	private int indexFor(int z) {
		return z - currentZ;
	}

	public void put(int x, int y, int xSize, int ySize, int zSize) {
		unset(x, y, xSize, ySize, currentLayer());
		set(x, y, xSize, ySize, getLayer(currentZ + zSize));
	}

	public void pushDown(int x, int y, int xSize, int ySize, int zSize, int delta) {
		unset(x, y, xSize, ySize, getLayer(currentZ + zSize));
		set(x, y, xSize, ySize, getLayer(Math.max(currentZ + zSize - delta, currentZ)));
	}

	public int fit(int x, int y, int xSize, int ySize) {
		BitSet currentLayer = currentLayer();
		for (int p = x; p < x + xSize; p++) {
			for (int q = y; q < y + ySize; q++) {
				int bitIndex = p * 1000 + q;
				if (!currentLayer.get(bitIndex)) {
					int nextSetBit = currentLayer.nextSetBit(bitIndex + 1);
					if (nextSetBit < 0 || nextSetBit / 1000 != p) {
						return 1000;
					} else {
						return nextSetBit % 1000;
					}
				}
			}
		}
		return y;
	}

	public void nextZ(int nextZ) {
		BitSet newLayer = this.getLayer(nextZ);
		for (int z = nextZ - 1; z >= currentZ; z--) {
			newLayer.or(this.getLayer(z));
		}
		for (int z = currentZ; z < nextZ; z++) {
			layers.remove(0);
		}
		this.currentZ = nextZ;
	}

	private BitSet currentLayer() {
		return layers.get(indexFor(currentZ));
	}

	private void unset(int x, int y, int xSize, int ySize, BitSet layer) {
		for (int xi = x; xi < x + xSize; xi++) {
			layer.clear(xi * 1000 + y, xi * 1000 + y + ySize);
		}
	}

	private void set(int x, int y, int xSize, int ySize, BitSet layer) {
		for (int xi = x; xi < x + xSize; xi++) {
			layer.set(xi * 1000 + y, xi * 1000 + y + ySize);
		}
	}

	public void export() {
		System.out.println("exporting..." + currentZ);
		try {
			CSVWriter w = new CSVWriter(new FileWriter(new File(new File("layersLucas"), "layerCSV" + currentZ + ".csv")), ',', CSVWriter.NO_QUOTE_CHARACTER);
			BitSet currentLayer = currentLayer();
			for (int xi = 0; xi < 1000; xi++) {
				for (int yi = 0; yi < 1000; yi++) {
					if (currentLayer.get(xi * 1000 + yi)) {
						String[] line = new String[2];
						line[0] = String.valueOf(xi);
						line[1] = String.valueOf(yi);
						w.writeNext(line);
					}
				}
			}
			w.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("done.");
	}
}
