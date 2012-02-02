package wr3.model;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;

import wr3.Cell;
import wr3.Row;
import wr3.text.Template;
import wr3.util.Stringx;
import domain.Man;

/**
 * 输入：JPA Domain/Model/Form 类 输出：录入Form的Html
 * 
 * @author jamesqiu 2010-3-28
 */
public class Form {

	private Class<?> domainClass;
	private LinkedHashMap<String, Field> fieldsOrder = new LinkedHashMap<String, Field>();

	private Form() {
	}

	/**
	 * 使用Domain类初始化
	 * 
	 * @param domainClass
	 * @return
	 */
	public static Form create(Class<?> domainClass) {
		Form instance = new Form();
		instance.domainClass = domainClass;
		return instance;
	}

	/**
	 * 得到相应的html
	 * 
	 * @return
	 */
	public String html() {

		// wr3/model/Form.ftl
		Template template = Template.createClass(getClass());

		template.set("formName", domainClass.getName());
		template.set("action", "action");
		template.set("title", Annotationx.title(domainClass));
		Row fields = fields();
		template.set("fields", fields);

		return template.toString();
	}

	private Row fields() {

		initFieldsOrder();
		final Row row = new Row();
		
		for (Field field : fieldsOrder.values()) {
			if (field==null) continue;
			String html = html(field);
			row.add(new Cell(html));
		}

		return row;
	}

	private void initFieldsOrder() {

		String[] order = Annotationx.order(domainClass);

		// 如果没有设置@Order
		if (order == null)
			return;

		// 设置了@Order, 则先订好field name的位置
		for (String id : order) {
			fieldsOrder.put(id, null);
		}
		// 然后按照field name的位置放置field
		for (Field f : domainClass.getDeclaredFields()) {
			String fname = f.getName();
			if (!fieldsOrder.containsKey(fname)) continue;
			fieldsOrder.put(fname, f);
		}
	}

	private String html(Field f) {

		Class<?> clazz = f.getType();

		if (clazz == int.class) {
			return fieldInt(f);
		} else if (clazz == String.class) {
			return fieldString(f);
		} else {
			return fieldOther(f);
		}
	}

	private String fieldInt(Field f) {

		String format = "<td align=right>%s : </td>"
				+ "<td><input type=text name=\"%s\" title=\"数字\" /></td>";
		return Stringx.printf(format, Annotationx.title(f), f.getName());
	}

	private String fieldString(Field f) {

		String format = "<td align=right>%s : </td>"
				+ "<td><input type=text name=\"%s\" title=\"文字\" /></td>";
		return Stringx.printf(format, Annotationx.title(f), f.getName());
	}

	private String fieldOther(Field f) {

		String format = "<td align=right>%s : </td>"
				+ "<td><input type=text name=\"%s\" title=\"其他\" /></td>";
		return Stringx.printf(format, Annotationx.title(f), f.getName());
	}

	// ----------------- main() -----------------//
	public static void main(String[] args) {

		Form form = Form.create(Man.class);
		String html = form.html();
		System.out.println(html);
	}
}
