package wr3.util;

import java.util.ArrayList;
import java.util.List;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;
import com.jacob.com.Variant;

/**
 * 使用jacob进行Excel的读写
 * 本class的包装目前主要用来读取表格及表格之前的文字
 * @author jamesqiu 2009-4-29
 */
public class Word {

	ActiveXComponent word = new ActiveXComponent("Word.Application");
	Dispatch doc;	// 当前文档
	Dispatch table; // 当前使用的Table
	Dispatch selection; // 当前选中的区域

	boolean readonly = true; // 是否以只读方式打开

	/**
	 * 打开Excel文件
	 * @param filename
	 * @param visible
	 */
	public void open(String filename, boolean visible) {

		word.setProperty("Visible", visible);
		Dispatch docs = word.getProperty("Documents").toDispatch();
		doc = call(docs, "Open", filename, new Variant(false), new Variant(readonly));
		selection = get(word, "Selection"); // 从文章开头
	}

	Dispatch tables;

	/**
	 * 初始化doc中所有的tables
	 * @return tables的数量
	 * @throws Exception
	 */
	public int tables() throws Exception {

		tables = get(doc, "Tables");
		int n = getInt(tables, "Count");
		return n;
	}

	/**
	 * 激活第i个table
	 * @param i 遵循vb的规范，从1开始
	 * @return 所有的表格数目
	 */
	public Word table(int i) throws Exception {

		table = call(tables, "Item",  new Variant(i));
		// 把光标放在整个表格之前，便于之后找表格之前的字符串（如表名）
		call(table, "Select");
		call(selection, "MoveLeft"); // 光标移动到当前选择之前
		return this;
	}

	public int rows() throws Exception {
		Dispatch rows = get(table, "Rows");
		return getInt(rows, "Count");
	}

	public int cols() throws Exception {
		Dispatch rows = get(table, "Columns");
		return getInt(rows, "Count");
	}

	public String cell(int row, int col) throws Exception {
		Dispatch cell = call(table, "Cell", new Variant(row), new Variant(col));
		Dispatch range = get(cell, "Range");
		String s = getString(range, "Text");
		s =  s.substring(0, s.length()-2);// 去掉最后的回车和换行
		return s;
	}

	private boolean find(String s, boolean forward) {

		Dispatch find = Dispatch.call(selection, "Find").toDispatch();
		Dispatch.put(find, "Text", s);
		Dispatch.put(find, "Forward", forward ? "True" : "False"); // true-向下搜索, false-往上搜索,
		boolean found = Dispatch.call(find, "Execute").getBoolean();
		return found;
	}

	/**
	 * <pre>
	 * 用于查找表名称。
	 * 往回查找一行中s字符串之后至回车为止的字符串，例如:
	 * 在“表结构：Table1”中 findBack("表结构：")，得到"Table1"
	 * </pre>
	 * @param s 要查找的字符串
	 * @return
	 * @throws Exception
	 */
	public String findTitle(String s) throws Exception {

		boolean found = find(s, false);
		if (!found) return "Word.findTitle() not found.";

		// 选中s
		call(selection, "MoveRight"); // 光标挪到s右边
		Dispatch.call(selection, "MoveEndUntil", "\r"); // 选中光标到回车间的字符串
		String rt = getString(selection, "Text");
		call(selection, "MoveRight");
		return rt;
	}

	/**
	 *
	 * @param s 要搜索的字符串（“字段描述”）
	 * @param n 字段个数
	 * @return
	 * @throws Exception
	 */
	public List<String> findComment(String s, int n) throws Exception {

		boolean found = find(s, true);
		List<String> rt = new ArrayList<String>();
		if (!found) return rt;

		call(selection, "MoveLeft");
		call(selection, "MoveDown");

		for (int i = 1; i < n; i++) {
			Dispatch.call(selection, "MoveEndUntil", "\r");
			rt.add(getString(selection, "Text"));
			call(selection, "MoveRight");
			call(selection, "MoveRight");
		}

		return rt;
	}

