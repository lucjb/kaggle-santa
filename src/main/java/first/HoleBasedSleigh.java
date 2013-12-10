package first;
import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

public class HoleBasedSleigh {

	List<List<Hole>> space = Lists.newArrayList();

	public void addPresent(Present present) {
		int xSize = present.xSize;
		int ySize = present.ySize;
		int zSize = present.zSize;
		int sup = xSize * ySize;

		int z = 0;
		while (true) {
			if (z == this.space.size()) {
				List<Hole> slice = Lists.newArrayList();
				slice.add(new Hole(0, 1000, 0, 1000));
				this.space.add(slice);
			}
			List<Hole> slice = space.get(z);
			for (Hole hole : slice) {
				
			}
		}

	}

}
