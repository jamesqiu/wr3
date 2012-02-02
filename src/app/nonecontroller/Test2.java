package app.nonecontroller;

import wr3.Table;
import wr3.db.DbServer;
import wr3.web.Render;

public class Test2 {

	public Render hello() {
		Table table = DbServer.create().query("select top 10 * from cust");
		return Render.html(table);
	}
	
	public Render location() {
		String html = "这是app子目录下的非Controller类的方法。";
		return Render.html(html);
	}
}
