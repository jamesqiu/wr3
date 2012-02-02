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
 * 使用Memory-Mapped File实现Java进程间共享内存读写.
 * 优点: 不用做同步多线程没有问题.
 * 缺点: 不能触发, 必须程序读取才知道是否改变.
 * </pre>
 * @author jamesqiu 2009-5-30
 *
 */
public class IPC {

	File file = new File("filename1");
	
	/**
	 * 读出int值
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
	 * 写入一个int值
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
	 * int自增
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
	 * 100个线程同时读写不用加同步机制没有问题.
	 */
	void con() {
		for (int i = 1; i <= 100; i++) {
			new Concurrent("thread " + i).start();			
		}
	}
	
	/**
	 * 执行10次seq()的线程
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
			System.out.println("共享内存测试, usage: \n" +
					"    IPC -r    读共享内存的int\n" +
					"    IPC -w n  写1个int到共享内存\n" +
					"    IPC s     共享内存的int自增\n" +
					"    IPC c     测试多个线程并发int自增\n" +
					"    IPC t     测试5000次写入的时间");
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
