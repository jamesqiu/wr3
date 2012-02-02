package test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestRegex {

	// ---------------------- main() ----------------------
	public static void main(String[] args) {
		TestRegex o = new TestRegex();
		System.out.println(o.match("hello cn����", "hel+o cn����"));
		System.out.println(o.find("hello cn����", "c[nm]"));
		System.out.println(o.replace("cmllo cn����", "^c[nm]", "**"));
	}
	
	boolean match(CharSequence src, String pattern) {
		
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(src);
		return m.matches();
	}
	
	String find(CharSequence src, String pattern) {

		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(src);
		if (m.find()) {
			return m.group();
		}
		return null;
	}
	
	String replace(CharSequence src, String pattern, String newString) {
		
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(src);
		return m.replaceAll(newString); 
	}
}
