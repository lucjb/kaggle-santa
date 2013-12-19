package pipi;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.primitives.Ints;
import com.google.common.util.concurrent.RateLimiter;

import first.Point;

public class SuperMain {

	public static void main(String[] args) {
		List<SuperPresent> presents = new SuperPresentsParser()
				.parse("presents.csv");
		SliceSuperSleigh superSleigh = new SliceSuperSleigh();

		JPanel marvinImagePanel = new JPanel() {
			@Override
			public void paintComponent(Graphics g) {
				super.paintComponents(g);
				Graphics2D graphics2d = (Graphics2D) g;
				graphics2d.setColor(Color.BLACK);
				graphics2d.fillRect(0, 0, 500, 500);
			}
		};
		JFrame frame = new JFrame();
		marvinImagePanel.setSize(1000, 1000);
		marvinImagePanel.setPreferredSize(new Dimension(1000, 1000));
		Container contentPane = frame.getContentPane();
		// contentPane.setLayout(new BorderLayout());
		frame.add(marvinImagePanel);
//		frame.setSize(1000, 1000);
		marvinImagePanel.setVisible(true);
		 frame.pack();
		frame.setVisible(true);

		RateLimiter rateLimiter = RateLimiter.create(1.0);
		int count = 0;
		// ByteProcessor imageProcessor = new ByteProcessor(1000, 1000);
		// ImagePlus currentSliceImage = new ImagePlus("My new image",
		// imageProcessor);
		// currentSliceImage.setColor(new Color(1.0f, 1.0f, 1.0f));
		try (BufferedWriter newBufferedWriter = Files.newBufferedWriter(
				Paths.get(".", "super.csv"), Charsets.UTF_8)) {

			for (SuperPresent superPresent : presents) {
				marvinImagePanel.invalidate();
				PresentDimension dimension = superPresent.getDimension();
				Box box = new Box(dimension.small, dimension.medium,
						dimension.large);

				Point point = superSleigh.putPesent(box);
				if (point == null) {
					break;
				}
				// imageProcessor.drawRect(point.x, point.y, box.dx, box.dy);
				// imageProcessor.drawRect(0, 0, 1, 1);
				// imageProcessor.drawRect(3, 3, 3, 3);
				int order = superPresent.getOrder();
				int[] ouputPresent = SuperPresent.ouputPresent(order, point,
						box);
				newBufferedWriter.write(Joiner.on(',').join(
						Ints.asList(ouputPresent)));
				newBufferedWriter.newLine();
				count++;
				if (rateLimiter.tryAcquire()) {
					System.out.printf("Progress: %f%%\n",
							100.0 * ((double) count / presents.size()));
					System.out.printf("Progress: %d\n", count);
					// currentSliceImage.updateAndDraw();
					// ImageCanvas canvas = new ImageCanvas(currentSliceImage);
					// canvas.setMagnification(1);
					// new ImageWindow(currentSliceImage, canvas);
					//
					// currentSliceImage.show();
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
