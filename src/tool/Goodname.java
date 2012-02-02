package tool;

import static wr3.util.Stringx.capitalize;
import static wr3.util.Stringx.join;
import static wr3.util.Stringx.lower;
import static wr3.util.Stringx.replaceAll;
import static wr3.util.Stringx.split;
import wr3.util.Charsetx;

//TODO 中文词句-->分词-->找英文-->找拼音简拼(单字用全拼)-->组成英文变量名
//TODO 反之, 英文变量名-->自动中文注释

/**
 * 得到一个中文词句的简明英文解释
 * @author jamesqiu 2009-4-24
 *
 */
public class Goodname {

	/**
	 * 初始化分词器
	 */
	Tokenizer token;
	/**
	 * 初始化汉英辞典
	 */
	Dict cedict;
	/**
	 * 初始化英汉辞典
	 */
	Dict ecdict;
	/**
	 * 初始化汉字拼音表
	 */
	Pinyin pinyin;
	
	private Goodname() {
		token = new Tokenizer();
		cedict = Dict.ce();
		ecdict  = Dict.ec(); 
		pinyin = Pinyin.instance();
//		System.out.println("-- init Goodname --");
	}
	
	private static Goodname instance = null;
	
	public static Goodname instance () {
		if (instance==null) instance = new Goodname();
		return instance;
	}

	/**
	 * 切分，中文则分词为中文数组，骆驼命名法则分为单词数组
	 * @param cn
	 * @return
	 */
	public String[] token(String s) {
		if (Charsetx.hasChinese(s))
			return token.words(s);
		else
			return var2words(s);
	}

	/**
	 * <pre>
	 * 得到中文串的CamelStype英文变量名.
	 * 如: 客户信息 -> CustomerInformation
	 * @param cn 中文字串
	 * @return
	 */
	public String en(String cn) {

		// 中文分词
		String[] words = token.words(cn);
		// DEBUG: 分词结果调试信息
//		System.out.println(join(words, " "));

		StringBuffer sb = new StringBuffer();
		for (String word : words) {
			// 得到英文
			String en = cedict.get(word);
			// 没有英文释义的得到拼音表示
			if (en==null) {
				en = pinyin.auto(word);
			}
			sb.append(words2var(en));
		}
		return lower(sb.toString());
	}

	/**
	 * <pre>
	 * 得到骆驼样式英文变量的中文解释. 如:
	 * VIPCustomer/VipCustomer/vipCustomer -> 贵宾客户
	 * </pre>
	 * @param en
	 * @return
	 */
	public String cn(String en) {

		// 得到多个单词
		String[] words = var2words(en);
		StringBuilder sb = new StringBuilder();
		for (String word : words) {
			// 得到单词中文
			String cn = ecdict.get(word);
			if (cn==null) cn = word;
			sb.append(cn);
		}
		return sb.toString();
	}

	/**
	 * <pre>
	 * 把一个骆驼样式表示的变量分为单词, 如:
	 *  vipCust -> vip Cust
	 *  VipCust -> Vip Cust
	 *  VIPCust -> VIP Cust
	 *  openDB -> open DB
	 *  DBServer -> DB Server
	 *  VIP -> VIP
	 *  aZ -> a Z
	 *  aa -> aa
	 * </pre>
	 * @param varName
	 * @return
	 */
	private String[] var2words(String varName) {

		StringBuilder sb = new StringBuilder();
		char[] chars = varName.toCharArray();
		int n = chars.length;
		int begin = 0;
		boolean b1; // 是否大写
		boolean b2; // 前后是否不同
		for (int i = 1; i < n; i++) {
			// 找到单词的开头(规则: 是大写字母且前后至少有一个小写字母)
			b1 = Character.isUpperCase(chars[i]);
			if (i==(n-1)) {
				b2 = false;
			} else {
				b2 = Character.isLowerCase(chars[i-1])
					|| Character.isLowerCase(chars[i+1]);
			}
			if (b1 && b2) {
				// 找到单词的开头
				sb.append(varName.substring(begin, i)).append(' ');
				begin = i;
			}
		}
		sb.append(varName.substring(begin, n));
		return split(sb.toString(), " ");
	}

	// 得到1+英文词的骆驼样式表示的变量名
	private String words2var(String en) {
		en = replaceAll(en, new String[]{".", ","}, new String[]{"", ""});
		String[] ss = split(en, " ");
		for (int i = 0; i < ss.length; i++) {
			ss[i] = capitalize(ss[i], true);
		}
		return join(ss, "");
	}

	// ---------------------- main() ----------------------
	public static void main(String[] args) {

		if (args.length==0) {
			System.out.println("中文词句的简明英文解释.\n" +
					"usage: Goodname 中文词句1 中文词句2 ...\n" +
					"       Goodname -v varName1 varName2 ...");
			return;
		}

		Goodname o = new Goodname();

		if (args[0].equals("-v")) {
			for (int i = 1; i < args.length; i++) {
				String en = args[i];
				System.out.println(o.cn(en));
			}
		} else {
			for (int i = 0; i < args.length; i++) {
				String cn = args[i];
				System.out.println(o.en(cn));
			}
		}
	}
}
