package pipi;

import java.util.Deque;

import com.google.common.collect.Queues;

public class PresentBatch {
	private Deque<Dimension2d> presentsStack = Queues.newArrayDeque();
	private int area;
	private int maxArea = 1000 * 1000;
	public PresentBatch() {

	}

	boolean pushPresent(Dimension2d rectangle){
		int newArea = this.area + rectangle.area();
		if(newArea > this.maxArea){
			return false;
		}
		this.area = newArea;
		return true;
	}
	
	void popPresent(){
		Dimension2d last = this.presentsStack.getLast();
		this.presentsStack.removeLast();
		this.area -= last.area();
	}
	
}
