package first;

import java.util.BitSet;
import java.util.List;

import com.google.common.collect.Lists;

public class HeightBitMap {

	private List<BitSet> layers = Lists.newArrayList();

	int currentZ = 0;

	public HeightBitMap() {
		BitSet bitSet = new BitSet();
		bitSet.set(0, 1000 * 1000);
		this.layers.add(bitSet);
	}

	public BitSet getLayer(int z) {
		if (z >= layers.size()) {
			for (int i = layers.size(); i < z + 1; i++) {
				layers.add(new BitSet(1000 * 1000));
			}
		}
		return this.layers.get(z);
	}

	public void put(int x, int y, int xSize, int ySize, int zSize) {
		unset(x, y, xSize, ySize, currentLayer());
		set(x, y, xSize, ySize, getLayer(currentZ + zSize));
	}

	public int fit(int x, int y, int xSize, int ySize) {
		BitSet currentLayer = currentLayer();
		for (int p = x; p < x + xSize; p++) {
			for (int q = y; q < y + ySize; q++) {
				int bitIndex = p * 1000 + q;
				if (!currentLayer.get(bitIndex)) {
					while (q < 1000 + ySize && !currentLayer.get(p * 1000 + q)) {
						q++;
					}
					return q;
				}
			}
		}
		return y;
	}

	public void nextZ(int nextZ) {
		BitSet currentLayer = currentLayer();
		for (int i = currentZ + 1; i <= nextZ; i++) {
			this.getLayer(i).or(currentLayer);
		}
		this.currentZ = nextZ;
	}

	private BitSet currentLayer() {
		return layers.get(currentZ);
	}

	private void unset(int x, int y, int xSize, int ySize, BitSet layer) {
		for (int xi = x; xi < x + xSize; xi++) {
			for (int yi = x; yi < y + ySize; yi++) {
				layer.clear(x * 1000 + y);
			}
		}
	}

	private void set(int x, int y, int xSize, int ySize, BitSet layer) {
		for (int xi = x; xi < x + xSize; xi++) {
			for (int yi = x; yi < y + ySize; yi++) {
				layer.set(x * 1000 + y);
			}
		}
	}
}
