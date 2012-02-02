package wr3.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import test.wr3.util.RegexTest;
import wr3.text.LineFilter;

/**
 * <pre>
 * Java内建的正则表达式
 * usage:
 *   Regex.find("hello world", "h[ef]");
 *   Regex.match("James", "Ja[mn]es");
 *   Regex.replace("cn1,cm2", "c[nm]", "**");
 * </pre>
 * @author jamesqiu 2009-4-2
 * @see RegexTest
 */
public class Regex {

	/**
	 * get compiled pattern with case sensitive
	 * @param regex pattern string
	 * @return
	 */
	public static Pattern pattern(String regex) {

		return Pattern.compile(regex);
	}

	/**
	 * get compiled pattern with case Insensitive
	 * @param regex pattern string
	 * @return
	 */
	public static Pattern patterni(String regex) {

		return Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
	}

	/**
	 * check if src match given pattern.
	 * @param src source string
	 * @param pattern compiled pattern regex
	 * @return true if match
	 */
	public static boolean match(CharSequence src, Pattern pattern) {

		Matcher m = pattern.matcher(src);
		return m.matches();
	}

	/**
	 * 检查src是否和regex匹配.
	 * @param src
	 * @param regex
	 * @return
	 */
	public static boolean match(CharSequence src, String regex) {
		return match(src, pattern(regex));
	}

	/**
	 * find 1st pattern string in src.
	 * @param src
	 * @param pattern
	 * @return 1st matched string if find, null if not find.
	 */
	public static String find(CharSequence src, Pattern pattern) {

		Matcher m = pattern.matcher(src);
		if (m.find()) {
			return m.group();
		}
		return null;
	}

	/**
	 * 查找src中第一个匹配的子字符串.
	 * @param src
	 * @param regex
	 * @return
	 */
	public static String find(CharSequence src, String regex) {
		return find(src, pattern(regex));
	}

	/**
	 * 查找src中所有匹配的子字符串.
	 * @param src
	 * @param regex
	 * @return 所有匹配的子字符串数组
	 */
	public static String[] findAll(CharSequence src, Pattern pattern) {

		Matcher m = pattern.matcher(src);
		List<String> rt = new ArrayList<String>();
		while (m.find()) {
			rt.add(m.group());
		}
		return Stringx.list2array(rt);
	}

	/**
	 * replace all matched pattern string with newString.
	 * @param src
	 * @param pattern
	 * @param newString
	 * @return string wholely replaced.
	 */
	public static String replace(CharSequence src, Pattern pattern, String newString) {

		Matcher m = pattern.matcher(src);
		return m.replaceAll(newString);
	}

	/**
	 * 把src中所有匹配的字符串替换为新的一个固定字符串.
	 * @param src
	 * @param regex
	 * @param newString
	 * @return
	 */
	public static String replace(CharSequence src, String regex, String newString) {
		return replace(src, pattern(regex), newString);
	}

	/**
	 * 使用自定义的方法来替换src中所有匹配的字符串.
	 * 例如: 把所有匹配字符串变为大写.
	 * @param src
	 * @param pattern
	 * @param filter
	 * @return
	 */
	public static String replaceByFilter(CharSequence src, Pattern pattern, LineFilter filter) {

		Matcher matcher = pattern.matcher(src);
		StringBuffer sb = new StringBuffer();
	    while ((matcher.find())) {
	        String replaceStr = matcher.group();
	        replaceStr = filter.process(replaceStr);
	        matcher.appendReplacement(sb, replaceStr);
	    }
	    matcher.appendTail(sb);
	    String rt = sb.toString();
	    return rt;
	}

	/**
	 * 判断是否合法的Java名(类、方法、变量)
	 * @param name
	 * @return true: 形如$a, $_, _abc, $a_1$, v1, var
	 */
	public static boolean isVarName(String name) {

		if (Stringx.nullity(name)) return false;
		return name.matches("[a-zA-Z_$][$a-zA-Z0-9_$]*"); // 注：\\w === [0-9a-zA-Z]
	}

	/**
	 * 判断是否合法的Java带包类名
	 * @param name
	 * @return true: 形如 a.b.Test1, $._.test1
	 */
	public static boolean isPackageName(String name) {

		if (Stringx.nullity(name)) return false;
		return name.matches("[a-zA-Z_$][$a-zA-Z0-9_$]*(.[a-zA-Z_$][$a-zA-Z0-9_$]*)*");
	}
}
