package wr3.table;

import static wr3.util.Stringx.between;
import static wr3.util.Stringx.nullity;
import static wr3.util.Stringx.replaceInclude;
import static wr3.util.Stringx.right;
import static wr3.util.Stringx.split;
import wr3.util.Stringx;

/**
 * <pre>
 * CubeTable��ά��ָ�겼���ַ���������
 * ��3�����ַ���������������, ����ȥ�ء�ȥ�ո�ȥ���ַ�������
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
	private boolean measureOnTop = true;	// ȱʡΪtrue
	
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
			// ��ʼ��Ϊ��
			top = new String[0];
			left = new String[0];
			measure = new String[0];
			return;
		}

		// ����express����ʼ�� this.layout
		String sep = "\\"; // �ϡ���ķָ���		
		// ���û��д"\\"�����ϱ�ʡ���˵�"\\"��
		if (express.indexOf(sep) == -1) express = express + sep;
		
		// ָ��
		int i1 = express.indexOf('[');
		int i2 = express.indexOf(']');
		if ( i1 >= 0 && i2 >=0 && i1 < i2 ) {
			String measures0 = between(express, "[", "]");
			measure = split(measures0, ",");
			measureOnTop = (express.indexOf(sep) < express.indexOf('['));
			// ժ��ָ��
			express = replaceInclude(express, "[", "]", "");
		} else {
			measure = new String[0];
		}
		
		// ��ά��
		String left0 = Stringx.left(express, sep).trim();
		left = split(left0, ",");
		
		// ��ά��
		String top0 = right(express, sep).trim();
		top = split(top0, ",");
	}	
}
