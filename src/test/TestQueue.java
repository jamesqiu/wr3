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
		// ���߳���sleepһ�������������
		boolean isPut = (n%2==0);
		try {
			long L = Numberx.random(100);
			sleep(L);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// ��ż���������У������򲻷��룬��ȡ����
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

		// ��100���̣߳�50�������ż����50������ȡ
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
		
		// ����ȡ��ʱ��Ϊ��
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