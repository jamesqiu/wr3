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
 * 得到汉语字串的拼音(全拼和简拼)
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
	 * 得到汉语串全拼, 其中的英文不处理
	 *
	 * @param cn
	 * @return
	 */
	public String full(String cn) {

		return pinyin(cn, false);
	}

	/**
	 * 得到汉语串简拼, 其中的英文不处理
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
				// 英文则不处理
				rt.append(cc[i]);
			} else {
				if (jp) {
					// 简拼
					rt.append(py.charAt(0));
				} else {
					// 全拼
					rt.append(py.substring(0, py.length() - 1));
				}
			}
		}
		return rt.toString();
	}

	/**
	 * 单字得到全拼, 多字得到简拼, 英文不处理
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
	 * 得到所有拼音的Map
	 * @author jamesqiu 2011-6-2
	 * @return
	 */
	public Map<String,String> getAll() {
		return pinyins;
	}

	// ---------------------- main() ----------------------
	public static void main(String[] args) {

		if (args.length == 0) {
			System.out.println("得到1+个汉字串的拼音.\n" +
					"\tusage: Pinyin 汉字串1 汉字串2 ...      或者 Pinyin -f 汉字文本文件名");
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
