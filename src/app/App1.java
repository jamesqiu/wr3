package app;

import wr3.Cell;
import wr3.web.Params;
import wr3.web.Render;
import wr3.web.Session;

public class App1 {

	/**
	 * 定义后可取得所有的request变量
	 */
	public Params params;	
	/**
	 * 定义后可取得所有的request变量
	 */
	public Session session;
		
	public String index() {
	
		String s = "this is index页面cn中文. params.k1=" + params.value("k1");
		return s;
	}
	
	public int age() {
		
		return 30;
	}
	
	public String name() {
		
		return "<h1>你好cn中文</h1>";
	}
	
	public java.util.Date date() {
		return new java.util.Date();
	}
	
	public Object table() {
		wr3.Table table = new wr3.Table(3, 5);
		table.cell(0,0,new Cell("cn中文"));
		return Render.html(table);
	}
	
	public String session() {
		
		System.out.println("session before set():" + session);
		session.set("k1", "cn中文");
		System.out.println("session after set():" + session);
		session.close();
		System.out.println("session after close():" + session);
		return "hello world";
	}
	
	public String hello1() {
		return "call hello: " + hello();
	}
	
	private String hello() {
		return new TestTable().test();
	}
}
