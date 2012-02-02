package wr3.table;

import static wr3.util.Stringx.between;
import static wr3.util.Stringx.nullity;
import static wr3.util.Stringx.replaceInclude;
import static wr3.util.Stringx.right;
import static wr3.util.Stringx.split;
import wr3.util.Stringx;

/**
 * <pre>
 * CubeTable的维度指标布局字符串分析。
 * 把3部分字符串分析出来返回, 不作去重、去空格、去空字符串处理
 * </pre>
 * @author jamesqiu 2009-11-23
 */
public class CubeLayoutParser {

	private CubeLayoutParser() {}
	
	public static CubeLayoutParser create(String express) {
		
		CubeLayoutParser instance = new CubeLayoutParser();
		instance.parseLayout(express);
		return instance;
	}
	
	private String[] top;
	private String[] left;
	private String[] measure;
	private boolean measureOnTop = true;	// 缺省为true
	
	public String[] top() {
		
		return top;
	}
	
	public String[] left() {
		
		return left;
	}
	
	public String[] measure() {
	
		return measure;
	}
	
	public boolean measureOnTop() {
		
		return measureOnTop;
	}
	
	private void parseLayout(String express) {
		
		if (nullity(express)) {
			// 初始化为空
			top = new String[0];
			left = new String[0];
			measure = new String[0];
			return;
		}

		// 解析express，初始化 this.layout
		String sep = "\\"; // 上、左的分隔符		
		// 如果没有写"\\"，加上被省略了的"\\"，
		if (express.indexOf(sep) == -1) express = express + sep;
		
		// 指标
		int i1 = express.indexOf('[');
		int i2 = express.indexOf(']');
		if ( i1 >= 0 && i2 >=0 && i1 < i2 ) {
			String measures0 = between(express, "[", "]");
			measure = split(measures0, ",");
			measureOnTop = (express.indexOf(sep) < express.indexOf('['));
			// 摘除指标
			express = replaceInclude(express, "[", "]", "");
		} else {
			measure = new String[0];
		}
		
		// 左维度
		String left0 = Stringx.left(express, sep).trim();
		left = split(left0, ",");
		
		// 上维度
		String top0 = right(express, sep).trim();
		top = split(top0, ",");
	}	
}
