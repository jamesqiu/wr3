package tool;

import static wr3.util.Stringx.left;
import static wr3.util.Stringx.nullity;
import static wr3.util.Stringx.right;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import wr3.text.LineFilter;
import wr3.text.TextFile;
import wr3.util.Classx;

//TODO 增加自学习功能

/**
 * 从汉英字典txt中查找中文的英文意思; 或者
 * 从英汉字典txt中查找英文的中文意思.
 * @author jamesqiu 2009-4-23
 *
 */
public class Dict {

	Map<String, String> words = new LinkedHashMap<String, String>();

	/**
	 * 多本词典, 查询顺序0,1,2,..., 在前面1本查到就ok.
	 */
	final InputStream[] cedict = new InputStream[] {
			Classx.inputStream("dict/2.CE.Concise.txt"),
			Classx.inputStream( "dict/1.CE.ComputerGlossary.txt"),
			Classx.inputStream( "tool/0.CE.UserDict.txt") };
	final InputStream[] ecdict = new InputStream[] {
			Classx.inputStream( "dict/2.EC.Concise.txt"),
			Classx.inputStream( "dict/1.EC.ComputerGlossary.txt"),
			Classx.inputStream( "tool/0.EC.UserDict.txt") };

	enum TYPE {CE, EC};
	TYPE type;

	public static Dict ce() {
		return new Dict(TYPE.CE);
	}

	public static Dict ec() {
		return new Dict(TYPE.EC);
	}

	/**
	 * 把词典内容装入内存
	 * @param type
	 */
	private Dict(TYPE type) {
		this.type = type;
		// 装入文本文件
		InputStream[] dict = (type==TYPE.CE) ? cedict : ecdict;
		for (InputStream is : dict) {
			TextFile.create(new Filter1(type)).process(is);
		}
		// 装入 Preferences
		fromPreferences();
	}

	private void fromPreferences() {

		try {
			for (String key : pref.keys()) {
				words.put(key, pref.get(key, null));
			}
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
	}

	class Filter1 implements LineFilter {

		TYPE type;

		Filter1(TYPE type) {
			this.type = type;
		}

		public String process(String line) {
			if (type==TYPE.CE) {
				String cn = left(line, ": ");
				String en = right(line, ": ");
				words.put(cn, en);
			} else {
				String en = left(line, ": ").toLowerCase();
				String cn = right(line, ": ");
				words.put(en, cn);
			}
			return null;
		}
	}

	/**
	 * 得到装入所有英文或者中文字典的HashMap
	 * @return
	 */
	public Map<String, String> getAll() {
		return words;
	}

	/**
	 * 从多个字典中进行查找
	 * @param cnword
	 * @return 英文释义, 如果找不到则返回null
	 */
	public String get(String cnword) {
		if (type==TYPE.CE) {
			return words.get(cnword);
		} else {
			return words.get(cnword.toLowerCase());
		}
	}

	private Preferences pref = Preferences.userNodeForPackage(Dict.class);
	/**
	 * 增加一个词典条目至 Preferences 中
	 */
	public void learn(String key, String value) {

		if (nullity(key) || nullity(value)) return;
		pref.put(key.toLowerCase(), value);
	}

	/**
	 * 列出所有 Preferences 中的词典条目.
	 */
	public void learnt() {
		try {
			for (String key : pref.keys()) {
				System.out.println(key + ": " + pref.get(key, null));
			}
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 清除所有 Preferences 中的词典条目
	 */
	public void clear() {
		try {
			pref.clear();
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
	}

	// ---------------------- main() ----------------------
	public static void main(String[] args) {

		if (args.length==0) {
			System.out.println("得到1+个中文词的简明英文解释.\n" +
					"usage: 汉译英 Dict 中文1 中文2 ...\n" +
					"       英译汉 Dict -v english1 english2 ...\n" +
					"       增用户词条 Dict -a\n" +
					"       列用户词条 Dict -l\n" +
					"       清用户词条 Dict -c\n");
			return;
		}

		if (args[0].equals("-v")) {
			// 英汉
			Dict dict = Dict.ec();
			for (int i = 1; i < args.length; i++) {
				String cn = dict.get(args[i]);
				System.out.println(cn);
			}
		} else if (args[0].equals("-a")) {
			Dict dict = Dict.ec();
			if (args.length==3) dict.learn(args[1], args[2]);
		} else if (args[0].equals("-l")) {
			Dict dict = Dict.ec();
			dict.learnt();
		} else if (args[0].equals("-c")) {
			Dict dict = Dict.ec();
			dict.clear();
		} else {
			// 汉英
			Dict dict = Dict.ce();
			for (String arg : args) {
				String en = dict.get(arg);
				System.out.println(en);
			}
		}

	}
}
