package pipi.bitmatrix;

import static java.lang.Math.min;
import pipi.Slice;

public class BitsetSlice implements Slice {
	private PiolaBitset bitset;
	private int size;

	public BitsetSlice(int size) {
		this.size = size;
		this.bitset = new PiolaBitset(size * size);
	}

	public static BitsetSlice filled(int size) {
		BitsetSlice sleighSlice = new BitsetSlice(size);
		sleighSlice.bitset.set(0, size*size);
		return sleighSlice;
	}

	public static BitsetSlice freed(int size) {
		BitsetSlice sleighSlice = new BitsetSlice(size);
		sleighSlice.bitset.set(0, size*size, false);
		return sleighSlice;
	}

	/* (non-Javadoc)
	 * @see pipi.bitmatrix.SleighSlice#canContain(int, int, int, int)
	 */
	@Override
	public boolean isFree(int x, int y, int dx, int dy) {
		if (x + dx > this.size) {
			return false;
		}
		if (y + dy > this.size) {
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

	@Override
	public void free(int x, int y, int dx, int dy) {
		for (int xx = x; xx < min(x + dx, this.size); xx++) {
			for (int yy = y; yy < min(y + dy, this.size); yy++) {
				this.bitset.clear(getBitIndex(xx, yy));
			}
		}
	}

	/* (non-Javadoc)
	 * @see pipi.bitmatrix.SleighSlice#set(int, int, int, int)
	 */
	@Override
	public void fill(int x, int y, int dx, int dy) {
		for (int xx = x; xx < min(x + dx, this.size); xx++) {
			for (int yy = y; yy < min(y + dy, this.size); yy++) {
				this.bitset.set(getBitIndex(xx, yy));
			}
		}
	}

	private int getBitIndex(int x, int y) {
		return x * this.size + y;
	}

	public void merge(BitsetSlice sleighSlice) {
		this.bitset.or(sleighSlice.bitset);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.bitset == null) ? 0 : this.bitset.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BitsetSlice other = (BitsetSlice) obj;
		if (this.bitset == null) {
			if (other.bitset != null)
				return false;
		} else if (!this.bitset.equals(other.bitset))
			return false;
		return true;
	}
	
	
}
