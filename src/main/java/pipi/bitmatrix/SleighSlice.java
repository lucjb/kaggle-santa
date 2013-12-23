package pipi.bitmatrix;

import static java.lang.Math.min;

public class SleighSlice {
	private PiolaBitset bitset = new PiolaBitset(1000 * 1000);

	public static SleighSlice filled() {
		SleighSlice sleighSlice = new SleighSlice();
		sleighSlice.bitset.set(1, sleighSlice.bitset.size());
		return sleighSlice;
	}

	public boolean canContain(int x, int y, int dx, int dy) {
		if (x + dx > 1000) {
			return false;
		}
		if (y + dy > 1000) {
			return false;
		}
		for (int xx = x; xx < x + dx; xx++) {
			for (int yy = y; yy < y + dy; yy++) {
				if (!this.bitset.get(getBitIndex(xx, yy))) {
					return false;
				}
			}
		}

		return true;
	}

	public int superContain(int x, int y, int dx, int dy) {
		if (x + dx > 1000) {
			return 1001 - y;
		}
		if (y + dy > 1000) {
			return 1001 - y;
		}
		for (int xx = x; xx < x + dx; xx++) {
			for (int yy = y; yy < y + dy; yy++) {
				if (!this.bitset.get(getBitIndex(xx, yy))) {
					return yy - y;
				}
			}
		}
		return dy;
	}

	public void clear(int x, int y, int dx, int dy) {
		for (int xx = x; xx < min(x + dx, 1000); xx++) {
			for (int yy = y; yy < min(y + dy, 1000); yy++) {
				this.bitset.clear(getBitIndex(xx, yy));
			}
		}
	}

	public void set(int x, int y, int dx, int dy) {
		for (int xx = x; xx < min(x + dx, 1000); xx++) {
			for (int yy = y; yy < min(y + dy, 1000); yy++) {
				this.bitset.set(getBitIndex(xx, yy));
			}
		}
	}

	private static int getBitIndex(int x, int y) {
		return x * 1000 + y;
	}

	public void merge(SleighSlice sleighSlice) {
		this.bitset.or(sleighSlice.bitset);
	}
}
