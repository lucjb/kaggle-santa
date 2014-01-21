package pipi.gui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.JFrame;
import javax.swing.JPanel;

import pipi.interval.Rectangle;

public class RectangleView extends JPanel {

	private Collection<RectangleSet> rectangleSets;

	public RectangleView(List<RectangleSet> rectangleSets) {
		this.rectangleSets = rectangleSets;
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponents(g);
		Graphics2D graphics2d = (Graphics2D) g;
		for (RectangleSet rectangleSet : this.rectangleSets) {
			graphics2d.setColor(rectangleSet.getColor());
			for (Rectangle rectangle : rectangleSet.getRectangles()) {
				drawRectangle(graphics2d, rectangle);
			}
		}
	}

	private void drawRectangle(Graphics2D graphics2d, Rectangle rectangle) {
		graphics2d.fillRect(rectangle.point2d.x, rectangle.point2d.y, rectangle.getBox2d().dx, rectangle.getBox2d().dy);
	}

	public static void show(List<RectangleSet> rectangleSets) {
		final Lock lock = new ReentrantLock();
		final Condition newCondition = lock.newCondition();

		RectangleView drawingPAnel = new RectangleView(rectangleSets);

		JFrame frame = new JFrame();
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				super.windowClosed(e);
				lock.lock();
				try{
					newCondition.signal();
				}finally{
					lock.unlock();
				}
			}
		});

		frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		drawingPAnel.setSize(1000, 1000);
		drawingPAnel.setPreferredSize(new Dimension(1000, 1000));
		frame.add(drawingPAnel);
		drawingPAnel.setVisible(true);
		frame.pack();
		frame.setVisible(true);
		frame.repaint();
		lock.lock();
		try {
			newCondition.await();
		} catch (InterruptedException e1) {
			throw new RuntimeException(e1);
		}finally{
			lock.unlock();
		}
	}

}