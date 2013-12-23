package pipi;

public interface SleighSlice {

	public abstract boolean isFree(int x, int y, int dx, int dy);

	public abstract void free(int x, int y, int dx, int dy);

	public abstract void fill(int x, int y, int dx, int dy);

}