package wr3.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import test.wr3.util.NumberxTest;
/**
 * Number (int, Double) handle utilities.
 * <pre>
 * usage:

	// ����
	Numberx.random(10);								// int[10]�������(0~100)
	Numberx.getSafeIndex (4, 3);					// return 1
	Numberx.getSafeRange (10, -2, 3); 				// return 3
	Numberx.getNumberString (new Double(10.343)); 	// "10.343"
	Numberx.getInt ("-234d", -100);					// -100

	// �������
	Numberx.power (2, 4); 							// 16
	Numberx.sum ({0.1, 0.3, 0.6});					// 1.0
	Numberx.radio ({0.2, 0.3});						// {0.4, 0.6}
	Numberx.hasZero ({0.1, 4.0, 0, 1.2});			// true
	Numberx.zeros ({2, 5, 0, 4, 0});				// {1,1,0,1,0}
	Numberx.toDouble ({2, 5, 0, 4});				// {2.0, 5.0, 0.0, 4.0}
	Numberx.toInt ({2.3, 5.8, 0.0, 4.0});			// {2, 5, 0, 4}
	Numberx.multiply ({5.0, 5.0}, {0.1, 0.9})		// {0.5, 4.5}
	Numberx.adjustSum ({2,3,5}, {0,4,5})			// {0, (40+20*3/8), (50+20*5/8)}
	Numberx.adjustRound ({3.3, 3.3, 3.4}, 0);		// {4, 3, 3}
	Numberx.isDigit ("234324");						// true

	// Excel ���к�
	Numberx.Excel2Ints ("AB30");					// {27, 29}
	Numberx.AZ2Int ("AB");  						// 27
	Numberx.Int2AZ (27);							// "AB"

	// �ж�����
	Numberx.isNumber(object);

	// ������λ����
	Numberx.digits(2008);							// [2,0,0,8]
	Numberx.digitSet(-2008);						// [0,2,8]

 *  </pre>
 * @see NumberxTest
 * @see Stringx, Datetime
 */
public class Numberx {

	/**
	 * �õ�һ��[0, 100]��Χ���������
	 * @return
	 */
	public static int random() {
		return random(100);
	}

	/**
	 * �õ�һ��[0, max]��Χ���������
	 * @param max
	 * @return
	 */
	public static int random(int max) {
		return new Random().nextInt(max + 1);
	}

	/**
	 * �õ�����Ϊn��һϵ��[0, 100]��Χ���������
	 * @param n ������еĳ���
	 * @return �������, ��: random(5) = [20, 0, 75, 3, 100]
	 */
	public static int[] randoms(int n) {

		return randoms(n, 100);
	}

	/**
	 * �õ�����Ϊn��һϵ��[0, max]��Χ���������
	 * @param n
	 * @param max
	 * @return
	 */
	public static int[] randoms(int n, int max) {

		if (n < 0) n = 0;
		if (max <= 0) max = 1;

		int[] rt = new int[n];
		Random random = new Random();
		for (int i = 0; i < n; i++) {
			rt[i] =random.nextInt(max + 1);
		}
		return rt;
	}

	/**
	 * <pre>
	 * ������index�޶��ڰ�ȫ��Χ��[0, array.length-1]
	 * return index in range [0, length)
	 * condition: length > 0;
	 * ���磺
	 * -1 --> ���һ��(array.length-1),
	 * -2 --> ������2��(array.length-2)
	 * </pre>
	 * @param index ����intֵ
	 * @param length ����Ĵ�С��array.length��, �������0
	 * @param [0, length-1]��Χ�ڵ�����
	 */
	public static int safeIndex (int index, int length) {

		if (length<1) return 0;

		index %= length;	// �޶���-(length-1),-1,0...(length-1)
		if (index<0) index += length;
		return index;
//		return (index % length) + (index < 0 ? length : 0);
	}

	/**
	 * �Ѹ���ֵn�޶���ָ������[min, max]
	 * return int value in range [min, max]
	 */
	public static int safeRange (int n, int min, int max) {

		if (n < min) return min;
		if (n > max) return max;
		return n;
	}

	/**
	 * �õ�һ����������������[from, to]
	 * @see {@link #range(int, int, int)}
	 * @param from
	 * @param to
	 * @return
	 */
	public static List<Integer> range(int from, int to) {
		return range(from, to, 1);
	}

