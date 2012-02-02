package wr3.bank;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import wr3.text.LineFilter;
import wr3.text.TextFile;
import wr3.util.Classx;
import wr3.util.Stringx;

/**
 * �ӵ��������areacode.jar�е�areacode/areacode.txt����ѯ��������
 *
 * @author jamesqiu 2009-12-8
 */
public class Areacode {

	private static Map<String, String> m1 = new HashMap<String, String>();
	static {
		m1.put("11", "����");
		m1.put("12", "���");
		m1.put("13", "�ӱ�");
		m1.put("14", "ɽ��");
		m1.put("15", "����");
		m1.put("21", "����");
		m1.put("22", "����");
		m1.put("23", "������");
		m1.put("31", "�Ϻ�");
		m1.put("32", "����");
		m1.put("33", "�㽭");
		m1.put("34", "����");
		m1.put("35", "����");
		m1.put("36", "����");
		m1.put("37", "ɽ��");
		m1.put("41", "����");
		m1.put("42", "����");
		m1.put("43", "����");
		m1.put("44", "�㶫");
		m1.put("45", "����");
		m1.put("46", "����");
		m1.put("51", "�Ĵ�");
		m1.put("52", "����");
		m1.put("53", "����");
		m1.put("54", "����");
		m1.put("61", "����");
		m1.put("62", "����");
		m1.put("63", "�ຣ");
		m1.put("64", "����");
		m1.put("65", "�½�");
	};

	/**
	 * �õ��������� ����������ʺϴ�����������������ʹ��һ��װ���ļ���Areacode
	 *
	 * @param areacode
	 *            6λ�������룬�磺532525
	 * @return �������ƣ��磺���Ϻ��ʯ����
	 */
	public static String name(String areacode) {

		InputStream is = Classx.inputStream("areacode/areacode.txt");
		AreacodeFilter filter = new AreacodeFilter(areacode);
		TextFile.create(filter).process(is);
		String rt = filter.result();
		if (rt != null)
			return rt;
		return areacode;
	}

	private static class AreacodeFilter implements LineFilter {

		String areacode = null;
		String result;

		public AreacodeFilter(String areacode) {
			this.areacode = areacode;
		}

		public String process(String line) {
			if (result != null)
				return null;
			if (line.startsWith(areacode + "\t"))
				result = Stringx.right(line, "\t");
			return null;
		}

		public String result() {
			return result;
		}
	}

	/**
	 * �õ�����30��һ������
	 *
	 * @return
	 */
	public static Map<String, String> areas1() {
		return new HashMap<String, String>(m1);
	}

	/**
	 * ����һ�������õ����ж�������Map
	 *
	 * @param code1
	 *            һ����������
	 * @return ��Ӧһ����������ж�����������
	 */
	public static Map<String, String> areas2(String code1) {

		if (Stringx.nullity(code1) || code1.length()!=2) return new HashMap<String,String>();

		InputStream is = Classx.inputStream("areacode/areacode.txt");
		class Filter2 implements LineFilter {
			String code = null;
			Map<String, String> result = new HashMap<String,String>();

			public Filter2(String code) {
				this.code = code;
			}

			public String process(String line) {
				if (line.startsWith(code))
					result.put(Stringx.left(line, "\t"), Stringx.right(line, "\t"));
				return null;
			}

			public Map<String, String> result() {
				return result;
			}
		}
		Filter2 filter = new Filter2(code1);
		TextFile.create(filter).process(is);
		return filter.result();
	}

}
