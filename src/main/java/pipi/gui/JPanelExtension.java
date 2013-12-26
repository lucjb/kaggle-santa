package pipi.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReferenceArray;

import javax.swing.JPanel;

public class JPanelExtension extends JPanel {
	private final List<Rectangle> rectangles;
	volatile public int index = 0;

	public JPanelExtension(List<Rectangle> rectangles2) {
		this.rectangles = rectangles2;
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponents(g);
		Graphics2D graphics2d = (Graphics2D) g;
		graphics2d.setColor(Color.BLACK);
		for (int i = 0; i < this.rectangles.size(); i++) {
			Rectangle rectangle = this.rectangles.get(i);
			graphics2d.fillRect(rectangle.x, rectangle.y, rectangle.width,
					rectangle.height);
		}
	}
	
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

	// ByteProcessor imageProcessor = new ByteProcessor(1000, 1000);
	// ImagePlus currentSliceImage = new ImagePlus("My new image",
	// imageProcessor);
	// currentSliceImage.setColor(new Color(1.0f, 1.0f, 1.0f));
	
//	Point point = new Point(1, 1, 1);
	// rectangles.set(marvinImagePanel.index, (new Rectangle(point.x,
	// point.y, box.dx, box.dy)));
	// marvinImagePanel.index++;
	// imageProcessor.drawRect(point.x, point.y, box.dx, box.dy);
	// imageProcessor.drawRect(0, 0, 1, 1);
	// imageProcessor.drawRect(3, 3, 3, 3);

	// marvinImagePanel.repaint();
	
}