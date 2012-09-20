package test;

import java.io.IOException;

import wr3.text.LineFilter;
import wr3.text.TextFile;
import wr3.util.Filex;
import wr3.util.Stringx;

public class Test3 {

	public static boolean hasMultiName(String s) {
		String s2 = Stringx.between(s, "<DNAME>", "</DNAME>");
		return s2.indexOf(";")!=-1;
	}
	
	public static String splitMultiName(String s) {
		String[] names = Stringx.split(Stringx.between(s, "<DNAME>", "</DNAME>"), ";");
		String type = "<DTYPE>" + Stringx.between(s, "<DTYPE>", "</DTYPE>") + "</DTYPE>";
		StringBuffer sb = new StringBuffer();
		for(String name : names) {
			sb.append(" <Row><DNAME>"+name+"</DNAME> "+type+" </Row>\n");
		}
		return sb.toString();
	}
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {

		String s = Filex.getFileText("b:/duty.xml", "utf-8");
		final StringBuffer out = new StringBuffer();
		LineFilter lf = new LineFilter () {
			public String process (String line) {
				String rt = line;
				if (hasMultiName(line)) {
					rt = splitMultiName(line);
				}
				out.append(rt + (rt.endsWith("\n") ? "" : "\n"));
				return rt;
			}
		};	
		TextFile.create(lf).processUtf8("b:/duty.xml");
		
		Filex.writeUtf8("duty-out.xml", out.toString());
			    
	}

}
