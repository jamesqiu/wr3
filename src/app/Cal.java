package app;

import test.CalTable;
import wr3.text.Template;
import wr3.util.Datetime;
import wr3.web.Appx;
import wr3.web.Params;
import wr3.web.Render;

public class Cal {

	public Params params;
	
	public Render today() {
		
		Template t = Appx.view(this);
		
		int y = params.intValue("y");
		int m = params.intValue("m");
		if (y==-1) y = Datetime.year();
		if (m==-1) m = Datetime.month();
		
		t.set("today", Datetime.date());
//		String table = new DateTable(y, m).html(); 
		t.set("y", y);
		t.set("m", m);
		t.set("table", CalTable.create7123456(y, m).table());
//		t.set("table", CalTable.create1234567(y, m).table());
		return Render.html(t.toString());
	}
}
