package pipi.packer;

import java.util.List;

import com.google.common.collect.Lists;

public class Indexed<T> {
	public final int index;
	public final T indexee;

	public Indexed(int index, T indexee) {
		this.index = index;
		this.indexee = indexee;
	}

	public static <T> List<Indexed<T>> index(List<T> dimensions) {
		List<Indexed<T>> indexeds = Lists.newArrayList();
		int i = 0;
		for (T t : dimensions) {
			indexeds.add(new Indexed<T>(i, t));
			i++;
		}
		return indexeds;
	}
}
