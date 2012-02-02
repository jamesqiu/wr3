package wr3.model;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;

import wr3.Cell;
import wr3.Row;
import wr3.text.Template;
import wr3.util.Stringx;
import domain.Man;

/**
 * ���룺JPA Domain/Model/Form �� �����¼��Form��Html
 * 
 * @author jamesqiu 2010-3-28
 */
public class Form {

	private Class<?> domainClass;
	private LinkedHashMap<String, Field> fieldsOrder = new LinkedHashMap<String, Field>();

	private Form() {
	}

	/**
	 * ʹ��Domain���ʼ��
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
	 * �õ���Ӧ��html
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

		// ���û������@Order
		if (order == null)
			return;

		// ������@Order, ���ȶ���field name��λ��
		for (String id : order) {
			fieldsOrder.put(id, null);
		}
		// Ȼ����field name��λ�÷���field
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
				+ "<td><input type=text name=\"%s\" title=\"����\" /></td>";
		return Stringx.printf(format, Annotationx.title(f), f.getName());
	}

	private String fieldString(Field f) {

		String format = "<td align=right>%s : </td>"
				+ "<td><input type=text name=\"%s\" title=\"����\" /></td>";
		return Stringx.printf(format, Annotationx.title(f), f.getName());
	}

	private String fieldOther(Field f) {

		String format = "<td align=right>%s : </td>"
				+ "<td><input type=text name=\"%s\" title=\"����\" /></td>";
		return Stringx.printf(format, Annotationx.title(f), f.getName());
	}

	// ----------------- main() -----------------//
	public static void main(String[] args) {

		Form form = Form.create(Man.class);
		String html = form.html();
		System.out.println(html);
	}
}