	/**
	 * �õ�һ����������������[from, to]
	 * @param from ��ʼ
	 * @param to ����
	 * @param step �����ɸ�
	 * @return
	 */
	public static List<Integer> range(int from , int to, int step) {

		List<Integer> rt = new ArrayList<Integer>();

		if ( (step==0) || ((from>to) && (step>0)) || ((from<to) && (step<0)) )
			return rt;

		if (from < to) {
			for (int i = from; i <= to; i+=step) {
				rt.add(i);
			}
		} else {
			for (int i = from; i >= to; i+=step) {
				rt.add(i);
			}
		}
		return rt;
	}

	/**
	 * return formated string of a double.
	 * -10.0 --> -10
	 * -10.53 --> -10.53
	 */
	public static String toString (Double number) {

		if (number == null) return "";

        return toString(number.doubleValue());
	}

	/**
	 * return formated string of a double.
	 * -10.0 --> -10
	 * -10.53 --> -10.53
	 */
	public static String toString (double number) {

		return toString(number, "#.########");
	}

	/**
	 * return formated string of a double.
	 * @param number
	 * @param formatPattern ����: "#.##"
	 * @return
	 */
	public static String toString(double number, String formatPattern) {

		if (Double.isNaN(number)) return "NaN";

		NumberFormat formatter = new DecimalFormat (formatPattern);
		String string = formatter.format (number);

		return string;
	}

	/**
	 * safe version of Integer.parseInt ();
	 * return transfered int value of given valid string, default int value for invalid string.
	 */
	public static int toInt (String s, int defaultValue) {
		try {
			return Integer.parseInt(s);
		} catch (Exception e) { // NumberFormatException, NullPointerException, ..
			return defaultValue;
		}
	}

	public static long toLong(String s, long defaultValue) {
		try {
			return Long.parseLong(s);
		} catch (Exception e) { // NumberFormatException, NullPointerException, ..
			return defaultValue;
		}
	}

	/**
	 * get double from string.
	 * @param s double string to parse.
	 * @param defaultValue default value if exception.
	 * @return double
	 */
	public static double toDouble (String s, double defaultValue) {
		try {
			return Double.parseDouble(s);
		} catch (Exception e) { // NumberFormatException, NullPointerException, ..
			return defaultValue;
		}
	}

	/**
	 * ��doubleת��Ϊռ��, ��: 1.03 -> 103%, 3.14159 -> 314.16%, 0.0350 -> 3.5%
	 * @param d
	 * @return
	 */
	public static String toPercent (double d) {
		return toString(d, "#.##%");
	}
	/**
	 * alias of {@link #toPercent(double)}
	 * @param d
	 * @return
	 */
	public static String percent(double d) { return toPercent(d); }

	/**
	 * return n**m,
	 * this is to compute integer, Math.pow() is to compute double.
	 */
	public static int power (int n, int m) {
		int rt = 1;
		for (int i = 0; i < m; i++) {
			rt *= n;
		}
		return rt;
	}

	/**
	 * @return sum of int[] array
	 * @param values �������int[]
	 */
	public static double sum (double[] values) {
		if (values == null) return 0;
		double rt = 0.0;
		for (int i = 0; i < values.length; i++) {
			rt += values[i];
		}
		return rt;
	}

	/**
	 * ����int[]��ÿ��Ԫ�ص�ռ��
	 */
	public static double[] radio (double[] values) {
		if (values == null) return null;

		double sum = sum (values);
		int n = values.length;
		double[] rt = new double[n];
		for (int i = 0; i < n; i++) {
			rt[i] = (sum==0) ? Double.NaN : (values[i]/sum);
		}
		return rt;
	}

	/**
	 * �ж��Ƿ���ֵΪ0��Ԫ��
	 */
	public static boolean hasZero (double[] values) {
		if (values==null) return false;
		for (int i = 0; i < values.length; i++) {
			if (values[i] == 0.0) return true;
		}
		return false;
	}

	/**
	 * �����з���Ԫ����Ϊ1
	 * @param values like {2, 5, 0, 4, 0}
	 * @return {1, 1, 0, 1, 0}
	 */
	public static double[] zeros (double[] values) {
		if (values==null) return null;
		int n = values.length;
		double[] rt = new double[n];
		for (int i = 0; i < n; i++) {
			rt[i] = values[i]==0 ? 0.0 : 1.0;
		}
		return rt;
	}

	/**
	 * ��int[]ǿ��ת��Ϊdouble[]
	 */
	public static double[] toDouble (int[] values) {
		if (values==null) return null;
		double[] rt = new double[values.length];
		for (int i = 0; i < values.length; i++) {
			rt[i] = (double) values[i];
		}
		return rt;
	}