	/**
	 * 保存、关闭Word文件
	 * @param f
	 */
	public void close() {
		boolean f = false;
//		call(doc, "Save");
		Dispatch.call(doc, "Close", new Variant(f));
		word.invoke("Quit", new Variant[] {});
	}

	/**
	 * 执行ActiveX方法(无参数)
	 * @param o 执行方法的ActiveX对象
	 * @param name 方法名
	 */
	private void call(Dispatch o, String name) {
		Dispatch.call(o, name);
	}

	/**
	 * 执行ActiveX方法(带参数)
	 * @param o 执行方法的ActiveX对象
	 * @param name 方法名
	 * @param args 参数
	 * @return
	 */
	private Dispatch call(Dispatch o, String name, Object ... args) {
		return invoke(o, name, Dispatch.Method, args);
	}

	/**
	 * 得到属性(无参数)
	 * @param o ActiveX对象
	 * @param name 属性名称
	 * @return
	 */
	private Dispatch get(Dispatch o, String name) {
		return Dispatch.get(o, name).toDispatch();
	}

	/**
	 * 得到属性的int值
	 * @param o
	 * @param name
	 * @return
	 */
	private int getInt(Dispatch o, String name) {
		return Dispatch.get(o, name).getInt();
	}

	private String getString(Dispatch o, String name) {
		return Dispatch.get(o, name).getString();
	}

	private Dispatch invoke(Dispatch o, String name, int wFlags, Object ... args) {
		Object[] argsArray = new Object[args.length];
		for (int i = 0; i < args.length; i++) {
			argsArray[i] = args[i];
		}
		return Dispatch.invoke(
				o,
				name,
				wFlags,
				argsArray,
				new int[1])
			.toDispatch();
	}

	/**
	 * 延时
	 * @param ms 毫秒, 1000代表1秒
	 */
	public static void sleep(int ms) {
		System.out.println("sleep "+ (ms/1000) +" second.");
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param filePath
	 */
	public static void toHtml(String wordPath, String htmlPath) {
		ActiveXComponent activeApp = new ActiveXComponent("Word.Application");
		try {
			activeApp.setProperty("Visible", new Variant(false));
			Dispatch docs = activeApp.getProperty("Documents").toDispatch();
			Dispatch doc = Dispatch.invoke(docs, "Open", Dispatch.Method, 
					new Object[] { wordPath, new Variant(false), new Variant(true) }, new int[1])
					.toDispatch();
			Dispatch.invoke(doc, "SaveAs", Dispatch.Method, new Object[] {
					htmlPath, new Variant(8) }, new int[1]);
			Dispatch.call(doc, "Close", new Variant(false));
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			activeApp.invoke("Quit", new Variant[] {});
		}
	}	

	// ----------------- main() -----------------//
	public static void main(String[] args) {

		Word w = new Word();
		System.out.println("---");
		w.open("f:/dev3/WordTest.doc", false);

		try {
			int n = w.tables();
			System.out.println("共有表格：" + n);
			for (int I = 1; I <= n; I++) {
				w.table(I);
				String find = w.findTitle("表结构：");
				int rows = w.rows();
				int cols = w.cols();
				System.out.printf("-----------------------\n表名=%s \t rows=%d, cols=%d\n", find, rows, cols);
				for (int i = 1; i <= rows; i++) {
					for (int j = 1; j <= cols; j++) {
						System.out.printf("%s\t", w.cell(i,	j));
					}
					System.out.println("");
				}
				List<String> cc = w.findComment("字段描述", rows);
				System.out.printf("--字段描述\n%s\n\n", Stringx.join(cc, "\n"));
			}
			// 读字段注释
			w.table(2);
//			String s = w.findComment("公称直径：");
//			System.out.println("comment=" + s);
			List<String> comments = w.findComment("字段描述", w.rows());
			System.out.println("comments=\n" + Stringx.join(comments, "\n"));

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			w.close();
		}
	}

}
