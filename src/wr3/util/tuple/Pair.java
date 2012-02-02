package wr3.util.tuple;

public class Pair<T1 extends Comparable<T1>, T2 extends Comparable<T2>> extends Tuple<T1, Tuple<T2, End>> {

	public Pair(T1 m1, T2 m2) {
		super(m1, Tuple.from(m2));
	}

}
