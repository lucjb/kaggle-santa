package pipi.interval;

import static pipi.interval.Rectangle.of;

import java.awt.Dimension;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.swing.JFrame;

import pipi.gui.JPanelExtension;
import pipi.main.BruteForce;
import pipi.sandbox.BitsetSlice;

import com.google.common.collect.Lists;

public class SliceTest {
	public static void main(String[] args) {
		IntervalSlice sleighSlice = IntervalSlice.empty(1000, 1000);
		BitsetSlice testSleighSlice = BitsetSlice.freed(1000);
//		rectangles.add(new Rectangle(0, 0, 2, 3)); //1
//		rectangles.add(new Rectangle(0, 3, 73, 207));
//		rectangles.add(new Rectangle(0, 210, 78, 160));//1
//		rectangles.add(new Rectangle(73, 0, 3, 8));
//		rectangles.add(new Rectangle(73, 8, 9, 9));
//		rectangles.add(new Rectangle(73, 17, 120, 170));
//		rectangles.add(new Rectangle(78, 187, 91, 116));
//		rectangles.add(new Rectangle(78, 303, 142, 206));
//		rectangles.add(new Rectangle(169, 187, 8, 28));
//		rectangles.add(new Rectangle(73, 187, 2, 3));
//		rectangles.add(new Rectangle(169, 215, 22, 41));
//		rectangles.add(new Rectangle(169, 256, 11, 31));
//		rectangles.add(new Rectangle(177, 187, 20, 28));
//		rectangles.add(new Rectangle(180, 256, 13, 29));
//		rectangles.add(new Rectangle(193, 0, 96, 98));
//		rectangles.add(new Rectangle(193, 215, 4, 26));
//		rectangles.add(new Rectangle(193, 241, 9, 34));
//		rectangles.add(new Rectangle(75, 187, 3, 4));
//		rectangles.add(new Rectangle(193, 98, 78, 84));
//		rectangles.add(new Rectangle(220, 182, 114, 219));
//		rectangles.add(new Rectangle(193, 275, 22, 28));
//		rectangles.add(new Rectangle(220, 401, 185, 195));
//		rectangles.add(new Rectangle(73, 190, 2, 3));
//		rectangles.add(new Rectangle(197, 182, 9, 31));
//		rectangles.add(new Rectangle(289, 0, 101, 104));//2
//		rectangles.add(new Rectangle(271, 104, 31, 32));//2

		
		
		
//		rectangles.add(new Rectangle(0, 0, 6, 6));
//		rectangles.add(new Rectangle(0, 6, 2, 5));
//		rectangles.add(new Rectangle(0, 11, 72, 195));
//		rectangles.add(new Rectangle(0, 206, 17, 32));
//		rectangles.add(new Rectangle(2, 6, 3, 5));
//		rectangles.add(new Rectangle(0, 238, 14, 32));
//		rectangles.add(new Rectangle(0, 270, 7, 36));
//		rectangles.add(new Rectangle(5, 6, 3, 4));
//		rectangles.add(new Rectangle(0, 306, 8, 54));
//		rectangles.add(new Rectangle(0, 360, 105, 171));
//		rectangles.add(new Rectangle(7, 270, 6, 16));
//		rectangles.add(new Rectangle(72, 0, 131, 160));
//		rectangles.add(new Rectangle(72, 160, 161, 179));
//		rectangles.add(new Rectangle(8, 286, 5, 27));
//		rectangles.add(new Rectangle(8, 313, 2, 3));
//		rectangles.add(new Rectangle(8, 316, 8, 9));
//		rectangles.add(new Rectangle(8, 325, 8, 15));   //3
//		rectangles.add(new Rectangle(13, 270, 8, 28));  //3
//		rectangles.add(new Rectangle(10, 313, 2, 2));
//		rectangles.add(new Rectangle(13, 298, 4, 4));   //3
//		rectangles.add(new Rectangle(14, 238, 15, 22));
//		rectangles.add(new Rectangle(14, 260, 3, 6));
//		rectangles.add(new Rectangle(105, 339, 106, 172));
//		rectangles.add(new Rectangle(17, 260, 5, 8));   //3
//		rectangles.add(new Rectangle(17, 206, 21, 23));
//		rectangles.add(new Rectangle(17, 298, 26, 27));
//		rectangles.add(new Rectangle(14, 266, 3, 4));

//		rectangles.add(new Rectangle(1,1,999,999)); done
//		rectangles.add(new Rectangle(0,1,998,998)); done

		
//		rectangles.add(new Rectangle(0,0,999,997));  done
//		rectangles.add(new Rectangle(2,1,998,996));  done
//		rectangles.add(new Rectangle(0,0,999,1000)); done

//		rectangles.add(new Rectangle(0,0,999,997)); done  
//		rectangles.add(new Rectangle(2,2,998,996)); done
//		rectangles.add(new Rectangle(0,0,999,1000));done
		
//		rectangles.add(new Rectangle(1,0,998,993));   
//		rectangles.add(new Rectangle(5,1,991,995)); 
//		rectangles.add(new Rectangle(0,3,999,995)); 

//		rectangles.add(new Rectangle(0,3,999,995)); 

		

		
		List<pipi.interval.Rectangle> rectangles = Arrays.asList(
				of(24, 353, 23, 54), of(24, 410, 2, 7), of(21, 411, 3, 8), of(26, 407, 3, 4)
				);


		
		 show(rectangles);

		
		int i = 0;
		BruteForce.assertRectangles(rectangles);
//		for (Rectangle rectangle : rectangles) {
//			testSleighSlice.fill(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
//			sleighSlice.fill(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
//			Collection<pipi.interval.MaximumRectangle> maximumRectangles = sleighSlice.getMaximumRectangles();
//			System.out.println(i + "->" + maximumRectangles);
//			for (pipi.interval.MaximumRectangle rectangle2 : maximumRectangles) {
//				boolean free = testSleighSlice.isFree(rectangle2.getHorizontalRange().getFrom(), rectangle2.getVerticalRange().getFrom(), rectangle2.getHorizontalRange().length(), rectangle2.getVerticalRange().length());
//				if(!free){
//					System.out.println("ACA!!!");
//					return;
//				}
//			}
//			i++;
//		}
		System.out.println("Listo");
		
		//		Random random = new Random();
//		for (;;) {
//			int w = random.nextInt(99) + 1;
//			int h = random.nextInt(99) + 1;
//			int x = random.nextInt(1000 - w);
//			int y = random.nextInt(1000 - h);
//			sleghSlice.fill(x, y, w, h);
//			System.out.println(sleghSlice.getMaximumRectangles().size());
//			
//		}

	}

	public static void show(List<pipi.interval.Rectangle> rectangles) {
		JPanelExtension marvinImagePanel = new JPanelExtension(rectangles, Lists.reverse(rectangles).subList(0, 0));
		 JFrame frame = new JFrame();
		 frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		 marvinImagePanel.setSize(1000, 1000);
		 marvinImagePanel.setPreferredSize(new Dimension(1000, 1000));
//		 contentPane.setLayout(new BorderLayout());
		 frame.add(marvinImagePanel);
		 marvinImagePanel.setVisible(true);
		 frame.pack();
		 frame.setVisible(true);
		 frame.repaint();
	}
}
