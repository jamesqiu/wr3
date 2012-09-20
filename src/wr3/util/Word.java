package wr3.util;

import java.util.ArrayList;
import java.util.List;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;
import com.jacob.com.Variant;

/**
 * ʹ��jacob����Excel�Ķ�д
 * ��class�İ�װĿǰ��Ҫ������ȡ��񼰱��֮ǰ������
 * @author jamesqiu 2009-4-29
 */
public class Word {

	ActiveXComponent word = new ActiveXComponent("Word.Application");
	Dispatch doc;	// ��ǰ�ĵ�
	Dispatch table; // ��ǰʹ�õ�Table
	Dispatch selection; // ��ǰѡ�е�����

	boolean readonly = true; // �Ƿ���ֻ����ʽ��

	/**
	 * ��Excel�ļ�
	 * @param filename
	 * @param visible
	 */
	public void open(String filename, boolean visible) {

		word.setProperty("Visible", visible);
		Dispatch docs = word.getProperty("Documents").toDispatch();
		doc = call(docs, "Open", filename, new Variant(false), new Variant(readonly));
		selection = get(word, "Selection"); // �����¿�ͷ
	}

	Dispatch tables;

	/**
	 * ��ʼ��doc�����е�tables
	 * @return tables������
	 * @throws Exception
	 */
	public int tables() throws Exception {

		tables = get(doc, "Tables");
		int n = getInt(tables, "Count");
		return n;
	}

	/**
	 * �����i��table
	 * @param i ��ѭvb�Ĺ淶����1��ʼ
	 * @return ���еı����Ŀ
	 */
	public Word table(int i) throws Exception {

		table = call(tables, "Item",  new Variant(i));
		// �ѹ������������֮ǰ������֮���ұ��֮ǰ���ַ������������
		call(table, "Select");
		call(selection, "MoveLeft"); // ����ƶ�����ǰѡ��֮ǰ
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
		s =  s.substring(0, s.length()-2);// ȥ�����Ļس��ͻ���
		return s;
	}

	private boolean find(String s, boolean forward) {

		Dispatch find = Dispatch.call(selection, "Find").toDispatch();
		Dispatch.put(find, "Text", s);
		Dispatch.put(find, "Forward", forward ? "True" : "False"); // true-��������, false-��������,
		boolean found = Dispatch.call(find, "Execute").getBoolean();
		return found;
	}

	/**
	 * <pre>
	 * ���ڲ��ұ����ơ�
	 * ���ز���һ����s�ַ���֮�����س�Ϊֹ���ַ���������:
	 * �ڡ���ṹ��Table1���� findBack("��ṹ��")���õ�"Table1"
	 * </pre>
	 * @param s Ҫ���ҵ��ַ���
	 * @return
	 * @throws Exception
	 */
	public String findTitle(String s) throws Exception {

		boolean found = find(s, false);
		if (!found) return "Word.findTitle() not found.";

		// ѡ��s
		call(selection, "MoveRight"); // ���Ų��s�ұ�
		Dispatch.call(selection, "MoveEndUntil", "\r"); // ѡ�й�굽�س�����ַ���
		String rt = getString(selection, "Text");
		call(selection, "MoveRight");
		return rt;
	}

	/**
	 *
	 * @param s Ҫ�������ַ��������ֶ���������
	 * @param n �ֶθ���
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
	 * ���桢�ر�Word�ļ�
	 * @param f
	 */
	public void close() {
		boolean f = false;
//		call(doc, "Save");
		Dispatch.call(doc, "Close", new Variant(f));
		word.invoke("Quit", new Variant[] {});
	}

	/**
	 * ִ��ActiveX����(�޲���)
	 * @param o ִ�з�����ActiveX����
	 * @param name ������
	 */
	private void call(Dispatch o, String name) {
		Dispatch.call(o, name);
	}

	/**
	 * ִ��ActiveX����(������)
	 * @param o ִ�з�����ActiveX����
	 * @param name ������
	 * @param args ����
	 * @return
	 */
	private Dispatch call(Dispatch o, String name, Object ... args) {
		return invoke(o, name, Dispatch.Method, args);
	}

	/**
	 * �õ�����(�޲���)
	 * @param o ActiveX����
	 * @param name ��������
	 * @return
	 */
	private Dispatch get(Dispatch o, String name) {
		return Dispatch.get(o, name).toDispatch();
	}

	/**
	 * �õ����Ե�intֵ
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
	 * ��ʱ
	 * @param ms ����, 1000����1��
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
			System.out.println("���б��" + n);
			for (int I = 1; I <= n; I++) {
				w.table(I);
				String find = w.findTitle("��ṹ��");
				int rows = w.rows();
				int cols = w.cols();
				System.out.printf("-----------------------\n����=%s \t rows=%d, cols=%d\n", find, rows, cols);
				for (int i = 1; i <= rows; i++) {
					for (int j = 1; j <= cols; j++) {
						System.out.printf("%s\t", w.cell(i,	j));
					}
					System.out.println("");
				}
				List<String> cc = w.findComment("�ֶ�����", rows);
				System.out.printf("--�ֶ�����\n%s\n\n", Stringx.join(cc, "\n"));
			}
			// ���ֶ�ע��
			w.table(2);
//			String s = w.findComment("����ֱ����");
//			System.out.println("comment=" + s);
			List<String> comments = w.findComment("�ֶ�����", w.rows());
			System.out.println("comments=\n" + Stringx.join(comments, "\n"));

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			w.close();
		}
	}

}
