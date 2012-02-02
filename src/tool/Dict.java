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

//TODO ������ѧϰ����

/**
 * �Ӻ�Ӣ�ֵ�txt�в������ĵ�Ӣ����˼; ����
 * ��Ӣ���ֵ�txt�в���Ӣ�ĵ�������˼.
 * @author jamesqiu 2009-4-23
 *
 */
public class Dict {

	Map<String, String> words = new LinkedHashMap<String, String>();

	/**
	 * �౾�ʵ�, ��ѯ˳��0,1,2,..., ��ǰ��1���鵽��ok.
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
	 * �Ѵʵ�����װ���ڴ�
	 * @param type
	 */
	private Dict(TYPE type) {
		this.type = type;
		// װ���ı��ļ�
		InputStream[] dict = (type==TYPE.CE) ? cedict : ecdict;
		for (InputStream is : dict) {
			TextFile.create(new Filter1(type)).process(is);
		}
		// װ�� Preferences
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
	 * �õ�װ������Ӣ�Ļ��������ֵ��HashMap
	 * @return
	 */
	public Map<String, String> getAll() {
		return words;
	}

	/**
	 * �Ӷ���ֵ��н��в���
	 * @param cnword
	 * @return Ӣ������, ����Ҳ����򷵻�null
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
	 * ����һ���ʵ���Ŀ�� Preferences ��
	 */
	public void learn(String key, String value) {

		if (nullity(key) || nullity(value)) return;
		pref.put(key.toLowerCase(), value);
	}

	/**
	 * �г����� Preferences �еĴʵ���Ŀ.
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
	 * ������� Preferences �еĴʵ���Ŀ
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
			System.out.println("�õ�1+�����Ĵʵļ���Ӣ�Ľ���.\n" +
					"usage: ����Ӣ Dict ����1 ����2 ...\n" +
					"       Ӣ�뺺 Dict -v english1 english2 ...\n" +
					"       ���û����� Dict -a\n" +
					"       ���û����� Dict -l\n" +
					"       ���û����� Dict -c\n");
			return;
		}

		if (args[0].equals("-v")) {
			// Ӣ��
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
			// ��Ӣ
			Dict dict = Dict.ce();
			for (String arg : args) {
				String en = dict.get(arg);
				System.out.println(en);
			}
		}

	}
}
