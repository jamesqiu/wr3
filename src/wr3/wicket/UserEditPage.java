package wr3.wicket;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.wicket.extensions.markup.html.form.DateTextField;
import org.apache.wicket.extensions.yui.calendar.DatePicker;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.CheckBoxMultipleChoice;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.ListChoice;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.RadioChoice;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.validation.validator.StringValidator;

import wr3.util.Numberx;

/**
 * @author IBM
 * F:\data\WebDev\Wicket\Wicket开发指南-src代码\WEB-INF\classes\org\wicket\demo\\user
 * 
 */
public class UserEditPage extends WebPage {
	private static List<String> favorites = Arrays.asList(
		"电影", "阅读", "旅行");
	
	private static List<String> jobs = Arrays.asList(
		"演员", "老师", "学生", "记者");
	
	private static List<Integer> workYears = Arrays.asList(
		0,1,2,3,4,5,6,7,8,9);
	
	private static List<Boolean> marriages = Arrays.asList(
		true, false);
	
	@SuppressWarnings({ "serial", "unchecked" })
	public UserEditPage() {
		super();
		
		add(new FeedbackPanel("feedback"));
		
		add(new Form<Object>("form") {
			{
				// acount
				TextField<String> account = new TextField<String>("account", 
						new Model<String>("default"));
				account.add(StringValidator.lengthBetween(3, 6));
				account.setRequired(true);
				add(account);
				
				// name
				TextField<String> name = new TextField<String>("name", new Model<String>(){
					@Override
					public String getObject() {
						return "name:" + Numberx.random();
					}
				});
				add(name);
				
				// female
				add(new CheckBox("female", new Model<Boolean>()));
	
				// password
				PasswordTextField password = new PasswordTextField("password", new Model<String>("pass"));
				password.setRequired(false);
				add(password);
				
				// birthday
				DateTextField field = new DateTextField("birthday", new Model<Date>(new Date()));
				field.add(new DatePicker());
				add(field);
	
				// salary
				add(new TextField<Double>("salary", new Model<Double>(), Double.class));
				
				// job
				add(new ListChoice<String>("job", new Model<String>(), jobs));
				
				// workYears
				add(new DropDownChoice<Integer>("workYear", new Model<Integer>(), workYears));
				
				// marriages
				add(new RadioChoice<Boolean>("married", new Model<Boolean>(), marriages));
				
				// favorites
				add(new CheckBoxMultipleChoice<String>("favorites", new Model(), favorites));
	
				// image
				add(new FileUploadField("image", new Model<FileUpload>()));
			}
			@Override
			protected void onSubmit() {
				info("form.onSubmit()");				
			}
		});
		
	}
}

