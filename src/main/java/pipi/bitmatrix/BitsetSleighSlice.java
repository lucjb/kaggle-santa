package pipi.bitmatrix;

import static java.lang.Math.min;
import first.BitsetsSleigh;
import pipi.SleighSlice;

public class BitsetSleighSlice implements SleighSlice {
	private PiolaBitset bitset = new PiolaBitset(1000 * 1000);

	public static BitsetSleighSlice filled() {
		BitsetSleighSlice sleighSlice = new BitsetSleighSlice();
		sleighSlice.bitset.set(0, sleighSlice.bitset.size());
		return sleighSlice;
	}

	public static BitsetSleighSlice freed() {
		BitsetSleighSlice sleighSlice = new BitsetSleighSlice();
		sleighSlice.bitset.set(0, sleighSlice.bitset.size(), false);
		return sleighSlice;
	}

	/* (non-Javadoc)
	 * @see pipi.bitmatrix.SleighSlice#canContain(int, int, int, int)
	 */
	@Override
	public boolean isFree(int x, int y, int dx, int dy) {
		if (x + dx > 1000) {
			return false;
		}
		if (y + dy > 1000) {
			return false;
		}
		for (int xx = x; xx < x + dx; xx++) {
			for (int yy = y; yy < y + dy; yy++) {
				if (this.bitset.get(getBitIndex(xx, yy))) {
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

	/* (non-Javadoc)
	 * @see pipi.bitmatrix.SleighSlice#clear(int, int, int, int)
	 */
	@Override
	public void free(int x, int y, int dx, int dy) {
		for (int xx = x; xx < min(x + dx, 1000); xx++) {
			for (int yy = y; yy < min(y + dy, 1000); yy++) {
				this.bitset.clear(getBitIndex(xx, yy));
			}
		}
	}

	/* (non-Javadoc)
	 * @see pipi.bitmatrix.SleighSlice#set(int, int, int, int)
	 */
	@Override
	public void fill(int x, int y, int dx, int dy) {
		for (int xx = x; xx < min(x + dx, 1000); xx++) {
			for (int yy = y; yy < min(y + dy, 1000); yy++) {
				this.bitset.set(getBitIndex(xx, yy));
			}
		}
	}

	private static int getBitIndex(int x, int y) {
		return x * 1000 + y;
	}

	public void merge(BitsetSleighSlice sleighSlice) {
		this.bitset.or(sleighSlice.bitset);
	}
}