	/**
	 * ��double[]ǿ��ת��Ϊint[]
	 */
	public static int[] toInt (double[] values) {
		if (values==null) return null;
		int[] rt = new int[values.length];
		for (int i = 0; i < values.length; i++) {
			rt[i] = (int) values[i];
		}
		return rt;
	}

	/**
	 * ֵ��double[]����ϵ����double[]�����
	 */
	public static double[] multiply (double[] values, double[] radios) {
		if (values==null || radios==null) return null;
		if (values.length != radios.length) return null;
		int n = values.length;
		double[] rt = new double[n];
		for (int i = 0; i < n; i++) {
			rt[i] = values[i] * radios[i];
		}
		return rt;
	}

	/**
	 * �ܷ�ֵ��ƽ����ƽ�����sumֵ == ��׼ֵ��sumֵ�����磺
	 * bases��20%��30%��50%
	 * values: 0%, 40%, 50%
	 * -->
	 * return: 0%, (40+20*3/8)%, (50+20*5/8)%
	 *
	 * @param v0 ��׼ֵ
	 * @param v1 ʵ��ֵ
	 * @return ����ֵ
	 */
	public static double[] adjustSum (double[] v0, double[] v1) {

		if (v0==null || v1==null) return null;
		if ((v0.length != v1.length) || !hasZero(v1))
			return v1;

		int n = v0.length;
//		double sum = sum (v1);
		double sum1 = sum(v0) - sum(v1);
		double[] zeros = zeros (v1);
		double[] v2 = multiply(v0, zeros);
		double[] radio = radio (v2);
		double[] rt = new double[n];
		for (int i = 0; i < n; i++) {
			rt[i] = v1[i] + sum1 * radio[i];
		}
		return rt;
	}

	/**
	 * ���������ƽ��double[]ÿ��ֵ���������������ƽ��ĳ��ָ��ֵ�ϣ����磺
	 * {3.3, 3.3, 3.4} -->
	 * {3, 3, 4}
	 * @param index ������ƽ���Ǹ�ֵ��index
	 * ��֤��Math.round (δ��ƽ֮��) = ��ƽ֮��
	 */
	public static double[] adjustRound (double[] values, int index) {
		if (values==null) return null;
		int n = values.length;
		double[] rt = new double[n];
		double sum0 = Math.round(sum (values));
		for (int i = 0; i < n; i++) {
			rt[i] = Math.round(values[i]);
		}
		double sum1 = sum (rt);
		if (sum0 != sum1) {
			double delta = sum0 - sum1;
			int safeIndex = safeIndex(index, values.length);
			rt[safeIndex] += Math.round (delta);
		}

		return rt;
	}

