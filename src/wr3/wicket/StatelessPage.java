package wr3.wicket;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.StatelessForm;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import wr3.util.Numberx;

/**
 * @author IBM
 */
public class StatelessPage extends WebPage {

	int i = 3;
	
	public StatelessPage() {
		
		final TextField<Integer> input = new TextField<Integer>(
				"input", new Model<Integer>(i));
		final Label result = new Label(
				"result", new PropertyModel<Integer>(this, "i"));
		

		StatelessForm<String> form1 = new StatelessForm<String>("form")	{
			private static final long serialVersionUID = 1L;
			@Override
			protected void onSubmit() {
				super.onSubmit();
				i = Numberx.toInt(input.getInput(), 0);
			}
		};
		
		add(form1);
		form1.add(input);
		form1.add(result);
	}
	
	public int getI() {
		return i*i;
	}
}

