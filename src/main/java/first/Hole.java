package first;
import java.util.List;

public class Hole {

	int x;
	int xSize;
	int y;
	int ySize;

	public Hole(int x, int xSize, int y, int ySize) {
		super();
		this.x = x;
		this.xSize = xSize;
		this.y = y;
		this.ySize = ySize;
	}

	public boolean fits(int xSize, int ySize) {
		return this.xSize >= xSize && this.ySize >= ySize;
	}


}
