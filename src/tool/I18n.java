package tool;

import java.io.File;
import java.io.IOException;

import wr3.text.LineFilter;
import wr3.text.TextFile;

import static wr3.util.Stringx.*;

import static wr3.util.Regex.*;

/**
 * ��Grails��domain���г�ȡ����ֶε���������.
 * 
 * @author jamesqiu 2009-4-4
 */
public class I18n {

	// ---------------------- main() ----------------------
	public static void main(String[] args) throws IOException {

		if (args.length==0) return;
		
		String name = args[0];
		new I18n().run(name);
	}

	String domainName;  // ��: VipCust
	String domainId; 	// ��: vipCust
	String domainLabel; // ��: VIP�ͻ�

	void run(String name) throws IOException {
		
		String filename = "grails-app/domain/"+name+".groovy";
		domainName = leftback(new File(filename).getCanonicalFile().getName(), 
							".groovy");
		domainId = lower(domainName);
		// 
		LineFilter filter = new LineFilter() {
			public String process(String line) {
				line = line.trim();
				String label = label(line);
				if (line.indexOf("//") > 0) {
					if (find(line, "class[ ]+"+domainName) != null) { // class
						domainLabel = label;
						print("#------ " + domainLabel);
					} else { // field
						String prop = prop(line);
						print(domainId+"."+prop+" = "+label);
					}
				}
				return null;
			}
		};
		TextFile.create(filter).processUtf8(filename);
		
		print("");
		print(domainId+".create = ����"+domainLabel); 
		print(domainId+".edit = �༭"+domainLabel);
		print(domainId+".list = "+domainLabel+"�б�");
		print(domainId+".new = ����"+domainLabel);
		print(domainId+".show = ��ʾ"+domainLabel); 
		print(domainId+".created = "+domainLabel+"{0}�������"); 
		print(domainId+".updated = "+domainLabel+"{0}�������");
		print(domainId+".deleted = "+domainLabel+"{0}ɾ�����"); 
		print("");
		print(domainId+".not.found = δ����IDΪ{0}��"+domainLabel);

	}

	String label(String line) {
		return rightback(line, "//").trim();
	}
	
	/**
	 * e.g.: 
	 * private String s
	 * int i = 10;
	 */
	String prop(String line) {
		
		line = left(line, "//").trim();
		if (line.endsWith(";")) line = leftback(line, ";").trim();
		if (line.indexOf("=") > 0) line = leftback(line, "=").trim();
		
		return rightback(line, " ").trim();
	}
	
	static void print(String s) {
		System.out.println(s);
	}
}
