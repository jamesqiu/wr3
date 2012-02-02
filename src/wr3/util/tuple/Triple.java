package wr3.util.tuple;

public class Triple<T1 extends Comparable<T1>, T2 extends Comparable<T2>, T3 extends Comparable<T3>> extends Tuple<T1, Tuple<T2, Tuple<T3, End>>> {

	public Triple(T1 m1, T2 m2, T3 m3) {
		super(m1, Tuple.from(m2, m3));
	}
	
}
