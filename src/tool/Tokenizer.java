package tool;

import java.io.IOException;

import jeasy.analysis.MMAnalyzer;

import static wr3.util.Stringx.split;

/**
 * <pre>
 * ���ķִ�
 * </pre>
 * @author jamesqiu 2009-4-24
 *
 */
public class Tokenizer {

	MMAnalyzer analyzer = new MMAnalyzer(2); //IK_CAnalyzer();
	String sep = " "; // �ָ���
	
	public String[] words(String cn) {

		String ss = list(cn);
		return split(ss, sep);
	}
	
	public String list(String cn) {
		
		try {
			String ss = analyzer.segment(cn, sep).trim();
			return ss;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	// ---------------------- main() ----------------------
	public static void main(String[] args) {
	
		if (args.length==0) {
			System.out.println("���ķִ�. \n" +
					"\tusage: Tokenizer ����1 ����2 ...");
			return;
		}
		
		Tokenizer t = new Tokenizer();
		for (String arg : args) {
			String list = t.list(arg);
			System.out.println(list);
		}
	}
}
