package app;

import wr3.model.Form;
import wr3.web.Params;
import wr3.web.Render;
import domain.Man;

public class Crud {

	Params params;
	
	public Render index() {
		
		Class<?> clazz = Man.class;
		
		String content = Form.create(clazz).html();
		
		return Render.body(content.toString());
	}	
}
