package tool;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import wr3.text.LineFilter;
import wr3.text.TextFile;
import wr3.util.Classx;

import static wr3.util.Stringx.left;
import static wr3.util.Stringx.nullity;
import static wr3.util.Stringx.right;

/**
 * �õ������ִ���ƴ��(ȫƴ�ͼ�ƴ)
 *
 * @author jamesqiu 2009-4-24
 *
 */
public class Pinyin {

	private static Map<String, String> pinyins = new HashMap<String, String>();

	/**
	 * load pinyin lib
	 */
	private Pinyin() { 
		InputStream is = Classx.inputStream("pinyin/pinyin.txt");
		TextFile.create(new Filter1()).process(is);
//		System.out.println("-- init Pinyin --");				
	}
	
	private static Pinyin instance = null; 

	public static Pinyin instance() {
		if (instance==null) instance = new Pinyin();
		return instance;
	}

	class Filter1 implements LineFilter {

		public String process(String line) {
			String cn = left(line, "\t");
			String py = right(line, "\t");
			if (!pinyins.containsKey(cn))
				pinyins.put(cn, py);
			return null;
		}
	}

	/**
	 * �õ����ﴮȫƴ, ���е�Ӣ�Ĳ�����
	 *
	 * @param cn
	 * @return
	 */
	public String full(String cn) {

		return pinyin(cn, false);
	}

	/**
	 * �õ����ﴮ��ƴ, ���е�Ӣ�Ĳ�����
	 *
	 * @param cn
	 * @return
	 */
	public String jp(String cn) {

		return pinyin(cn, true);
	}

	private String pinyin(String cn, boolean jp) {

		char[] cc = cn.trim().toCharArray();
		StringBuffer rt = new StringBuffer();
		for (int i = 0; i < cc.length; i++) {
			String py = pinyins.get("" + cc[i]);
			if (py == null) {
				// Ӣ���򲻴���
				rt.append(cc[i]);
			} else {
				if (jp) {
					// ��ƴ
					rt.append(py.charAt(0));
				} else {
					// ȫƴ
					rt.append(py.substring(0, py.length() - 1));
				}
			}
		}
		return rt.toString();
	}

	/**
	 * ���ֵõ�ȫƴ, ���ֵõ���ƴ, Ӣ�Ĳ�����
	 *
	 * @param cn
	 * @return
	 */
	public String auto(String cn) {

		if (nullity(cn))
			return "";
		String rt;

		switch (cn.length()) {
		case 1:
			rt = full(cn);
			break;
		default:
			rt = jp(cn);
			break;
		}
		return rt;
	}

	static void test(final Pinyin pinyin, String filename) {
		TextFile.create(new LineFilter() {

			public String process(String line) {
				System.out.println(line + " " + pinyin.jp(line));
				return null;
			}
		}).process(filename);
	}

	/**
	 * �õ�����ƴ����Map
	 * @author jamesqiu 2011-6-2
	 * @return
	 */
	public Map<String,String> getAll() {
		return pinyins;
	}

	// ---------------------- main() ----------------------
	public static void main(String[] args) {

		if (args.length == 0) {
			System.out.println("�õ�1+�����ִ���ƴ��.\n" +
					"\tusage: Pinyin ���ִ�1 ���ִ�2 ...      ���� Pinyin -f �����ı��ļ���");
			return;
		}

		Pinyin pinyin = new Pinyin();
		if (args[0].equals("-f")) {
			test(pinyin, args[1]);
		} else {
			for (String arg : args) {
				String py = pinyin.auto(arg);
				System.out.println(py);
			}
		}
	}
}
