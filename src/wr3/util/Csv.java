package wr3.util;

import java.io.IOException;

import wr3.text.LineFilter;
import wr3.text.TextFile;

import com.csvreader.CsvReader;

/**
 * .csvÿ��Ԫ�صĸ�ʽ: -- �ַ�����".."��Χ, �ַ����������ַ�"�ģ���2������""��ʾ -- null����null��ʾ -- ����ֱ�ӱ�ʾ
 *
 * @author jamesqiu 2011-2-14
 */
public class Csv {

	/**
	 * ��String������ת��csv���еĺϷ��ֶ�
	 * @param o
	 * @return
	 */
	public static String toCsv(Object o) {

		if (o == null) {
			return null;
		} else if (Stringx.isString(o)) {
			return quote((String) o);
		} else if (Numberx.isNumber(o)) {
			return Json.number2json((Number) o);
		} else {
			return quote(o.toString());
		}
	}

	/**
	 * ��ȡcsv�ļ���һ�����ݵ�String[]
	 * @param line
	 * @return
	 */
	public static String[] fromCsv(String line) {
		CsvReader r = CsvReader.parse(line);
		try {
			r.readRecord();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		int n = r.getColumnCount();
		String[] rt = new String[n];
		for (int i = 0; i < n; i++) {
			try {
				rt[i] = r.get(i);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return rt;
	}

	public static String quote(String s) {
		if (s == null)
			return null;
		return "\"" + Stringx.replaceAll(s, "\"", "\"\"") + "\"";
	}

	public static void main(String[] args) {
		System.out.println(toCsv("�ҵ,,..."));
		System.out.println(toCsv("�ҵ\"..."));
		System.out.println(toCsv(1000000000000L));
		System.out.println(toCsv(12343453.14159265));

		TextFile.create(new LineFilter() {
			public String process(String line) {
				System.out.println("line=" + line);
				System.out.println(Stringx.join(fromCsv(line), "\n"));
				return line;
			}
		}).process("f:/lib/sql/csv��ʽ����.csv");
	}
}
