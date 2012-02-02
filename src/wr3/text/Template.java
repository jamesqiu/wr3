package wr3.text;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import wr3.Cell;
import wr3.Row;
import wr3.Table;
import wr3.util.Charsetx;
import wr3.util.Classx;
import wr3.util.Filex;
import wr3.util.Stringx;
import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.FileTemplateLoader;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;

/**
 * <pre>
 * ͨ��FreeMarker 2.3.15ģ������String���.
 * usage:
 *   t = Template.create("test.ftl");
 *   t.set("id", "001");
 *   t.set("table1", new Table(10,20));
 *   t.toString();
 *   
 * ע: Template�ı��ļ��ı���ͳһ��ΪUTF-8, �� {@link #process()}  
 * </pre>
 * 
 * @author jamesqiu 2009-1-15
 */
public class Template {

	/**
	 * template file path.
	 */
//	private String filename;
	/**
	 * template configuration
	 */
	private Configuration config;
	/**
	 * template from file/class resource/string
	 */
	private freemarker.template.Template template;
	/**
	 * template root 
	 */
	private Map<String, Object> root;
	/**
	 * parse only when root change
	 */
	private boolean needProcess = true;
	/**
	 * 
	 */
	private String result = "";
	
	private Template() {
		config = new Configuration();
	}
	
	/**
	 * ʹ���ļ�Ŀ¼װ����
	 * @param filename
	 * @return
	 */
	public static Template create(String filename) {
		Template t = new Template();
		t.setFileLoader(filename);
		return t;
	}
	
	/**
	 * <pre>
	 * ʹ����·����ģ�����������ָ�����ȱʡģ�壬
	 * ����: createThis(wr3.model.Form.class) ���� "/wr3/model/Form.ftl"
	 * </pre>
	 * @param cls
	 * @return
	 */
	public static Template createClass(Class<?> cls) {
		
		String clsTemplate = Classx.filepath(cls, ".ftl");
		return create(cls, clsTemplate);  
	}
	
	/**
	 * ʹ����·����ģ�������.
	 * �磺Tempalte.create(getClass(), getClass().getName()+".ftl");
	 * @param cls һ���� getClass()
	 * @param name ģ���ļ�����������"/"��ͷ���磺"wr3/table/FormTable.ftl"
	 * 			ע�⣺����ֻд"FormTable.ftl"
	 * @return
	 */
	public static Template create(Class<?> cls, String name) {
		
		Template t = new Template();
		t.setClassLoader(cls, name);
		return t; 
	}
	
	/** 
	 * �õ���ǰ������·���µ�ģ���������
	 * @param o һ����this;
	 * @param name ģ���ļ�����������"/"��ͷ���磺"wr3/table/FormTable.ftl"
	 * 			ע�⣺����ֻд"FormTable.ftl"
	 * @return
	 * @see #create(Class, String)
	 */
	public static Template create(Object o, String name) {
		
		if (o==null) return new Template();
		
		return create(o.getClass(), name);
	}
	
	public static Template createByString(String templateString) {

		Template t = new Template();
		t.setStringLoader(templateString);
		return t; 
	}
	
