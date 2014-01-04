package pipi.interval;

import static pipi.interval.Rectangle.of;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import pipi.main.BruteForce;

import com.google.common.collect.Lists;

@RunWith(Parameterized.class)
public class SliceBruteTest {
	private final Rectangle[] rectangles;

	private static Object[] oa(Rectangle... rectangles) {
		return new Object[] { rectangles };
	}

	@Parameters
	public static Collection<Object[]> parameters() {
		return Arrays.<Object[]> asList(oa(of(1, 0, 998, 993), of(5, 1, 991, 995), of(0, 3, 999, 995)),
				oa(of(0, 0, 999, 997), of(2, 2, 998, 996), of(0, 0, 999, 1000)),
				oa(of(0, 0, 999, 997), of(2, 1, 998, 996), of(0, 0, 999, 1000)), oa(of(1, 1, 999, 999), of(0, 1, 998, 998)),

				oa(of(8, 325, 8, 15), of(13, 270, 8, 28), of(13, 298, 4, 4), of(17, 260, 5, 8)),

				oa(of(289, 0, 101, 104), of(271, 104, 31, 32)), oa(of(0, 0, 2, 3), of(0, 210, 78, 160)),
				oa(of(0, 0, 10, 10), of(500, 0, 10, 50)),
				oa(of(24, 353, 23, 54), of(21, 411, 3, 8), of(26, 407, 3, 4), of(24, 410, 2, 7)),
				oa(of(0, 0, 2, 3), of(0, 692, 114, 219), of(100, 215, 22, 28), of(118, 466, 14, 16)),
				oa(of(10, 0, 5, 15), of(5, 5, 15, 5))/*,
				oa(of(0,0,998,996), of(0,0,996,998), of(0,0,997,997))*/

		);
	}

	public SliceBruteTest(Rectangle... rectangles) {
		this.rectangles = rectangles;
	}

	@Test
	public void testArea() {
		BruteForce.assertRectangles(Lists.newArrayList(this.rectangles));
	}

}
