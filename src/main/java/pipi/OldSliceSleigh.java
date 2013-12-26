package pipi;

import java.util.Map;
import java.util.PriorityQueue;

import com.google.common.collect.Maps;
import com.google.common.collect.Queues;

import pipi.bitmatrix.BitsetSleighSlice;
import first.Point;

public class OldSliceSleigh {
	private Map<Integer, BitsetSleighSlice> slices = Maps.newHashMap();
	private PriorityQueue<Integer> levels = Queues.newPriorityQueue();

	private int currentZ;

	public SleighSlice getSlice(int level) {
		BitsetSleighSlice sleighSlice = this.slices.get(level);
		if (sleighSlice == null) {
			sleighSlice = BitsetSleighSlice.filled();
			this.slices.put(level, sleighSlice);
			this.levels.offer(level);
		}
		return sleighSlice;
	}

	public OldSliceSleigh() {
		this.slices.put(0, BitsetSleighSlice.filled());
		this.currentZ = 0;
	}

	public Point putPesent(Box box) {
		for (;;) {
			BitsetSleighSlice sleighSlice = this.slices.get(this.currentZ);
			for (int x = 0; x <= 1000 - box.dx; x++) {
				for (int y = 0; y <= 1000 - box.dy; y++) {
					boolean canContain = sleighSlice.isFree(x, y, box.dx, box.dy);
					if (canContain) {
						sleighSlice.free(x, y, box.dx, box.dy);
						int newZ = this.currentZ + box.dz;
						SleighSlice zSleighSlice = this.getSlice(newZ);
						zSleighSlice.fill(x, y, box.dx, box.dy);
						return new Point(x, y, this.currentZ);
					}
				}
			}
			this.currentZ = this.levels.remove();
			this.slices.get(this.currentZ).merge(sleighSlice);
		}
	}

	public int getLastZ() {
		Integer lastZ;
		do {
			lastZ = levels.poll();
		} while (!levels.isEmpty());
		return lastZ;
	}
	
	public int getCurrentZ() {
		return currentZ;
	}

}
