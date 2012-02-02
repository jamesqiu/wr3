package wr3.wicket;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

import wr3.util.Numberx;

public class MyComponent extends Panel {

	private static final long serialVersionUID = 1L;
	
	private Label receivedMailLabel;
	private Label deletedMailLabel;
	private Label innerMailLabel;
	
	public MyComponent(String id) {
		super(id);
		
		int r = Numberx.random();
		int d = Numberx.random();
		int i = Numberx.random();		
		
		receivedMailLabel = new Label("receivedMail", new Model<String>("received "+r));
		deletedMailLabel = new Label("deletedMail", new Model<String>("deleted "+d));
		innerMailLabel = new Label("innerMail", new Model<String>("inner "+i));
		// ±ê×¢ÑÕÉ«
		mark(receivedMailLabel, r);
		mark(deletedMailLabel, d);
		mark(innerMailLabel, i);
		
		add(receivedMailLabel);
		add(deletedMailLabel);
		add(innerMailLabel);
	}
	
	void mark(Label label, int n) {
		if (n < 60) label.add(new AttributeModifier("class", true, new Model<String>("red")));
		if (n >= 90) label.add(new AttributeModifier("class", true, new Model<String>("blue")));
		
	}
	
}