	/**
	 * ���ļ�ϵͳװ��ģ�壬
	 * ע�⣺ģ���ļ�������utf-8���������ȷ�����ġ�
	 * @param filename �ļ����·�����߾���·����
	 * @throws IOException 
	 */
	private void setFileLoader(String filename) {

//		System.out.println("Template.setFileLoader(): filename=" + filename);
		if (!Filex.isFile(filename)) {
			new FileNotFoundException("error template file: [" +
					Filex.fullpath(filename) + "]").printStackTrace();
			return;
		}
		
		File dir = new File(filename).getParentFile();
		try {
			 config.setTemplateLoader(new FileTemplateLoader(dir));
			 this.template = config.getTemplate(
					 new File(filename).getName(), Charsetx.UTF);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}
	
	/**
	 * ������Դ�ļ���װ��ģ�塣
	 * @param cls
	 * @param name ����·����������"/"��ͷ���磺"wr3/table/FormTable.ftl"
	 */
	private void setClassLoader(Class<?> cls, String name) {

		config.setTemplateLoader(new ClassTemplateLoader(cls, "/"));
		try {
			this.template = config.getTemplate(name, Charsetx.UTF);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}
	
	/**
	 * ֱ����ģ���ַ���װ�ء�
	 * @param templateString
	 */
	private void setStringLoader(String templateString) {
		
		StringTemplateLoader stringLoader = new StringTemplateLoader();
		String name = Stringx.uid(); // ʹ��Ψһ������֤�̰߳�ȫ��
		stringLoader.putTemplate(name, templateString);
		config.setTemplateLoader(stringLoader);
		try {
			this.template = config.getTemplate(name);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}
	
	/**
	 * ͨ�� Cell / Row / Table / Object.toString() ���ñ���ֵ��
	 * @param key
	 * @param value
	 * @return
	 */
	public Template set(String key, Object value) {
		
		if (Stringx.nullity(key) || config==null || template==null) return this;

		if (root==null) root = new HashMap<String, Object>();
		
		if (value==null) {
			root.put(key, null);
		} else if (value instanceof Cell) {
			setCell(key, (Cell)value);
		} else if (value instanceof Row) {
			setRow(key, (Row)value);
		} else if (value instanceof Table) {
			setTable(key, (Table)value);
		} else {
			root.put(key, value.toString());
		}
		needProcess = true;
		return this;
	}
	
	private void setCell(String key, Cell cell) {
		root.put(key, cell.value());
	}
	
	private void setRow(String key, Row row) {
		root.put(key, row.asList());
	}
	
	@SuppressWarnings("unchecked")
	private void setTable(String key, Table table) {
		Map map = new HashMap();
		map.put("head", table.head().asList());
		List<List<Object>> rows = new ArrayList<List<Object>>();
		for (int i = 0; i < table.rows(); i++) {
			rows.add(table.row(i).asList());
		}	
		map.put("rows", rows);
		root.put(key, map);
	}
	
	@Override
	public String toString() {
		try {
			process();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TemplateException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * <pre>
	 * ��ӡ��template�Ľ����
	 * usage��
	 *   Template.create("a.ftl")
	 *     .set(...).print()
	 *     .set(...).toString();
	 * </pre>
	 * @return
	 */
	public Template print() {
		
		System.out.println(toString());
		return this;
	}
	
	/**
	 * ���״κ� {@link #set(String, Object)} �ı�����ʱ����ģ���������.
	 * @throws IOException
	 * @throws TemplateException
	 */
	private void process() throws IOException, TemplateException {
	
		if (!needProcess || config==null || template==null) return;
		
//		freemarker.template.Template template = config.getTemplate(
//				new File(filename).getName(), 
//				"UTF-8");
		
		// ��û����set(), �ڴ˳�ʼ��root���������toString()��null��
		if (root==null) root = new HashMap<String, Object>();
		
		addCustomerTag();
		
		StringWriter out = new StringWriter();
		template.process(root, out);
		out.flush();  
		result = out.toString();
		
		needProcess = false;
	}	

	/**
	 * �����Զ���tag
	 */
	private void addCustomerTag() {
		// <@include url="http://localhost/a.jsp" enc="GBK" />
		root.put("include", new TemplateInclude());
	}
	
	// ---------------------- main() ----------------------
	public static void main(String[] args) {
		/*
		long t0 = System.currentTimeMillis();
		int n = 1;
		for (int i = 0; i < n; i++) {			
			Template t = Template.createByString("${HEAD_HTML}\r\n" + 
					"<#assign n=head?size - 1><#-- n���±� --><#list 0..n as i>�ֶ�${head[i]}: ֵ${body[i]}\r\n" + 
					"</#list>\r\n" + 
					"ͷ��\r\n" + 
					"<#list table.head as th>${th} </#list>\r\n" + 
					"")
					.set("HEAD_HTML", "ͷͷͷ")
					.set("table", new Table(3,1))
					.set("head", new Row(3))
					.set("body", new Row(3));
			t.print();			
		}
		
		for (int i = 0; i < n; i++) {
//			Template t2 = Template.create(String.class, "wr3/table/FormTable.ftl")
			Template t2 = Template.create("classes/wr3/table/FormTable.ftl")
				.set("HEAD_HTML", "ͷͷͷ")
				.set("table", new Table(3,1))
				.set("head", new Row(3))
				.set("body", new Row(3));
			t2.print();						
		}
		long t1 = System.currentTimeMillis();
		System.out.println("t1-t0=" + (t1 - t0));
		//*/
	}
}
