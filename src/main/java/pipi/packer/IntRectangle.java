package pipi.packer;

public class IntRectangle {

	public final int left;
	public final int right;
	public final int up;
	public final int down;

	public IntRectangle(int left, int right, int up, int down) {
		this.left = left;
		this.right = right;
		this.up = up;
		this.down = down;
	}
	
	public int perimeter(){
		return this.left + this.right + this.up + this.down;
	}
	
	public int sides(){
		return Integer.signum(this.left) + Integer.signum(this.right) + Integer.signum(this.up) + Integer.signum(this.down);
		
	}
	
}
