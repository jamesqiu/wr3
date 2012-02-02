package test;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

import wr3.util.Numberx;

/**
 * <pre>
 * ʹ��Memory-Mapped Fileʵ��Java���̼乲���ڴ��д.
 * �ŵ�: ������ͬ�����߳�û������.
 * ȱ��: ���ܴ���, ��������ȡ��֪���Ƿ�ı�.
 * </pre>
 * @author jamesqiu 2009-5-30
 *
 */
public class IPC {

	File file = new File("filename1");
	
	/**
	 * ����intֵ
	 * @return
	 * @throws IOException
	 */
	int read() throws IOException {
		
        FileChannel channel = new RandomAccessFile(file, "r").getChannel();
        MapMode mode = FileChannel.MapMode.READ_ONLY; 
        int size = (int)channel.size();
        MappedByteBuffer buf = channel.map(mode, 0, size);
        int n = buf.getInt();
        channel.close();
        return n;
	}
	
	/**
	 * д��һ��intֵ
	 * @param n
	 * @throws IOException
	 */
	void write(int n) throws IOException {
		
        FileChannel channel = new RandomAccessFile(file, "rw").getChannel();
        MapMode mode = FileChannel.MapMode.READ_WRITE; 
        int size = 4;
        MappedByteBuffer buf = channel.map(mode, 0, size);
        buf.putInt(0, n);
        buf.force();
        channel.close();
	}
	
	/**
	 * int����
	 * @return
	 * @throws IOException
	 */
	int seq() throws IOException {
		
        FileChannel channel = new RandomAccessFile(file, "rw").getChannel();
        MapMode mode = FileChannel.MapMode.READ_WRITE; 
        int size = (int)channel.size();
        MappedByteBuffer buf = channel.map(mode, 0, size);
        int n = buf.getInt();
        buf.putInt(0, ++n);
        buf.force();
        channel.close();
        return n;
	}
	
	/**
	 * 100���߳�ͬʱ��д���ü�ͬ������û������.
	 */
	void con() {
		for (int i = 1; i <= 100; i++) {
			new Concurrent("thread " + i).start();			
		}
	}
	
	/**
	 * ִ��10��seq()���߳�
	 * @author jamesqiu 2009-5-30
	 *
	 */
	class Concurrent extends Thread {
		
		String name;
		
		public Concurrent(String name) {
			this.name = name;
		}
		
		public void run() {
			IPC ipc = new IPC();
			for (int i = 0; i < 10; i++) {
				try {
					int n = ipc.seq();
					System.out.println(name + ", n=" + n);
				} catch (IOException e) {
					e.printStackTrace();
				}
				try {
					sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}				
			}
		}
	}
	
	// ---------------------- main() ----------------------
	public static void main(String[] args) throws IOException {
		
		if (args.length == 0) {
			System.out.println("�����ڴ����, usage: \n" +
					"    IPC -r    �������ڴ��int\n" +
					"    IPC -w n  д1��int�������ڴ�\n" +
					"    IPC s     �����ڴ��int����\n" +
					"    IPC c     ���Զ���̲߳���int����\n" +
					"    IPC t     ����5000��д���ʱ��");
			return;
		}
		
		IPC ipc = new IPC();
		
		if (args[0].equals("-r")) {
			int n = ipc.read();
			System.out.println(n);
		} else if (args[0].equals("-w")) {
			int n = Numberx.toInt(args[1], 10);
			ipc.write(n);			
		} else if (args[0].equals("s")) {
			for (int i = 0; i < 100; i++) {
				int n = ipc.seq();
				System.out.println(n);
			}
		} else if (args[0].equals("c")) {
			ipc.con();
		} else if (args[0].equals("t")) {
			long t0 = System.currentTimeMillis();
			
			for (int i = 1; i <= 5000; i++) {
				ipc.write(i);
			}
			int n = ipc.read();
			System.out.println(n);
			
			long t1 = System.currentTimeMillis();
			System.out.println("(t1-t0)=" + (t1 - t0));
		}
	}
}
