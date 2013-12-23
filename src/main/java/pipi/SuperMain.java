package pipi;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.atomic.AtomicReferenceArray;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;
import com.google.common.util.concurrent.RateLimiter;

import first.Point;

public class SuperMain {

	private static final class JPanelExtension extends JPanel {
		private final AtomicReferenceArray<Rectangle> rectangles;
		volatile public int index = 0;

		private JPanelExtension(AtomicReferenceArray<Rectangle> rectangles) {
			this.rectangles = rectangles;
		}

		@Override
		public void paintComponent(Graphics g) {
			super.paintComponents(g);
			Graphics2D graphics2d = (Graphics2D) g;
			graphics2d.setColor(Color.BLACK);
			for (int i = 0; i < index; i++) {
				Rectangle rectangle = rectangles.get(i);
				graphics2d.fillRect(rectangle.x, rectangle.y, rectangle.width,
						rectangle.height);
			}
		}
	}

	public static void main(String[] args) throws Exception {
		List<SuperPresent> presents = new SuperPresentsParser()
				.parse("presents.csv");
		SliceSuperSleigh superSleigh = new SliceSuperSleigh();

		// final AtomicReferenceArray<Rectangle> rectangles = new
		// AtomicReferenceArray<Rectangle>(1000*1000);

		// JPanelExtension marvinImagePanel = new JPanelExtension(rectangles);
		// JFrame frame = new JFrame();
		// frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// marvinImagePanel.setSize(1000, 1000);
		// marvinImagePanel.setPreferredSize(new Dimension(1000, 1000));
		// contentPane.setLayout(new BorderLayout());
		// frame.add(marvinImagePanel);
		// marvinImagePanel.setVisible(true);
		// frame.pack();
		// frame.setVisible(true);

		RateLimiter rateLimiter = RateLimiter.create(1.0);
		int count = 0;
		// ByteProcessor imageProcessor = new ByteProcessor(1000, 1000);
		// ImagePlus currentSliceImage = new ImagePlus("My new image",
		// imageProcessor);
		// currentSliceImage.setColor(new Color(1.0f, 1.0f, 1.0f));
		List<OutputPresent> outputPresents = Lists.newArrayList();
		for (SuperPresent superPresent : presents) {
			// marvinImagePanel.invalidate();
			PresentDimension dimension = superPresent.getDimension();
			Box box = new Box(dimension.small, dimension.medium,
					dimension.large);

			int order = superPresent.getOrder();
			 Point point = superSleigh.putPesent(box);
//			Point point = new Point(1, 1, 1);
			// rectangles.set(marvinImagePanel.index, (new Rectangle(point.x,
			// point.y, box.dx, box.dy)));
			// marvinImagePanel.index++;
			// imageProcessor.drawRect(point.x, point.y, box.dx, box.dy);
			// imageProcessor.drawRect(0, 0, 1, 1);
			// imageProcessor.drawRect(3, 3, 3, 3);
			outputPresents.add(new OutputPresent(order, point, box));
			count++;
			// marvinImagePanel.repaint();
			if (rateLimiter.tryAcquire()) {
				System.out.printf("Progress: %f%%\n",
						100.0 * ((double) count / presents.size()));
				System.out.printf("Progress: %d\n", count);
			}
		}
		try (BufferedWriter newBufferedWriter = Files.newBufferedWriter(
				Paths.get(".", "super.csv"), Charsets.UTF_8)) {
			newBufferedWriter
					.write("PresentId,x1,y1,z1,x2,y2,z2,x3,y3,z3,x4,y4,z4,x5,y5,z5,x6,y6,z6,x7,y7,z7,x8,y8,z8");
			newBufferedWriter.newLine();
			int lastZ = superSleigh.getLastZ();
			for (OutputPresent outputPresent : outputPresents) {
				int[] ouputPresent = SuperPresent.ouputPresent(
						outputPresent.getOrder(), outputPresent.getPoint(),
						outputPresent.getBox(), lastZ);
				newBufferedWriter.write(Joiner.on(',').join(
						Ints.asList(ouputPresent)));
				newBufferedWriter.newLine();
			}

		}
	}

}
