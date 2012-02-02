package test;

import wr3.util.Numberx;

public class ThreadTest {
/**
 * 
 * @param args
      var total = 1000
      def get = {
        Thread.sleep(scala.util.Random.nextInt(100))
        total
      }
      def cut(n: Int) = {
        val n0 = get
        Thread.sleep(scala.util.Random.nextInt(100))
        total = n0 - n
        println("%d-%d=%d" format (n0, n, total))
      }

 */
	private int total = 1000;
	
	synchronized int get() {
		try {
			Thread.sleep(Numberx.random());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return total;
	}
	
	synchronized void cut(int n) {

		int n0 = get();
		try {
			Thread.sleep(Numberx.random());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		total = n0 - n;
		System.out.printf("%d-%d=%d\n", n0, n, total);
	}
	
	public static void main(String[] args) {
		
		final ThreadTest o = new ThreadTest();
		for (int i = 0; i < 10; i++) {
			new Thread() {
				public void run() {
					o.cut(100);
				}
			}.start();
		}
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("Ê£ÏÂ£º" + o.get());
	}
}
