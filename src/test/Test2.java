package test;

import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import wr3.util.Numberx;
import wr3.util.Stringx;
import wr3.util.tuple.Pair;
import wr3.util.tuple.Triple;
import wr3.util.tuple.Tuple;

public class Test2 {

	Map<Long, Object> queue = new TreeMap<Long, Object>();

	void queue() {

		String s;
		for (int i = 0; i < 1000000; i++) {
			s = new String(new char[256]);
			put(1, i, s);
		}
	}

	void put(int prio, int seq, Object o) {
		queue.put(Long.valueOf(prio*1000000000 + seq), o);
	}

	void uuid() {
		System.out.println(UUID.randomUUID().toString());
	}

	// 找到 abcde * f = edcba 的数字
	void test() {

		for(int i = 12345; i < 98765; i++) {
			Set<Integer> s1 = Numberx.digitSet(i);
			if (s1.size()!=5 || s1.contains(0)) continue;
			for (int j = 1; j < 10; j++) {
				int k = i*j;
				if (k > 99999 || Numberx.digitSet(k).size()!=5) continue;
				int[] a1 = Numberx.digits(i);
				int[] a2 = Numberx.digits(k);
				if (	a1[0]==a2[4] &&
						a1[1]==a2[3] &&
						a1[2]==a2[2] &&
						a1[3]==a2[1] &&
						a1[4]==a2[0]) {
					System.out.printf("%d * %d = %d\n", i, j, k);
				}
			}
		}

	}

	// test printf
	void printf() {
		Date d = new Date();
		System.out.printf("foo1 %d foo2\n %s\n", 123, d);
	}

	void args(String... ss) {
		// 调用 args() -> ss = new String[0];
		System.out.println("args: " + ss.length);
	}

	Pair<Integer, String> tuple() {

		Triple<Long, Long, Long> t = Tuple.from(10L,20L,30L);
		System.out.printf("(%d,%d,%d)\n",
				Tuple.get1(t), Tuple.get2(t), Tuple.get3(t));

		return Tuple.from(10, "aaa");
	}

	// ----------------- main() -----------------//
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Test2 o = new Test2();
		o.printf();

		o.args();
		o.args("a", "b", "c");
		Pair<Integer, String> t = o.tuple();
		int i = Tuple.get1(t);
		String s = Tuple.get2(t);
		System.out.printf("t.i=%d, t.s=%s\n", i, s);

		String[] rt = Stringx.remove1(new String[] {"a", "b", null, "c", "b"}, "b");
		System.out.println(Stringx.join(rt));
	}

}
