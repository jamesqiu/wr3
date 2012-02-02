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
 * 通过FreeMarker 2.3.15模板生成String输出.
 * usage:
 *   t = Template.create("test.ftl");
 *   t.set("id", "001");
 *   t.set("table1", new Table(10,20));
 *   t.toString();
 *   
 * 注: Template文本文件的编码统一设为UTF-8, 见 {@link #process()}  
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
	 * 使用文件目录装载器
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
	 * 使用类路径的模板加载器加载指定类的缺省模板，
	 * 例如: createThis(wr3.model.Form.class) 加载 "/wr3/model/Form.ftl"
	 * </pre>
	 * @param cls
	 * @return
	 */
	public static Template createClass(Class<?> cls) {
		
		String clsTemplate = Classx.filepath(cls, ".ftl");
		return create(cls, clsTemplate);  
	}
	
	/**
	 * 使用类路径的模版加载器.
	 * 如：Tempalte.create(getClass(), getClass().getName()+".ftl");
	 * @param cls 一般用 getClass()
	 * @param name 模板文件名，不必以"/"开头，如："wr3/table/FormTable.ftl"
	 * 			注意：不能只写"FormTable.ftl"
	 * @return
	 */
	public static Template create(Class<?> cls, String name) {
		
		Template t = new Template();
		t.setClassLoader(cls, name);
		return t; 
	}
	
	/** 
	 * 得到当前对象类路径下的模板加载器。
	 * @param o 一般用this;
	 * @param name 模板文件名，不必以"/"开头，如："wr3/table/FormTable.ftl"
	 * 			注意：不能只写"FormTable.ftl"
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
	 * 从文件系统装载模板，
	 * 注意：模板文件必须是utf-8编码才能正确读中文。
	 * @param filename 文件相对路径或者绝对路径。
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
	 * 从类资源文件中装载模板。
	 * @param cls
	 * @param name 绝对路径，不必以"/"开头，如："wr3/table/FormTable.ftl"
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
	 * 直接用模板字符串装载。
	 * @param templateString
	 */
	private void setStringLoader(String templateString) {
		
		StringTemplateLoader stringLoader = new StringTemplateLoader();
		String name = Stringx.uid(); // 使用唯一名，保证线程安全。
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
	 * 通过 Cell / Row / Table / Object.toString() 设置变量值。
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
	 * 打印本template的结果，
	 * usage：
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
	 * 在首次和 {@link #set(String, Object)} 改变内容时进行模板解析处理.
	 * @throws IOException
	 * @throws TemplateException
	 */
	private void process() throws IOException, TemplateException {
	
		if (!needProcess || config==null || template==null) return;
		
//		freemarker.template.Template template = config.getTemplate(
//				new File(filename).getName(), 
//				"UTF-8");
		
		// 若没调用set(), 在此初始化root，避免调用toString()出null错。
		if (root==null) root = new HashMap<String, Object>();
		
		addCustomerTag();
		
		StringWriter out = new StringWriter();
		template.process(root, out);
		out.flush();  
		result = out.toString();
		
		needProcess = false;
	}	

	/**
	 * 增加自定义tag
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
					"<#assign n=head?size - 1><#-- n：下标 --><#list 0..n as i>字段${head[i]}: 值${body[i]}\r\n" + 
					"</#list>\r\n" + 
					"头：\r\n" + 
					"<#list table.head as th>${th} </#list>\r\n" + 
					"")
					.set("HEAD_HTML", "头头头")
					.set("table", new Table(3,1))
					.set("head", new Row(3))
					.set("body", new Row(3));
			t.print();			
		}
		
		for (int i = 0; i < n; i++) {
//			Template t2 = Template.create(String.class, "wr3/table/FormTable.ftl")
			Template t2 = Template.create("classes/wr3/table/FormTable.ftl")
				.set("HEAD_HTML", "头头头")
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
