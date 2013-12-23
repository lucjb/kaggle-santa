package pipi.interval;

public class SliceTest {
	public static void main(String[] args) {
		CapoSleghSlice sleghSlice = CapoSleghSlice.empty(7, 7);
		sleghSlice.fill(3, 0, 1, 1);
		sleghSlice.fill(3, 6, 1, 1);
		sleghSlice.fill(0, 3, 1, 1);
		sleghSlice.fill(6, 3, 1, 1);
	}
}
