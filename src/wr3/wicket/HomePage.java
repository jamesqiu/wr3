package wr3.wicket;

import java.awt.Color;
import java.awt.Graphics2D;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.extensions.markup.html.form.DateTextField;
import org.apache.wicket.extensions.markup.html.form.palette.Palette;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.DataGridView;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.PropertyPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.extensions.markup.html.tabs.TabbedPanel;
import org.apache.wicket.extensions.markup.html.tree.Tree;
import org.apache.wicket.extensions.markup.html.tree.table.ColumnLocation;
import org.apache.wicket.extensions.markup.html.tree.table.IColumn;
import org.apache.wicket.extensions.markup.html.tree.table.PropertyRenderableColumn;
import org.apache.wicket.extensions.markup.html.tree.table.PropertyTreeColumn;
import org.apache.wicket.extensions.markup.html.tree.table.TreeTable;
import org.apache.wicket.extensions.markup.html.tree.table.ColumnLocation.Alignment;
import org.apache.wicket.extensions.markup.html.tree.table.ColumnLocation.Unit;
import org.apache.wicket.extensions.wizard.StaticContentStep;
import org.apache.wicket.extensions.wizard.Wizard;
import org.apache.wicket.extensions.wizard.WizardModel;
import org.apache.wicket.extensions.yui.calendar.DatePicker;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.image.resource.RenderedDynamicImageResource;
import org.apache.wicket.markup.html.include.Include;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.DownloadLink;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.link.PopupSettings;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.list.PageableListView;
import org.apache.wicket.markup.html.navigation.paging.IPagingLabelProvider;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.GridView;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import wr3.util.Datetime;
import wr3.util.Numberx;
import static wr3.util.Datetime.date;

/**
 * Everybody's favorite example!
 * 
 * @author Jonathan Locke
 */
public class HomePage extends WebPage {
	
