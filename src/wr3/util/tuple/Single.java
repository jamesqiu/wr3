package wr3.util.tuple;

public class Single<T1 extends Comparable<T1>> extends Tuple<T1, End> {
	
	public Single(T1 m1) {
		super(m1, End.getInstance());
	}

}
