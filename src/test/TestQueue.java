package test;

import java.util.concurrent.ConcurrentLinkedQueue;

import wr3.util.Numberx;

public class TestQueue extends Thread {
	
	int n;
	Que que;
	
	public TestQueue(int n, Que que) {
		this.n = n;
		this.que = que;
	}
	
	public void run() {
		// 该线程先sleep一个随机毫秒数；
		boolean isPut = (n%2==0);
		try {
			long L = Numberx.random(100);
			sleep(L);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// 是偶数则放入队列，奇数则不放入，做取操作
		if (isPut) {
			que.put(n);
			System.out.println("+" + n);
		} else {
			int i = que.get();
			System.err.println("-" + i);
		}		
	}
	
	//----------------- main() -----------------//
	public static void main(String[] args) throws InterruptedException {

		// 开100个线程，50个往里放偶数，50个往外取
		Que que = new Que();
		for (int i = 0; i < 100; i++) {
			TestQueue t = new TestQueue(i, que);
			t.start();
		}
	}	
}

class Que {
	
	private ConcurrentLinkedQueue<Integer> que = 
		new ConcurrentLinkedQueue<Integer>();

	public synchronized void put(int i) {
		
		que.add(i);
		notify();
	}
	
	public synchronized int get() {
		
		// 可能取的时候为空
		while (que.size()==0) {
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return que.poll();
	}
}