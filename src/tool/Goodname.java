package tool;

import static wr3.util.Stringx.capitalize;
import static wr3.util.Stringx.join;
import static wr3.util.Stringx.lower;
import static wr3.util.Stringx.replaceAll;
import static wr3.util.Stringx.split;
import wr3.util.Charsetx;

//TODO ���Ĵʾ�-->�ִ�-->��Ӣ��-->��ƴ����ƴ(������ȫƴ)-->���Ӣ�ı�����
//TODO ��֮, Ӣ�ı�����-->�Զ�����ע��

/**
 * �õ�һ�����Ĵʾ�ļ���Ӣ�Ľ���
 * @author jamesqiu 2009-4-24
 *
 */
public class Goodname {

	/**
	 * ��ʼ���ִ���
	 */
	Tokenizer token;
	/**
	 * ��ʼ����Ӣ�ǵ�
	 */
	Dict cedict;
	/**
	 * ��ʼ��Ӣ���ǵ�
	 */
	Dict ecdict;
	/**
	 * ��ʼ������ƴ����
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
	 * �з֣�������ִ�Ϊ�������飬�������������Ϊ��������
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
	 * �õ����Ĵ���CamelStypeӢ�ı�����.
	 * ��: �ͻ���Ϣ -> CustomerInformation
	 * @param cn �����ִ�
	 * @return
	 */
	public String en(String cn) {

		// ���ķִ�
		String[] words = token.words(cn);
		// DEBUG: �ִʽ��������Ϣ
//		System.out.println(join(words, " "));

		StringBuffer sb = new StringBuffer();
		for (String word : words) {
			// �õ�Ӣ��
			String en = cedict.get(word);
			// û��Ӣ������ĵõ�ƴ����ʾ
			if (en==null) {
				en = pinyin.auto(word);
			}
			sb.append(words2var(en));
		}
		return lower(sb.toString());
	}

	/**
	 * <pre>
	 * �õ�������ʽӢ�ı��������Ľ���. ��:
	 * VIPCustomer/VipCustomer/vipCustomer -> ����ͻ�
	 * </pre>
	 * @param en
	 * @return
	 */
	public String cn(String en) {

		// �õ��������
		String[] words = var2words(en);
		StringBuilder sb = new StringBuilder();
		for (String word : words) {
			// �õ���������
			String cn = ecdict.get(word);
			if (cn==null) cn = word;
			sb.append(cn);
		}
		return sb.toString();
	}

	/**
	 * <pre>
	 * ��һ��������ʽ��ʾ�ı�����Ϊ����, ��:
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
		boolean b1; // �Ƿ��д
		boolean b2; // ǰ���Ƿ�ͬ
		for (int i = 1; i < n; i++) {
			// �ҵ����ʵĿ�ͷ(����: �Ǵ�д��ĸ��ǰ��������һ��Сд��ĸ)
			b1 = Character.isUpperCase(chars[i]);
			if (i==(n-1)) {
				b2 = false;
			} else {
				b2 = Character.isLowerCase(chars[i-1])
					|| Character.isLowerCase(chars[i+1]);
			}
			if (b1 && b2) {
				// �ҵ����ʵĿ�ͷ
				sb.append(varName.substring(begin, i)).append(' ');
				begin = i;
			}
		}
		sb.append(varName.substring(begin, n));
		return split(sb.toString(), " ");
	}

	// �õ�1+Ӣ�Ĵʵ�������ʽ��ʾ�ı�����
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
			System.out.println("���Ĵʾ�ļ���Ӣ�Ľ���.\n" +
					"usage: Goodname ���Ĵʾ�1 ���Ĵʾ�2 ...\n" +
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