	private int count;
	/**
	 * Constructor
	 */
	@SuppressWarnings({ "serial", "unchecked" })
	public HomePage() {
		
		super();
		
		getApplication().getDebugSettings().setAjaxDebugModeEnabled(false);
		
		// --- div1
		WebMarkupContainer container = new WebMarkupContainer("div1");
		add(container);
		
		final Label label = new Label("message", new Model<String>() {
			public String getObject() {
				return "count:"+count;
			}
		});
		// 也可以用PropertyModel:
//		container.add(new Label("message", new PropertyModel(this, "count")));
		container.add(label);

		// --- link
		container.add(new ExternalLink("link2", ".", getClass().getSimpleName()));

		container.add(new DownloadLink("link3", new File("f:/u1.txt")));
		
		BookmarkablePageLink<?> blink = 
			new BookmarkablePageLink<AttributeModifier>("link4", LinkPage.class);
		container.add(blink);
		blink.add(new AttributeModifier("title", true, new Model<String>("新窗口")));
		blink.setPopupSettings(new PopupSettings().setHeight(200).setWidth(200));
		
		Link<?> link = new Link<Object>("link1") {
			@Override
			public void onClick() {
				count++;
			}
		}; 
		container.add(link);
		
		BookmarkablePageLink<?> userEditPage = 
			new BookmarkablePageLink<Object>("link5", UserEditPage.class);
		container.add(userEditPage);
		// ajax
		Link<?> link_ajax = new AjaxFallbackLink<Object>("link1_ajax") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				count++;
				if (target!=null) target.addComponent(label);
			}
		};
		label.setOutputMarkupId(true);
		container.add(link_ajax);

		// --- table
		final List<String> list1 = Arrays.asList(
			"hello world",
			"cn 中文.朱镕基",
			"last line");
		
		add(new ListView<String>("forums", list1) {
			@Override
			protected void populateItem(ListItem<String> item) {
				item.add(new Label("name", item.getModelObject()));
				item.add(new Label("description", ""
						+ System.currentTimeMillis()));
			}
		});
		
		// --- include
		add(new Include("include", "include.html"));
		
		// --- form
		Form<?> form = new Form<Object>("form");
		final TextField<String> textfield = 
			new TextField<String>("input1", new Model<String>(""));
		final TextField<String> result = 
			new TextField<String>("result", new Model<String>(""));
		Button button = new Button("button1") {
			public void onSubmit() {
				info("button click");
				String s = (String) textfield.getModelObject();
				result.setModelObject(s);
				textfield.setModelObject("");
			}
		};
		form.add(textfield);
		form.add(result);
		form.add(button);
		add(form);
		
		// --- image
		add(new Image("img", new RenderedDynamicImageResource(200, 50) {

			@Override
			protected boolean render(Graphics2D graphics) {
				graphics.setColor(Color.WHITE);
				graphics.fillRect(0, 0, 200, 50);
				graphics.setColor(Color.RED);
				graphics.drawString("cn中文,朱镕基", 10, 20);
				graphics.drawOval(25, 25, 150, 20);
				return true;
			}
			
		}));

		// --- date picker
		DateTextField field = new DateTextField("date1");
		field.add(new DatePicker());
		add(field);
		
		// --- customer compoment
		add(new MyComponent("mycomp1"));
		add(new MyComponent("mycomp2"));
		
		// ListView
		List<String> rows = Arrays.asList("james", "qh", "老邱");
		add(new ListView<String>("rows", rows) {
			@Override
			protected void populateItem(ListItem<String> item) {
				item.add(new Label("name", item.getModelObject()));
				item.add(new Label("age", "2"+item.getIndex()));
			}
		});
		
		// PageableListView
		List<String> rows2 = new ArrayList<String>();
		for (int i = 1; i <= 13; i++) {
			rows2.add("条目" + i);
		}
		PageableListView<?> listView = 
			new PageableListView<Object>("rows2", rows2, 3) {
			@Override
			protected void populateItem(ListItem<Object> item) {
				item.add(new Label("name2", item.getDefaultModelObjectAsString()));
				item.add(new Label("age2", "2"+item.getIndex()));
			}
		};
		add(listView);
		add(new PagingNavigator("navigator1", listView, new IPagingLabelProvider() {
			public String getPageLabel(int page) {
				return "[第"+page+"页]";
			}
		})); // 注意别用 PagingNavigation
		
		// RepeatingView
		RepeatingView repeatView = new RepeatingView("persons");
		for (int i = 0; i < 3; i++) {
			WebMarkupContainer item = new WebMarkupContainer(repeatView.newChildId());
			repeatView.add(item);
			item.add(new CheckBox("check", new Model<Boolean>()));
			item.add(new Label("name", new Model<String>("name" + i)));
			item.add(new Label("age", new Model<String>("2" + i)));
		}
		add(repeatView);
		
		// DataView
		List<User> users = Arrays.asList(
				new User("user1", date(1980,1,1)), 
				new User("user2", date(1990,2,28)), 
				new User("user3", date(2000,12,31)));
		DataView<Object> dataView = 
			new DataView<Object>("users", new ListDataProvider(users), 2) {
			@Override
			protected void populateItem(Item<Object> item) {
				User user = (User) item.getModelObject();
				item.add(new Label("name", user.name));
				item.add(new Label("birthday", date(user.birthday)));
			}
		};
		add(dataView);
		add(new PagingNavigator("navigator2", dataView)); // 注意别用 PagingNavigation
		
		// GridView
		List<Integer> list2 = Arrays.asList(1,3,5,6);
		GridView<?> gridView = 
			new GridView<Object>("grid", new ListDataProvider(list2)) {
			@Override
			protected void populateItem(Item<Object> item) {
				item.add(new Label("cell", "grid" + item.getIndex()));
			}
			@Override
			protected void populateEmptyItem(Item<Object> item) {
				item.add(new Label("cell", ""));
			}
		};
		gridView.setRows(2);
		gridView.setColumns(3);
		add(gridView);
		
		// DataGridView
		ICellPopulator<?> dgvColumn1 = new ICellPopulator<Object>() {
			int index = 0;
			public void populateItem(Item cellItem, String componentId,
					IModel rowModel) {
				cellItem.add(new Label(componentId, "sid:"+(index++)));
			}
			public void detach() {
			}
		};
		ICellPopulator<?>[] dgvColumns = new ICellPopulator[] {
			dgvColumn1,
			new PropertyPopulator<Object>("name"),
			new PropertyPopulator<Object>("birthday")
		};
		DataGridView<?> datagridView = 
			new DataGridView(
					"datagridView", 
					dgvColumns, 
					new ListDataProvider(users));
		datagridView.setRowsPerPage(2);
		add(datagridView);
		add(new PagingNavigator("navigator3", datagridView)); // 注意别用 PagingNavigation
				
		// DataTable
		org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn[] dtColumns = 
			new org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn[]{
			new PropertyColumn<Object>(new Model<String>("Name"), "name", "name"),
			new PropertyColumn<Object>(new Model<String>("Birthday"), "birthday", "birthday")
		};
		DataTable<?> dataTable = new DataTable<Object>("table", 
				dtColumns, new ListDataProvider(users), 2);
		add(dataTable);
		add(new PagingNavigator("navigator4", dataTable)); // 注意别用 PagingNavigation
		
		
		// StringResourceModel
		User user1 = new User();
		StringResourceModel model1 = new StringResourceModel("name", this, 
				new Model<User>(user1)); // 从JavaBean得到数据
		add(new Label("res_name", model1));
		HashMap<String, Object> user2 = new HashMap<String, Object>() {{
			put("name", "张三");
			put("birthday", Datetime.date());
		}};
		StringResourceModel model2 = new StringResourceModel("birthday", this, 
				new Model<HashMap<String, ?>>(user2)); // 从HashMap得到数据
		add(new Label("res_birthday", model2));
				
		// Tab
		ITab[] itabs = new ITab[]{
			new AbstractTab(new Model<String>("Tab 1")) {
				@Override
				public Panel getPanel(String panelId) {
					return new MyComponent(panelId);
				}
			},
			new AbstractTab(new Model<String>("Tab 2")) {
				@Override
				public Panel getPanel(String panelId) {
					return new MyComponent(panelId);
				}
			}			
		};
		TabbedPanel tabpanel = new TabbedPanel("tabpanel", Arrays.asList(itabs));
		add(tabpanel);
		
		// Palette
		Form<?> formPalette = new Form<Object>("formPalette");

		Palette<?> palette = new Palette<Object>("palette", 
				new Model(new ArrayList<Object>()),
				new Model((Serializable)users), 
				new ChoiceRenderer("birthday", "name"), 10, true);
		formPalette.add(palette);
		add(formPalette);
		
		// fragment
		class InlineFragment extends Fragment {
			@SuppressWarnings("deprecation")
			public InlineFragment(String id, String markupId) {
				super(id, markupId);
				add(new Label("label1", "Fragment内容1"));
				add(new Label("label2", "Fragment内容2"));
			}
		}
		add(new InlineFragment("useFragment", "fragment1"));
		
		// Tree
		DefaultMutableTreeNode treeroot = new DefaultMutableTreeNode("root");
		for (int i = 1; i <= 5; i++) {
			DefaultMutableTreeNode child1 = new DefaultMutableTreeNode("节点" + i);
			for (int j = 1; j <= 3; j++) {
				DefaultMutableTreeNode child2 = new DefaultMutableTreeNode("子节点" + (i*10+j));
				child1.add(child2);
			}
			treeroot.add(child1);
		}
		DefaultTreeModel treemodel = new DefaultTreeModel(treeroot);
		final Tree tree = new Tree("tree", treemodel);
		add(tree);
		add(new Link<Object>("expandTree") {
			@Override
			public void onClick() {
				tree.getTreeState().expandAll();
			}
		});
		add(new AjaxFallbackLink<Object>("collapseTree") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				if (target!=null) target.addComponent(tree);
				tree.getTreeState().collapseAll();
			}
		});
		tree.setOutputMarkupId(true);
		
		// TreeTable
		IColumn[] columns = new IColumn[]{
			new PropertyTreeColumn(new ColumnLocation(Alignment.LEFT, 8, Unit.EM), "列头1", ""),
			new PropertyRenderableColumn(new ColumnLocation(Alignment.LEFT, 8, Unit.EM), "列头2", "")
		};
		final TreeTable treeTable = new TreeTable("treeTable", treemodel, columns);
		add(treeTable);
		add(new Link<Object>("expandTreeTable") {
			@Override
			public void onClick() {
				treeTable.getTreeState().expandAll();
			}
		});
		add(new AjaxFallbackLink<Object>("collapseTreeTable") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				if (target!=null) target.addComponent(treeTable);
				treeTable.getTreeState().collapseAll();
			}
		});
		treeTable.getTreeState().setAllowSelectMultiple(true);
		treeTable.setOutputMarkupId(true);
		
		// Wizard, TODO: 还没搞明白如何在不同tab间传递数据
		WizardModel wizardModel = new WizardModel();
		Report report = new Report();
		wizardModel.add(new StaticContentStep("Step 1", "1111", "...", true));
		wizardModel.add(new WizardStepPage(report, "Step 3中文", "2222中文"));
		wizardModel.add(new WizardLastStep(report));
		Wizard wizard = new Wizard("wizard", wizardModel);
		add(wizard);
		
		// 联动下拉框
		List<String> firstName = Arrays.asList(
			"1","2","3"
		);
		final List<String> secondName = new ArrayList<String>();
		final DropDownChoice<?> firstNameChoice = 
			new DropDownChoice<Object>("firstName", new Model(), firstName);
		final DropDownChoice<?> secondNameChoice = 
			new DropDownChoice<Object>("secondName", new Model(), secondName);
		firstNameChoice.setOutputMarkupId(true);
		secondNameChoice.setOutputMarkupId(true);
		add(firstNameChoice);
		add(secondNameChoice);
		firstNameChoice.add(new AjaxFormComponentUpdatingBehavior("onChange") {
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				secondName.clear();
				int i1 = Numberx.toInt(""+firstNameChoice.getModelObject(), 10);
				for (int i = 1; i <= 5; i++) {
					int i2 = i1*10 + i;
					secondName.add(""+i2);
				}
				target.addComponent(secondNameChoice);
			}
		});
	}
}

class User implements Serializable {
	
	private static final long serialVersionUID = 1L;

	public User() {}
	
	public User(String name, Date birthday) {
		this.name = name;
		if (birthday == null) birthday = new Date();
		this.birthday = birthday;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Date getBirthday() {
		return birthday;
	}
	public void setBirthday(Date birthday) {
		this.birthday = birthday;
	}
	String name = "[User类.name]";
	Date birthday = new Date();
}

class Report implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<?> getParams() {
		return params;
	}
	public void setParams(List<?> params) {
		this.params = params;
	}
	private String name;
	private List<?> params = new ArrayList<Object>();
	
	public String toString() {
		return "报表："+name;
	}
}

class WizardLastStep extends StaticContentStep {
	
	private static final long serialVersionUID = 1L;

	public WizardLastStep(Report report) {
		super(true);
		setTitleModel(new Model<String>("Step last"));
		setSummaryModel(new Model<String>("最后一步"));
		setContentModel(new Model<Report>(report));
	}
}