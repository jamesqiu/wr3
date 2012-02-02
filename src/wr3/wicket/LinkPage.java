package wr3.wicket;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.MultiLineLabel;

public class LinkPage extends WebPage {

	public LinkPage() {
		add(new MultiLineLabel("msg", "111111111 \n 22222222 \n 33333333 \n 4444"));
	}
}
