package mt;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import wr3.util.Stringx;

/**
 * <pre>
 * 指定site和app的1个queue, 每个queeu有:
 * 1个包含多条普通消息的messages队列, 1个包含优先命令消息的cmds队列.
 * usage:
 * 	queue1 = Queue.create("app2@site");
 * 	mid = queue1.put(prio); // 增加1条消息, 得到唯一消息名
 * 	ok = queue1.remove(id);	// 删除1条消息
 * 
 * </pre>
 * @author jamesqiu 2009-5-30
 *
 */
public class Queue {

	private String site = "local";	// 站点名称
	private String app = "admin";	// 应用名称
	
	/**
	 * 消息队列在内存中的存储, 
	 * key为"消息优先级+消息序列号", 
	 * value为消息在硬盘存储的文件名.
	 */
	@SuppressWarnings("unused")
	private Map<Long, String> messages = new TreeMap<Long, String>();
	/**
	 * 0级命令消息(单独优先发送)在内存中的存储
	 */
	@SuppressWarnings("unused")
	private Map<Long, String> cmds = new LinkedHashMap<Long, String>();
	
	@SuppressWarnings("unused")
	private int seq = 1;	// 从1开始递增的消息序列号.

	public Queue(String site, String app) {		
		app(app);
		site(site);
	}
	
	public Queue(String app_site) {
		
		String app = Stringx.left(app_site, "@");
		String site = Stringx.right(app_site, "@"); 
		
		app(app);
		site(site);
	}
	
	public String site() {
		return this.site;
	}
	
	public void site(String site) {
		if (!Stringx.nullity(site)) this.site = site;
	}

	public String app() {
		return this.app;
	}
	
	public void app(String app) {
		if (!Stringx.nullity(app)) this.app = app;
	}
	
	public String put(int prio, Object message) {
		
		String filename = "";
		write(filename, message);
		return "";
	}
	
	void write(String filename, Object message) {
		System.out.println(filename + " (" + message + ")");
	}
	
	// ---------------------- main() ----------------------
	public static void main(String[] args) {
		
		Queue q1 = new Queue("site1", "app1");
		Queue q2 = new Queue("app2@site1");
		for (int i = 0; i < 10; i++) {
			q1.put(1, "111111111111111111111111111111111: " + i);			
			q2.put(2, "222222222222222222222222222222222: " + i);			
		}
		System.out.println(q1);
		System.out.println("");
		System.out.println(q2);
	}
	
}
