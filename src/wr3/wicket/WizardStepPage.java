package wr3.wicket;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.extensions.wizard.WizardStep;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.PropertyModel;

/**
 * @author IBM
 */
public class WizardStepPage extends WizardStep {

	private static final long serialVersionUID = 1L;
	
	private static List<String> reports = new ArrayList<String>();
	static {
		for (int i = 0; i < 7; i++) 
			reports.add("report " + i);
	}
	
	public WizardStepPage(Report report, String title, String content) {
		super(title, content);
		final RadioGroup<String> radioGroup = new RadioGroup<String>("group", 
				new PropertyModel<String>(report, "name"));
		add(radioGroup);
		radioGroup.setRequired(true);
		radioGroup.add(new ListView<String>("reports", reports) {
			private static final long serialVersionUID = 1L;
			@Override
			protected void populateItem(ListItem<String> item) {
				item.add(new Radio<String>("selected", item.getModel()));
				item.add(new Label("name", item.getDefaultModel()));
			}
		});
	}
}

