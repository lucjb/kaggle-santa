package pipi.interval;

import java.util.Collection;


public class SliceTest {
	public static void main(String[] args) {
		InvervalSleghSlice sleghSlice = InvervalSleghSlice.empty(6, 6);
		sleghSlice.fill(0, 3, 1, 1);
		sleghSlice.fill(2, 0, 1, 3);
		sleghSlice.fill(3, 5, 1, 1);
		sleghSlice.fill(5, 2, 1, 1);
		
		Collection<Rectangle> maximumRectangles = sleghSlice.getMaximumRectangles();
		for (Rectangle rectangle : maximumRectangles) {
			System.out.println(rectangle);
		}
	}
}
