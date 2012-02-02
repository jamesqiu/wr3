package wr3.clj;

import clojure.lang.RT;
import clojure.lang.Var;

public class CallClj {

	private static void test3() throws Exception {
		// �� classpath ת�� .clj �ļ�; ��һ��װ����Ҫ���2s���Ժ�ÿ�δ��150ms
		RT.loadResourceScript("wr3/clj/s.clj");
		Var foo = RT.var("wr3.clj.s", "left");
		Object rt = foo.invoke("hello-world", "-");
		System.out.println(rt);
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		System.out.println("-- call clojure from java --1");
		long t0 = System.currentTimeMillis();
		test3();
		long t1 = System.currentTimeMillis();
		System.out.println("time: " + (t1 - t0));
	}

}
