package tool;

import wr3.text.LineFilter;
import wr3.text.TextFile;

import static wr3.util.Stringx.*;

/**
 * 把xml汉英辞典(从金山词霸转)变为txt格式.
 * 由于KSDrip.exe转出来的格式中"解释项"格式不确定, 需要自定定制process().
 * @author jamesqiu 2009-4-23
 *
 */
public class Xml2txt {

	static boolean is(String line, String tag) {
		return line.startsWith("<"+tag+">") 
			&& line.endsWith("</"+tag+">");
	}
	
	void scan(String filename) {
		Filter1 filter = new Filter1();
		TextFile.create(filter).process(filename);
	}
	
	class Filter1 implements LineFilter {

		String cn;
		String en;
		
		/**
		 * 需要按照不同文件进行定制.
		 */
		public String process(String line) {
			
			if (is(line, "DC")) {
				en = between(line, "<DC><![CDATA[", "]]></DC>");
				if (en.indexOf(' ')>=0 || en.indexOf('-')>=0 || en.indexOf('.')>=0) 
					en = null;
			}
			if (en !=null && cn==null && is(line, "JX")) {
				cn = between(line, "<JX><![CDATA[", "]]></JX>");
				cn = remove(cn, "(", ")");
				cn = remove(cn, "〈", "〉");
				cn = remove(cn, "（", "）");
				cn = choose(cn);
			}
			if (line.equals("</CK>")) {
				if (en == null) {
					cn = null;
					return null;
				}
				if (cn!=null) System.out.println(en + ": " + cn);
				cn = null;
			}
			
			return null;
		}
		
		/**
		 * @param jx 解释
		 * @return
		 */
		String choose(String jx) {

			// 选最短的解释
			String[] ss = jx.split("[,;，]");
			if (ss.length==0) return null;
			String s0 = ss[0].trim();
			for (String s : ss) {
				s = s.trim();
				if ("".equals(s)) continue;
				if(s.length()<s0.length()) s0 = s;
			}
			
			return s0;
		}
		
		/**
		 * 去除所有在before和after之间的字符串, 如:
		 * remove("hello(,)world(abc)", "(", ")"); // helloworld
		 * @param s
		 * @param before
		 * @param after
		 * @return
		 */
		String remove(String s, String before, String after) {
			while (s.indexOf(before) >=0) {
				s = replaceBetween(s, before, after, "");
				s = replace(s, before+after, "");
			}
			return s;
		}
	}
	
	// ---------------------- main() ----------------------
	public static void main(String[] args) {

		
		switch (args.length) {
		case 0:
			System.out.println("usage: Xml2txt filename");
			return;
		default:
			new Xml2txt().scan(args[0]);			
			break;
		}
	}
}
