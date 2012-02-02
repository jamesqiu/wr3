package app;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import wr3.Cell;
import wr3.util.Datetime;
import wr3.util.Stringx;
import wr3.web.Render;

import static wr3.web.Render.*;
import static wr3.web.Appx.*;

public class Actions {

	/**
	 * 通过javassist得到方法的参数名称（需要带debug编译）
	 */
	public Render javassist() {
		Map<String, String> map = actionArgs("Actions", "m4");
		return text(map.toString());
	}	
	
	public String m2(String name, int age, Date birth, long money, Cell cell) {
		name = Stringx.s(name, "anonymous");
		
		return "name=" + name + 
			", age=" + age + 
			", birth=" + Datetime.date(birth) + 
			", money=" + money + 
			", cell=" + cell
		;
	}
	
	public String m3(int d2, double i3, String s3) { return ""; }
	
	void m4(int i0, long l0, double d0, Date d1, String s1, Cell c1, Calendar c2) {  }

}
