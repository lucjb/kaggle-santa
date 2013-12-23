package pipi;

import java.util.Map;
import java.util.PriorityQueue;

import com.google.common.collect.Maps;
import com.google.common.collect.Queues;

import pipi.bitmatrix.BitesetSleighSlice;
import first.Point;

public class SliceSuperSleigh {
	private Map<Integer, BitesetSleighSlice> slices = Maps.newHashMap();
	private PriorityQueue<Integer> levels = Queues.newPriorityQueue();

	private int currentZ;

	public SleighSlice getSlice(int level) {
		BitesetSleighSlice sleighSlice = this.slices.get(level);
		if (sleighSlice == null) {
			sleighSlice = BitesetSleighSlice.filled();
			this.slices.put(level, sleighSlice);
			this.levels.offer(level);
		}
		return sleighSlice;
	}

	public SliceSuperSleigh() {
		this.slices.put(0, BitesetSleighSlice.filled());
		this.currentZ = 0;
	}

	public Point putPesent(Box box) {
		for (;;) {
			SleighSlice sleighSlice = this.slices.get(this.currentZ);
			for (int x = 0; x <= 1000 - box.dx; x++) {
				for (int y = 0; y <= 1000 - box.dy; y++) {
//					int superContain = sleighSlice.superContain(x, y, box.dx, box.dy);
					boolean canContain = sleighSlice.isFree(x, y, box.dx, box.dy);
					if (canContain) {
						sleighSlice.free(x, y, box.dx, box.dy);
						int newZ = this.currentZ + box.dz;
						SleighSlice zSleighSlice = this.getSlice(newZ);
						zSleighSlice.fill(x, y, box.dx, box.dy);
						return new Point(x, y, this.currentZ);
					}
//					else{
//						y+= superContain;
//					}
				}
			}
			this.currentZ = this.levels.remove();
			// return null;
		}
	}

	public int getLastZ() {
		Integer lastZ;
		do {
			lastZ = levels.poll();
		} while (!levels.isEmpty());
		return lastZ;
	}

}
