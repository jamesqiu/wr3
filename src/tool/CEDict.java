package tool;

import wr3.text.LineFilter;
import wr3.text.TextFile;

import static wr3.util.Stringx.left;

/**
 * 从CE汉英词典中选择无能再分词的词元素.
 * @author jamesqiu 2009-4-28
 *
 */
public class CEDict {

	void scan(String filename) {
		Filter1 filter = new Filter1();
		TextFile.create(filter).process(filename);
	}
	
	Tokenizer token = new Tokenizer();
	
	class Filter1 implements LineFilter {

		public String process(String line) {
			
			String cn = left(line, ": ");
			if (!canSplit(cn)) {
				System.out.println(line);
			}
			return null;
		}
		
		boolean canSplit(String cn) {
			return token.list(cn).indexOf(' ')>=0;
		}
		
	}

	
	// ---------------------- main() ----------------------
	public static void main(String[] args) {
		if (args.length == 0) {
			System.out.println("过滤汉英辞典中还可分词的条目\n" + 
					"\tusage: CEDict 词典文件");
			return;
		}
		
		CEDict o = new CEDict();
		o.scan(args[0]);
	}
}
