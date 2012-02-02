package wr3.util.tuple;

public class Sextuple<T1 extends Comparable<T1>, T2 extends Comparable<T2>, T3 extends Comparable<T3>, T4 extends Comparable<T4>, T5 extends Comparable<T5>, T6 extends Comparable<T6>> extends Tuple<T1, Tuple<T2, Tuple<T3, Tuple<T4, Tuple<T5, Tuple<T6, End>>>>>> {

	public Sextuple(T1 m1, T2 m2, T3 m3, T4 m4, T5 m5, T6 m6) {
		super(m1, Tuple.from(m2, m3, m4, m5, m6));
	}

}