	/**
	 * �ж��Ƿ��ʾ�����������ַ���������С�����"-"����
	 */
	public static boolean isDigit (String value) {
		if (value==null) return false;
		char[] chars = value.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			if (!Character.isDigit(chars[i])) return false;
		}
		return true;
	}

	/**
	 * �ж�Object�Ƿ�ΪNumber����
	 * @param o ���ж����͵Ķ���
	 * @return
	 */
	public static boolean isNumber(Object o) {
		if (o==null) return false;
		return o.getClass().getSuperclass()==Number.class;
	}



	/**
	 * ��Excel�ĵ�Ԫ��λ�ñ�ʶת��Ϊint(col, row)��
	 * ���򣺵�һ����������ĸ���ڶ�������ĸ�������֣�֮���ȫ������
	 * A1->(0,0), IV3->(255,2)
	 */
	public static int[] excel2Ints (String code) {

		int[] rt = new int[]{-1,-1};
		if (code==null || code.length()<2) {
			System.err.println("Numberx.Excel2Ints() ����������ԣ�" + code);
			return rt;
		}
		char[] chars = code.toCharArray();
		if (!Character.isLetter(chars[0])) {
			System.err.println("Numberx.Excel2Ints() ����������ԣ�" + code);
			return rt;
		}
		int colLen = Character.isLetter(chars[1]) ? 2 : 1;
		String col = code.substring(0, colLen);
		String row = code.substring(colLen);

		if (!isDigit(row)) {
			System.err.println("Numberx.Excel2Ints() ����������ԣ�" + code);
			return rt;
		}

		int col_i = AZ2Int(col);
		int row_i = toInt(row, 0);
		if (row_i==0) {
			System.err.println("Numberx.Excel2Ints() ����������ԣ�" + code);
			return rt;
		}

		return new int[]{col_i, row_i-1};
	}

	/**
	 * ��Excel���к�(A-ZZ)ת��Ϊ��ֵ��
	 * A->0, B->1, Z->25, AA->26, IV->255
	 */
	private static int RANGE = 'Z'-'A'+1;	 // 26
	public static int AZ2Int (String AZ) {

		if (AZ == null || (AZ.length() != 1 && AZ.length() != 2)) {
			System.err.println("Numberx.AZtoInt() �д��Ų�����Ҫ��" + AZ);
			return -1;
		}
		AZ = AZ.toUpperCase();
		if (AZ.length() == 1) {
			return (AZ.charAt(0)-'A');
		}
		char c0 = AZ.charAt(0);
		char c1 = AZ.charAt(1);
		return ((c0-'A'+1)*RANGE + (c1-'A'));
	}

	/**
	 * ����ֵ(0-255)ת��ΪExcel���к�(A-ZZ)��
	 * 0->A, 1->B, 25->Z, 26->AA, 255->IV
	 */
	public static String int2AZ (int i) {

		if (i < 0 || i > 255) {
			System.err.println("�б�ų���[0-255]��" + i);
			return null;
		}
		if (i < RANGE) {
			return "" + (char)('A' + i);
		} else {
			return ("" + (char)('A' + i/RANGE - 1) + (char)('A' + i%RANGE));
		}
	}

	/**
	 * �õ�һ�������ĸ�λ����, 2008 -> [2,0,0,8]
	 * @param argv
	 */
	public static int[] digits(int n) {
		n = Math.abs(n);
		char[] chars = ("" + n).toCharArray();
		int[] digits = new int[chars.length];
		for (int i = 0; i < chars.length; i++) {
			digits[i] = chars[i] - '0';
		}
		return digits;
	}

	/**
	 * �õ�һ�������ĸ�λ���ֵ�����Set, 2008 -> [0,2,8]
	 * @param n
	 * @return
	 */
	public static Set<Integer> digitSet(int n) {

		int[] digits = digits(n);
		Set<Integer> set = new TreeSet<Integer>();
		for (int i = 0; i < digits.length; i++) {
			set.add(digits[i]);
		}
		return set;
	}

	/**
	 * �õ������Ķ����Ʊ�ʾ�ַ����������������š�
	 * @param n
	 * @return �磺9 -> "1001"
	 */
	public static String binary(int n) {

		return bin(n);
	}

	/**
	 * �ж��Ƿ�������������
	 * @param i
	 */
	public static boolean isPrime(int n) {

		if (n<=1) return false;
		if (n==2 || n==3 || n==5 || n==7) return true;
		if (n%2==0 || n%3==0 || n%5==0 || n%7==0) return false; // �Ѵ󲿷ַ�������ɸ��
		for (int i = 7; i*i>0 && i*i<=n; i+=2) { // ��ʹ��sqrt�ٶȿ�
			if (n%i==0) return false;
		}
		return true;
	}

	/**
	 * ��һ��10����������ʾΪ16����Сд
	 * @param n
	 * @return
	 */
	public static String hex(int n) {
		return Integer.toString(n, 16);
	}

	/**
	 * ��һ��10����������ʾΪ2����
	 * @param n
	 * @return
	 */
	public static String bin(int n) {
		return Integer.toString(n, 2);
	}

	/**
	 * ��16���Ʊ�ʾ������0x��ͷ��ת��Ϊ10��������
	 * @param hex
	 * @return
	 */
	public static int hex2i(String hex) {
		try {
			return Integer.parseInt(hex, 16);
		} catch (Exception e) {
			return -1;
		}
	}

	/**
	 * ��2���Ʊ�ʾ������0x��ͷ��ת��Ϊ10��������
	 * @param bin
	 * @return
	 */
	public static int bin2i(String bin) {
		try {
			return Integer.parseInt(bin, 2);
		} catch (Exception e) {
			return -1;
		}
	}

	//----------------- main() -----------------//
	public static void main(String argv[]) {

		String[] ss = {
			//"A1", "B3", "AA1", "IV52342", "zZ1024"
			" A1", "112", "A-3", "Azz3", "AZ-2.3"
		};
		for (int i = 0; i < ss.length; i++) {
			System.out.println(Arrays.asList(excel2Ints(ss[i])));
		}

		System.out.println(toString(new Double(-10.3343)));
		System.out.println(toString(new Double(-10.0)));
	} // main ()

} // class
