package mt;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import wr3.util.Stringx;

/**
 * <pre>
 * ָ��site��app��1��queue, ÿ��queeu��:
 * 1������������ͨ��Ϣ��messages����, 1����������������Ϣ��cmds����.
 * usage:
 * 	queue1 = Queue.create("app2@site");
 * 	mid = queue1.put(prio); // ����1����Ϣ, �õ�Ψһ��Ϣ��
 * 	ok = queue1.remove(id);	// ɾ��1����Ϣ
 * 
 * </pre>
 * @author jamesqiu 2009-5-30
 *
 */
public class Queue {

	private String site = "local";	// վ������
	private String app = "admin";	// Ӧ������
	
	/**
	 * ��Ϣ�������ڴ��еĴ洢, 
	 * keyΪ"��Ϣ���ȼ�+��Ϣ���к�", 
	 * valueΪ��Ϣ��Ӳ�̴洢���ļ���.
	 */
	@SuppressWarnings("unused")
	private Map<Long, String> messages = new TreeMap<Long, String>();
	/**
	 * 0��������Ϣ(�������ȷ���)���ڴ��еĴ洢
	 */
	@SuppressWarnings("unused")
	private Map<Long, String> cmds = new LinkedHashMap<Long, String>();
	
	@SuppressWarnings("unused")
	private int seq = 1;	// ��1��ʼ��������Ϣ���к�.

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
